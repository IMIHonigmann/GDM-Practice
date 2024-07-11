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

        String[] dropdownmenue = {"Bilinear", "Pixelwiederholung", "Kopie"};

        GenericDialog gd = new GenericDialog("scale");
        gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
        gd.addNumericField("Hoehe:",230,0);
        gd.addNumericField("Breite:",250,0);
        gd.addNumericField("Skalierungsfaktor:",2,0);
        gd.addCheckbox("Auflösung mit Skalierung verknüpfen", true);

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
        if(choice.equals("Bilinear")) {
            for (int y_n=0; y_n<height_n; y_n++) {
                for (int x_n=0; x_n<width_n; x_n++) {
                    int y = y_n;
                    int x = x_n;

                    if (y < height && x < width) {
                        int pos_n = y_n*width_n + x_n;
                        int pos  =  y  *width   + x;

                        pix_n[pos_n * scaleFactor] = pix[pos];
                    }
                }
            }
//            pix_n[1] = 0xFFFF0000;

            pix_n[1] = 0xFFFFFFFF;
            pix_n[width_n] = 0xFFFFFFFF;
            pix_n[width_n+2] = 0xFFF333;
            pix_n[2*width_n+1] = 0xFFFFF;

            int rn = (int) (getRGB(pix_n[0])[0]*(1-0.5)*(1-0.5) + getRGB(pix_n[2])[0]*0.5*(1-0.5) + getRGB(pix_n[2*width+0])[0]*(1-0.5)*0.5 + getRGB(pix_n[2*width+2])[0]*0.5*0.5);
            int gn = (int) (getRGB(pix_n[0])[1]*(1-0.5)*(1-0.5) + getRGB(pix_n[2])[1]*0.5*(1-0.5) + getRGB(pix_n[2*width+0])[1]*(1-0.5)*0.5 + getRGB(pix_n[2*width+2])[1]*0.5*0.5);
            int bn = (int) (getRGB(pix_n[0])[2]*(1-0.5)*(1-0.5) + getRGB(pix_n[2])[2]*0.5*(1-0.5) + getRGB(pix_n[2*width+0])[2]*(1-0.5)*0.5 + getRGB(pix_n[2*width+2])[2]*0.5*0.5);

            rn = Math.min(255, Math.max(0, rn));
            gn = Math.min(255, Math.max(0, gn));
            bn = Math.min(255, Math.max(0, bn));

            pix_n[width_n+1] = 0xFF000000 | (rn << 16) | (gn << 8) | bn;
//            pix_n[width_n+1] = (int) (pix[0]*(1-0.5)*(1-0.5) + pix[2]*0.5*(1-0.5) + pix[2*width+0]*(1-0.5)*0.5 + pix[2*width+2]*0.5*0.5);

            for(int ky=0; ky < 3; ky++) {
                for(int kx=0; kx < 3; kx++) {
                    int k_pos = ky*width+kx;
                    int k_argb = pix_n[k_pos];
                    if(k_argb != 0xFF000000) continue;
                    // sonst gehe formel durch für den pixel
                }
            }
        }


        // neues Bild anzeigen
        neu.show();
        neu.updateAndDraw();
    }

    int bilInterpolate(int[] pix, int width, int colorChannel) {
        return (int) (pix[0]*(1-0.5)*(1-0.5) + pix[2]*0.5*(1-0.5) + pix[width]*(1-0.5)*0.5 + pix[width+2]*0.5*0.5);
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
