package ImprovedVersions;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
 Opens an image window and adds a panel below the image
 */
public class GDM_U5_Improved implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Filter 1"};


    public static void main(String args[]) {

        IJ.open("C:/Users/Homam/Downloads/sail.jpg");
        //IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

        GDM_U5_Improved pw = new GDM_U5_Improved();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null)
            imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();

        origPixels = ((int []) ip.getPixels()).clone();
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
            int[] pixels = (int[])ip.getPixels();

            if (method.equals("Original")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;

                        pixels[pos] = origPixels[pos];
                    }
                }
            }

            int kSize = 3;
            if (method.equals("Filter 1")) {
                double[][] filter = {
                        {1.0 / 9, 1.0 / 9, 1.0 / 9},
                        {1.0 / 9, 1.0 / 9, 1.0 / 9},
                        {1.0 / 9, 1.0 / 9, 1.0 / 9}
                };
                int[] origPixelsCopy = origPixels.clone();

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double r = 0, g = 0, b = 0;

                        for (int ky = -1; ky < kSize - 1; ky++) {
                            for (int kx = -1; kx < kSize - 1; kx++) {
                                int ix = x + kx;
                                int iy = y + ky;

                                // Check for valid coordinates
                                if (ix >= 0 && ix < width && iy >= 0 && iy < height) {
                                    int kpos = iy * width + ix;
                                    int pixel = origPixelsCopy[kpos];

                                    r += ((pixel >> 16) & 0xff) * filter[kx + 1][ky + 1];
                                    g += ((pixel >> 8) & 0xff) * filter[kx + 1][ky + 1];
                                    b += (pixel & 0xff) * filter[kx + 1][ky + 1];
                                }
                            }
                        }

                        // Update the color values of the current pixel
                        int rn = clamp((int) r);
                        int gn = clamp((int) g);
                        int bn = clamp((int) b);
                        pixels[y * width + x] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }




        }
            private int clamp(int value) {
                return Math.min(255, Math.max(0, value));
            }


    } // CustomWindow inner class
} 