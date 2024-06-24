import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Opens an image window and adds a panel below the image
 */
public class GRDM_U5_592027 implements PlugIn {

    ImagePlus imp;
    private int[] origPixels;
    private int width;
    private int height;

    // List of filters/methods to apply
    String[] items = {"Original", "Verdunkeln", "Weichzeichnen (Box Blur)", "Sharpening", "Verst.Kanten",};

    public static void main(String args[]) {

        // Open an image
        IJ.open("C:/Users/Homam/Downloads/sail.jpg");

        // Create instance of the plugin and run it
        GRDM_U5_592027 pw = new GRDM_U5_592027();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp == null)
            imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }

        // Create a custom canvas to display the image
        CustomCanvas cc = new CustomCanvas(imp);

        // Store original pixel values of the image
        storePixelValues(imp.getProcessor());

        // Create a custom window to hold the image and panel
        new CustomWindow(imp, cc);
    }

    // Method to store original pixel values
    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();
        origPixels = ((int[]) ip.getPixels()).clone();
    }

    // CustomCanvas subclass to customize image display
    class CustomCanvas extends ImageCanvas {
        CustomCanvas(ImagePlus imp) {
            super(imp);
        }
    }

    // CustomWindow subclass to add a panel with options below the image
    class CustomWindow extends ImageWindow implements ItemListener {

        private String method; // Selected method

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel(); // Add control panel
        }

        // Add a panel with a ComboBox for choosing methods
        void addPanel() {
            Panel panel = new Panel();
            JComboBox cb = new JComboBox(items);
            panel.add(cb);
            cb.addItemListener(this);
            add(panel);
            pack();
        }

        // Handle changes in the ComboBox selection
        public void itemStateChanged(ItemEvent evt) {
            Object item = evt.getItem();
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + item.toString());
                method = item.toString();
                changePixelValues(imp.getProcessor());
                imp.updateAndDraw();
            }
        }

        // Method to apply selected filter/method to the image
        private void changePixelValues(ImageProcessor ip) {
            int[] pixels = (int[]) ip.getPixels(); // Get pixel array

            if (method.equals("Original")) {
                // Restore original pixel values
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        pixels[pos] = origPixels[pos];
                    }
                }
            }

            if (method.equals("Verdunkeln")) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        // RGB Werte durch 2 teilen
                        int rn = r / 2;
                        int gn = g / 2;
                        int bn = b / 2;

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Weichzeichnen (Box Blur)")) {
                // Durchschnittsfilter für Weichzeichnung
                double[][] coefficients = {
                        { 1.0/9.0, 1.0/9.0, 1.0/9.0 },
                        { 1.0/9.0, 1.0/9.0, 1.0/9.0 },
                        { 1.0/9.0, 1.0/9.0, 1.0/9.0 }
                };

            // Normalisierungsfaktor
                // Ist die Summe der Koeffizienten = 1?
                // Optional, benötigt performance

                /*
            double sumKoeff = 0.0;
            for (double[] col : coefficients) {
                for (double koeff : col) {
                    sumKoeff += koeff;
                }
            }
            double normalisierFaktor = 1.0 / sumKoeff;
            */
                double normalisierFaktor = 1.0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pos = y * width + x;

                    // sum startet bei 0
                    double sumR = 0.0, sumG = 0.0, sumB = 0.0;

                    // 3x3 Kernel Positionen checken
                    // | -1 | 0 | 1 |
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int nx = x + kx;
                            int ny = y + ky;

                            // Ignorier Positionen die Außerhalb der Bildgrenze liegen
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                int npos = ny * width + nx;
                                int nargb = origPixels[npos];

                                // Koeffizientenmatrix verwenden und aufsummieren
                                double koeff = coefficients[ky + 1][kx + 1];
                                sumR += koeff * ((nargb >> 16) & 0xff);
                                sumG += koeff * ((nargb >> 8) & 0xff);
                                sumB += koeff * (nargb & 0xff);
                            }
                        }
                    }

                    // Summen einsetzen
                    int rn = (int) (sumR * normalisierFaktor);
                    int gn = (int) (sumG * normalisierFaktor);
                    int bn = (int) (sumB * normalisierFaktor);

                    pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                }
            }
        }

            if (method.equals("Sharpening")) {
                // Sharpening filter
                // 8 * 0.11 = 0.88
                // 1-0.88 = 0.12
                // 0.12 * 10 = 1.2
                double[][] coefficients = {
                        { -(1.0 / 9.0), -(1.0 / 9.0), -(1.0 / 9.0) },
                        { -(1.0 / 9.0),      1.2,     -(1.0 / 9.0) },
                        { -(1.0 / 9.0), -(1.0 / 9.0), -(1.0 / 9.0) }
                };

                                // Hier!
                // validierung wichtiger als vorher
                double sumKoeff = 0.88;
                for (double[] row : coefficients) {
                    for (double koeff : row) {
                        sumKoeff += koeff;
                    }
                }

                double normalisierFaktor = 1.0 / sumKoeff;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {

                        int pos = y * width + x;
                        // sum started in 128
                        double sumR = 128.0, sumG = 128.0, sumB = 128.0;

                        // Kernel nochmal
                        for (int ky = -1; ky <= 1; ky++) {
                            for (int kx = -1; kx <= 1; kx++) {
                                int nx = x + kx;
                                int ny = y + ky;

                                // check nochmal obs drin is
                                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                    int npos = ny * width + nx;
                                    int nargb = origPixels[npos];
                                    double koeff = coefficients[kx + 1][ky + 1];
                                    sumR += koeff * ((nargb >> 16) & 0xff);
                                    sumG += koeff * ((nargb >> 8) & 0xff);
                                    sumB += koeff * (nargb & 0xff);
                                }
                            }
                        }

                        int rn = (int) (sumR * normalisierFaktor);
                        int gn = (int) (sumG * normalisierFaktor);
                        int bn = (int) (sumB * normalisierFaktor);

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }

            if (method.equals("Verst.Kanten")) {
                // edge enhancement
                double[][] coefficients = {
                        { -1.0 / 9, -1.0 / 9, -1.0 / 9 },
                        { -1.0 / 9, 17.0 / 9, -1.0 / 9 },
                        { -1.0 / 9, -1.0 / 9, -1.0 / 9 }
                };

                double sumKoeff = 0.0;
                for (double[] row : coefficients) {
                    for (double koeff : row) {
                        sumKoeff += koeff;
                    }
                }
                double normalisierFaktor = 1.0 / sumKoeff;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pos = y * width + x;

                        // sum startet bei 0
                        double sumR = 0.0, sumG = 0.0, sumB = 0.0;
                        for (int ky = -1; ky <= 1; ky++) {
                            for (int kx = -1; kx <= 1; kx++) {
                                int nx = x + kx;
                                int ny = y + ky;
                                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                    int npos = ny * width + nx;
                                    int nargb = origPixels[npos];
                                    double koeff = coefficients[kx + 1][ky + 1];
                                    sumR += koeff * ((nargb >> 16) & 0xff);
                                    sumG += koeff * ((nargb >> 8) & 0xff);
                                    sumB += koeff * (nargb & 0xff);
                                }
                            }
                        }

                        int rn = (int) (sumR * normalisierFaktor);
                        int gn = (int) (sumG * normalisierFaktor);
                        int bn = (int) (sumB * normalisierFaktor);

                        rn = Math.min(Math.max(rn, 0), 255);
                        gn = Math.min(Math.max(gn, 0), 255);
                        bn = Math.min(Math.max(bn, 0), 255);

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }
        }
    }
}