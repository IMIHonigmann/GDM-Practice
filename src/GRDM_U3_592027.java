import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
 Opens an image window and adds a panel below the image
 */

public class GRDM_U3_592027 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Invertiert", "Rot-Kanal", "Graustufen", "Binaer", "5 Stufen Grau", "27 Stufen Grau", "Sepia", "Vertikale Fehlerdiffusion", "9 geeignete Farben"};


    public static void main(String args[]) {

        IJ.open("C:/Users/Homam/Downloads/Bear.jpg");
        //IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

        GRDM_U3_592027 pw = new GRDM_U3_592027();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp == null)
            imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }

    public int clampChannel(int channel) {
        return Math.min(Math.max(channel, 0), 255);
    }

    public int clampChannel(int channel, int upperBorder) {
        return Math.min(Math.max(channel, 0), upperBorder);
    }

    public int changeToBinary(int channel, int threshhold) {
        if (channel > threshhold) {
            return 255;
        } else {
            return 0;
        }
    }

    public int changeToGreyShades(int channel, int shades) {
        int shadeStep = 255 / shades;
        for (int i = 0; i < shades; i++) {
            int shadeThreshhold = (int) ((i + 1) * shadeStep * 0.8);
            if (channel < shadeThreshhold) {
                return shadeThreshhold;
            }
        }

        return channel;
    }


    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();

        origPixels = ((int[]) ip.getPixels()).clone();
    }


    class CustomCanvas extends ImageCanvas {

        CustomCanvas(ImagePlus imp) {
            super(imp);
        }

    } // CustomCanvas inner class


    class CustomWindow extends ImageWindow implements ItemListener {

        private String method;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            //JPanel panel = new JPanel();
            Panel panel = new Panel();

            JComboBox cb = new JComboBox(items);
            panel.add(cb);
            cb.addItemListener(this);

            add(panel);
            pack();
        }

        public void itemStateChanged(ItemEvent evt) {

            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + item.toString());
                method = item.toString();
                changePixelValues(imp.getProcessor());
                imp.updateAndDraw();
            }

        }


        private void changePixelValues(ImageProcessor ip) {

            // Array zum Zurückschreiben der Pixelwerte
            int[] pixels = (int[]) ip.getPixels();

            if (method.equals("Original")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;

                        pixels[pos] = origPixels[pos];
                    }
                }
            }

            if (method.equals("Invertiert")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        int rn = 255 - r;
                        int gn = 255 - g;
                        int bn = 255 - b;

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Rot-Kanal")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int rn = clampChannel(r);
                        int gn = 0;
                        int bn = 0;

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Graustufen")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        int rn = colorMedian;
                        int gn = colorMedian;
                        int bn = colorMedian;

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Binaer")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        int rn = changeToBinary(colorMedian, 128); // doesnt work as i hoped it would
                        int gn = changeToBinary(colorMedian, 128);
                        int bn = changeToBinary(colorMedian, 128);

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("5 Stufen Grau")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        int rn = changeToGreyShades(colorMedian, 5); // doesnt work as i hoped it would
                        int gn = changeToGreyShades(colorMedian, 5);
                        int bn = changeToGreyShades(colorMedian, 5);

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("27 Stufen Grau")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        double Y = 0.299 * r + 0.587 * g + 0.114 * b;
                        double U = (b - Y) * 0.493;
                        double V = (r - Y) * 0.877;

                        double dB = 255 / 27;
                        int nb = (int) (Y / dB);
                        int newGrey = (int) (nb * dB + dB / 2);


                        int rn = Math.min(Math.max(newGrey, 0), 255);
                        int gn = Math.min(Math.max(newGrey, 0), 255);
                        int bn = Math.min(Math.max(newGrey, 0), 255);


                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Sepia")) {

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        int colorMedian = (r + g + b) / 3;

                        int depth = 20;
                        int intensity = 30;

                        int rn = colorMedian + depth * 2;
                        int gn = colorMedian + depth;
                        int bn = colorMedian - intensity;

                        rn = clampChannel(rn);
                        gn = clampChannel(gn);
                        bn = clampChannel(bn);


                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Vertikale Fehlerdiffusion")) {
                for (int y = 0; y < height - 1; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = pixels[pos];

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        int gray = (r + g + b) / 3;
                        int binary = gray > 128 ? 255 : 0;
                        int error = gray - binary;

                        pixels[pos] = (0xFF << 24) | (binary << 16) | (binary << 8) | binary;

                        int posBelow = (y + 1) * width + x;
                        int argbBelow = pixels[posBelow];

                        int rBelow = (argbBelow >> 16) & 0xff;
                        int gBelow = (argbBelow >> 8) & 0xff;
                        int bBelow = argbBelow & 0xff;

                        int grayBelow = (rBelow + gBelow + bBelow) / 3;
                        int newGrayBelow = clampChannel(grayBelow + error);

                        pixels[posBelow] = (0xFF << 24) | (newGrayBelow << 16) | (newGrayBelow << 8) | newGrayBelow;
                    }
                }
            }

            if (method.equals("9 geeignete Farben")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        Color[] Color = new Color[]{
                                new Color(101, 67, 33),
                                new Color(139, 69, 19),
                                new Color(184, 19, 170),
                                new Color(0, 0, 0),
                                new Color(255, 255, 255),
                                new Color(138, 186, 182),
                                new Color(105, 105, 105),
                                new Color(100, 80, 80),
                                new Color(45, 28, 25)
                        };

                        int closestColorIndex = -1;
                        int minDifference = Integer.MAX_VALUE;

                        for (int i = 0; i < Color.length; i++) {
                            int dr = r - Color[i].getRed();
                            int dg = g - Color[i].getGreen();
                            int db = b - Color[i].getBlue();
                            int diff = dr * dr + dg * dg + db * db;
                            if (diff < minDifference) {
                                minDifference = diff;
                                closestColorIndex = i;
                            }
                        }

                        int rn = Color[closestColorIndex].getRed();
                        int bn = Color[closestColorIndex].getGreen();
                        int gn = Color[closestColorIndex].getBlue();

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;

                    }
                }
            }
        }
    }
}