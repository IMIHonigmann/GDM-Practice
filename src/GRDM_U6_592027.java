import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;


public class GRDM_U6_592027 implements PlugInFilter {

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_RGB+NO_CHANGES;
        // kann RGB-Bilder und veraendert das Original nicht
    }

    public void run(ImageProcessor ip) {

        String[] dropdownmenu = {"Kopie", "Pixelwiederholung", "Pixelwiederholung (nach Folien)", "Bilinear"};

        GenericDialog gd = new GenericDialog("scale");
        gd.addChoice("Methode", dropdownmenu, dropdownmenu[0]);
        gd.addNumericField("Hoehe:",230,0);
        gd.addNumericField("Breite:",250,0);
        gd.addNumericField("Skalierungsfaktor:",2,0);
        gd.addCheckbox("Auflösung mit Skalierung verknüpfen", false);

        gd.showDialog();

        int scaleFactor = 1;
        if(gd.getNextBoolean()) {
            TextField scaleText = (TextField) gd.getNumericFields().get(2);
            scaleFactor = Integer.parseInt(scaleText.getText());
        }

        int height_n = (int) (gd.getNextNumber() * scaleFactor); // _n fuer das neue skalierte Bild
        int width_n =  (int) (gd.getNextNumber() * scaleFactor);

        int width  = ip.getWidth();  // Breite bestimmen
        int height = ip.getHeight(); // Hoehe bestimmen

        //height_n = height;
        //width_n  = width;

        ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild",
                width_n, height_n, 1, NewImage.FILL_BLACK);

        ImageProcessor ip_n = neu.getProcessor();


        int[] pix = (int[])ip.getPixels();
        int[] pix_n = (int[])ip_n.getPixels();
        String choice = gd.getNextChoice();

        if(choice.equals("Kopie")) {
            for (int y_n=0; y_n<height_n; y_n++) {
                for (int x_n=0; x_n<width_n; x_n++) {
                    int y = y_n;
                    int x = x_n;

                    if (y < height && x < width) {
                        int pos_n = y_n*width_n + x_n;
                        int pos  =  y  *width   + x;

                        pix_n[pos_n] = pix[pos];
                    }
                }
            }
        }
        if(choice.equals("Pixelwiederholung")) {
            for (int y_n=0; y_n<height_n; y_n++) {
                for (int x_n=0; x_n<width_n; x_n++) {
                    int y = Math.round(y_n / scaleFactor);
                    int x = Math.round(x_n / scaleFactor);

                    if (y < height && x < width) {
                        int pos_n = y_n*width_n + x_n;
                        int pos  =  y  *width   + x;

                        pix_n[pos_n] = pix[pos];
                    }
                }
            }
        }
        if (choice.equals("Pixelwiederholung (nach Folien)")) {
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    float srcX = (float) x_n / scaleFactor;
                    float srcY = (float) y_n / scaleFactor;

                    int x = x_n / scaleFactor;
                    int y = y_n / scaleFactor;

                    float dx = srcX - x;
                    float dy = srcY - y;

                    int pos = y * width + x;
                    int pos_n = y_n * width_n + x_n;
                    if (y < height-1 && x < width-1) {

                        int colorTL = pix[pos];
                        int colorTR = pix[pos + 1];
                        int colorBL = pix[pos + width];
                        int colorBR = pix[pos + width + 1];

                        int res = 0;
                        if(dx < 0.5 && dy < 0.5) res = colorTL;
                        if(dx >= 0.5 && dy < 0.5) res = colorTR;
                        if(dx < 0.5 && dy >= 0.5) res = colorBL;
                        if(dx >= 0.5 && dy >= 0.5) res = colorBR;


                        pix_n[pos_n] = res;
                    }
                    else {
                        pix_n[pos_n] = pix[pos];
                    }
                }
            }
        }
        if (choice.equals("Bilinear")) {
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    float srcX = (float) x_n / scaleFactor;
                    float srcY = (float) y_n / scaleFactor;

                    int x = x_n / scaleFactor;
                    int y = y_n / scaleFactor;

                    float dx = srcX - x;
                    float dy = srcY - y;

                    int pos = y * width + x;
                    int pos_n = y_n * width_n + x_n;

                    if (y < height-1 && x < width-1) {

                        int colorTL = pix[pos];
                        int colorTR = pix[pos + 1];
                        int colorBL = pix[pos + width];
                        int colorBR = pix[pos + width + 1];

                        int[] rgbTL = getRGB(colorTL);
                        int[] rgbTR = getRGB(colorTR);
                        int[] rgbBL = getRGB(colorBL);
                        int[] rgbBR = getRGB(colorBR);

                        int rn = (int)(
                                rgbTL[0] * (1 - dx) * (1 - dy) +
                                        rgbTR[0] * dx * (1 - dy) +
                                        rgbBL[0] * (1 - dx) * dy +
                                        rgbBR[0] * dx * dy
                        );
                        int gn = (int)(
                                rgbTL[1] * (1 - dx) * (1 - dy) +
                                        rgbTR[1] * dx * (1 - dy) +
                                        rgbBL[1] * (1 - dx) * dy +
                                        rgbBR[1] * dx * dy
                        );
                        int bn = (int)(
                                rgbTL[2] * (1 - dx) * (1 - dy) +
                                        rgbTR[2] * dx * (1 - dy) +
                                        rgbBL[2] * (1 - dx) * dy +
                                        rgbBR[2] * dx * dy
                        );

                        rn = Math.min(255, Math.max(0, rn));
                        gn = Math.min(255, Math.max(0, gn));
                        bn = Math.min(255, Math.max(0, bn));

                        int res = 0xFF000000 | (rn << 16) | (gn << 8) | bn;

                        pix_n[pos_n] = res;
                    }
                    else {
                        pix_n[pos_n] = pix[pos];
                    }
                }
            }
        }

        // neues Bild anzeigen
        neu.show();
        neu.updateAndDraw();
    }

    int[] getRGB(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;

        return new int[]{r, g, b};
    }

    void showAbout() {
        IJ.showMessage("");
    }

    public static void main(String[] args) {
        ImagePlus imp = IJ.openImage("C:/Users/Homam/Downloads/component.jpg");
        imp.show();

        GRDM_U6_592027 scaler = new GRDM_U6_592027();
        scaler.setup("", imp);
        scaler.run(imp.getProcessor());
    }
}
