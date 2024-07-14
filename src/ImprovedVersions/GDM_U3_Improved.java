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
public class GDM_U3_Improved implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Graustufen", "Binär", "5 Graustufen", "27 Graustufen"};


    public static void main(String args[]) {

        IJ.open("C:\\Users\\Homam\\Downloads\\Bear.jpg");
        //IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

        GDM_U3_Improved pw = new GDM_U3_Improved();
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

            if (method.equals("Rot-Kanal")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        //int g = (argb >>  8) & 0xff;
                        //int b =  argb        & 0xff;

                        int rn = r;
                        int gn = 0;
                        int bn = 0;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }

            if (method.equals("Graustufen")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int median = (r+g+b) / 3;

                        int rn = median;
                        int gn = median;
                        int bn = median;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }

            if (method.equals("Binär")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;


                        int median = (r+g+b) / 3;
                        if(median > 127) median = 255; else median = 0;

                        int rn = median;
                        int gn = median;
                        int bn = median;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }

            if (method.equals("5 Graustufen")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int median = (r+g+b) / 3;


                        int steps = 5;
                        int grey5 = median / (255 / steps) * (255 / (steps-1)) ;

                        int rn = grey5;
                        int gn = grey5;
                        int bn = grey5;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }

            if (method.equals("27 Graustufen")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int median = (r+g+b) / 3;


                        int steps = 27;
                        int grey27 = median / (255 / steps) * (255 / (steps-1)) ;

                        int rn = grey27;
                        int gn = grey27;
                        int bn = grey27;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }





        }


    } // CustomWindow inner class
} 