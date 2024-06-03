import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;


public class GRDM_U4_592027 implements PlugInFilter {

    protected ImagePlus imp;
    final static String[] choices = {"Wischen", "Weiche Blende", "Ueberlagerung", "Schiebblende", "Chroma Key", "Extra"};

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_RGB+STACK_REQUIRED;
    }

    public static void main(String args[]) {
        ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
        ij.exitWhenQuitting(true);

        IJ.open("C:/Users/Homam/Downloads/StackB.zip");

        GRDM_U4_592027 sd = new GRDM_U4_592027();
        sd.imp = IJ.getImage();
        ImageProcessor B_ip = sd.imp.getProcessor();
        sd.run(B_ip);
    }

    public void run(ImageProcessor B_ip) {
        // Film B wird uebergeben
        ImageStack stack_B = imp.getStack();

        int length = stack_B.getSize();
        int width  = B_ip.getWidth();
        int height = B_ip.getHeight();

        // ermoeglicht das Laden eines Bildes / Films
        Opener o = new Opener();
        OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");

        // Film A wird dazugeladen
        String dateiA = od_A.getFileName();
        if (dateiA == null) return; // Abbruch
        String pfadA = od_A.getDirectory();
        ImagePlus A = o.openImage(pfadA,dateiA);
        if (A == null) return; // Abbruch

        ImageProcessor A_ip = A.getProcessor();
        ImageStack stack_A  = A.getStack();

        if (A_ip.getWidth() != width || A_ip.getHeight() != height)
        {
            IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
            return;
        }

        // Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
        length = Math.min(length,stack_A.getSize());

        ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
        ImageStack stack_Erg  = Erg.getStack();

        // Dialog fuer Auswahl des Ueberlagerungsmodus
        GenericDialog gd = new GenericDialog("Überlagerung");
        gd.addChoice("Methode",choices,"");
        gd.showDialog();

        int methode = 0;
        String s = gd.getNextChoice();
        if (s.equals("Wischen")) methode = 1;
        if (s.equals("Weiche Blende")) methode = 2;
        if (s.equals("Ueberlagerung")) methode = 3;
        if (s.equals("Schiebblende")) methode = 4;
        if (s.equals("Chroma Key")) methode = 5;
        if (s.equals("Extra")) methode = 6;

        // Arrays fuer die einzelnen Bilder
        int[] pixels_B;
        int[] pixels_A;
        int[] pixels_Erg;

        // Schleife ueber alle Bilder
        for (int z=1; z<=length; z++)
        {
            pixels_B   = (int[]) stack_B.getPixels(z);
            pixels_A   = (int[]) stack_A.getPixels(z);
            pixels_Erg = (int[]) stack_Erg.getPixels(z);

            int pos = 0;
            for (int y=0; y<height; y++)
                for (int x=0; x<width; x++, pos++)
                {
                    int cA = pixels_A[pos];
                    int rA = (cA & 0xff0000) >> 16;
                    int gA = (cA & 0x00ff00) >> 8;
                    int bA = (cA & 0x0000ff);

                    int cB = pixels_B[pos];
                    int rB = (cB & 0xff0000) >> 16;
                    int gB = (cB & 0x00ff00) >> 8;
                    int bB = (cB & 0x0000ff);

                    if (methode == 1)
                    {
                        if (y+1 > (z-1)*(double)height/(length-1))
                            pixels_Erg[pos] = pixels_B[pos];
                        else
                            pixels_Erg[pos] = pixels_A[pos];
                    }

                    if (methode == 2)
                    {
                        /*       -V- diese hier wird von 0-255 verlaufen */
                        int a = (z-1) * 255 / (length-1);

                        int r = (a * rB + (255-a) * rA) / 255;
                        int g = (a * gB + (255-a) * gA) / 255;
                        int b = (a * bB + (255-a) * bA) / 255;

                        pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
                    }

                    if (methode == 3)
                    {
                        int r;
                        int g;
                        int b;

                        r = 255-((255-rB)*(255-rA) / 128);
                        g = 255-((255-gB)*(255-gA) / 128);
                        b = 255-((255-bB)*(255-bA) / 128);

                        if(rA <= 128) {
                            r = rA*rB/128;
                        }
                        if(gA <= 128) {
                            g = gA*gB/128;
                        }
                        if(bA <= 128) {
                            b = bA*bB/128;
                        }


                        pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
                    }

                    if (methode == 4)
                    {
                        int schub = (int) ((z - 1) * (double) width / (length - 1));

                        if(x + 1 > schub && pos - schub >= 0) {
                            pixels_Erg[pos] = pixels_B[pos - schub];
                        }
                        else if (pos - schub >= 0) {
                            pixels_Erg[pos] = pixels_A[pos - schub];
                        }

                    }

                    if (methode == 5) {

                        // Auf 0 - 1 limitieren
                        float r = rA / 255.0f;
                        float g = gA / 255.0f;
                        float b = bA / 255.0f;

                        // initializieren
                        int rn = rA;
                        int gn = gA;
                        int bn = bA;

                        // nimm den minimalen farbkanal
                        float max = Math.max(r, Math.max(g, b));
                        float min = Math.min(r, Math.min(g, b));


                        // HSV definieren
                        float value = max;

                        float satN = (max == 0) ? 0 : (max - min) / max;

                        float h;
                        if (max == min) {
                            h = 0;
                        } else if (max == r) {
                            h = 60 * ((g - b) / (max - min)) + (g < b ? 360 : 0);
                        } else if (max == g) {
                            h = 60 * ((b - r) / (max - min)) + 120;
                        } else {
                            h = 60 * ((r - g) / (max - min)) + 240;
                        }

                        // Farben welche genommen werden
                        if ((h >= 20 && h <= 60) && (satN >= 0.3 && satN <= 1.0) && (value >= 0.3 && value <= 1.0)) {
                            rn = rB;
                            gn = gB;
                            bn = bB;
                        }

                        pixels_Erg[pos] = 0xFF000000 + ((rn & 0xff) << 16) + ((gn & 0xff) << 8) + (bn & 0xff);
                    }

                    if (methode == 6)
                    {
                        int rU;
                        int gU;
                        int bU;

                        rU = 255-((255-rB)*(255-rA) / 128);
                        gU = 255-((255-gB)*(255-gA) / 128);
                        bU = 255-((255-bB)*(255-bA) / 128);

                        if(rA <= 128) {
                            rU = rA*rB/128;
                        }
                        if(gA <= 128) {
                            gU = gA*gB/128;
                        }
                        if(bA <= 128) {
                            bU = bA*bB/128;
                        }

                        // Auf 0 - 1 limitieren
                        float r = rA / 255.0f;
                        float g = gA / 255.0f;
                        float b = bA / 255.0f;

                        // initializieren
                        int rn = rA;
                        int gn = gA;
                        int bn = bA;

                        // nimm den minimalen farbkanal
                        float max = Math.max(r, Math.max(g, b));
                        float min = Math.min(r, Math.min(g, b));

                        // HSV definieren
                        float value = max;

                        float satN = (max == 0) ? 0 : (max - min) / max;

                        float h;
                        if (max == min) {
                            h = 0;
                        } else if (max == r) {
                            h = 60 * ((g - b) / (max - min)) + (g < b ? 360 : 0);
                        } else if (max == g) {
                            h = 60 * ((b - r) / (max - min)) + 120;
                        } else {
                            h = 60 * ((r - g) / (max - min)) + 240;
                        }

                        // Farben welche genommen werden
                        if ((h >= 20 && h <= 60) && (satN >= 0.3 && satN <= 1.0) && (value >= 0.3 && value <= 1.0)) {
                            rn = rB;
                            gn = gB;
                            bn = bB;
                        }

                            int a = (z-1) * 255 / (length-1);
                            rn = (a * rn + (255-a) * rU) / 255;
                            gn = (a * gn + (255-a) * gU) / 255;
                            bn = (a * bn + (255-a) * bU) / 255;

                        pixels_Erg[pos] = 0xFF000000 + ((rn & 0xff) << 16) + ((gn & 0xff) << 8) + (bn & 0xff);
                    }
                }
        }

        // neues Bild anzeigen
        Erg.show();
        Erg.updateAndDraw();

    }

}
