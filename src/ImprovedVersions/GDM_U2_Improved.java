package ImprovedVersions;

import ij.IJ;
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

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 Opens an image window and adds a panel below the image
 */
public class GDM_U2_Improved implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;


    public static void main(String args[]) {
        //new ImageJ();
        //IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
        IJ.open("C:\\Users\\Homam\\Downloads\\orchid.jpg");

        GDM_U2_Improved pw = new GDM_U2_Improved();
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


    class CustomWindow extends ImageWindow implements ChangeListener {

        private JSlider jSliderBrightness;
        private JSlider jSliderContrast;
        private JSlider jSliderSaturation;
        private JSlider jSliderHue;
        private double brightness = 0;
        private int contrast = 1;
        private double saturation = 1;
        private double hue = 0;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            //JPanel panel = new JPanel();
            Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", -128, 128, 0);
            jSliderContrast = makeTitledSilder("Kontrastwert", 0, 10, 3);
            jSliderSaturation = makeTitledSilder("Sättigungswert", 0, 5, 3);
            jSliderHue = makeTitledSilder("Farbe", 0, 360, 0);
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            panel.add(jSliderSaturation);
            panel.add(jSliderHue);

            add(panel);

            pack();
        }

        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {

            JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
            Dimension preferredSize = new Dimension(width, 50);
            slider.setPreferredSize(preferredSize);
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);
            slider.setMajorTickSpacing((maxVal - minVal)/10 );
            slider.setPaintTicks(true);
            slider.addChangeListener(this);

            return slider;
        }

        private void setSliderTitle(JSlider slider, String str) {
            TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
                    str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
                    new Font("Sans", Font.PLAIN, 11));
            slider.setBorder(tb);
        }

        public void stateChanged( ChangeEvent e ){
            JSlider slider = (JSlider)e.getSource();

            if (slider == jSliderBrightness) {
                brightness = slider.getValue();
                String str = "Helligkeit " + brightness;
                setSliderTitle(jSliderBrightness, str);
            }

            if (slider == jSliderContrast) {
                contrast = slider.getValue();
                String str = "Kontrastwert " + contrast;
                setSliderTitle(jSliderContrast, str);
            }

            if (slider == jSliderSaturation) {
                saturation = slider.getValue();
                String str = "Sättigungswert " + saturation;
                setSliderTitle(jSliderSaturation, str);
            }

            if (slider == jSliderHue) {
                hue = slider.getValue();
                String str = "Sättigung " + hue;
                hue *= Math.PI / 180;
                setSliderTitle(jSliderHue, str);
            }

            changePixelValues(imp.getProcessor());

            imp.updateAndDraw();
        }


        private void changePixelValues(ImageProcessor ip) {

            // Array fuer den Zugriff auf die Pixelwerte
            int[] pixels = (int[])ip.getPixels();

            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];  // Lesen der Originalwerte

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int U = (int) ((b - Y) * 0.493);
                    int V = (int) ((r - Y) * 0.877);

                    // anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
                    // die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren
                    Y = (int) Math.min(255, Math.max(0, Y + brightness));
//                    saturation *= Math.sqrt(Math.pow(U, 2) + Math.pow(V, 2));
                    U = (int) (U * saturation);
                    V = (int) (V * saturation);

                    U = (int) (U * Math.cos(hue) - Math.sin(hue) * V);
                    V = (int) (U * Math.sin(hue) + Math.cos(hue) * V);

                    r = (int) (Y + V/0.877);
                    b = (int) (Y + U/0.493);
                    g = (int) (1/0.587 * Y - 0.299/0.587*r - 0.114/0.587 * b);

                    int rn = Math.min(255, Math.max(0, contrast * (r - 128) + 128));
                    int gn = Math.min(255, Math.max(0, contrast * (g - 128) + 128));
                    int bn = Math.min(255, Math.max(0, contrast * (b - 128) + 128));

                    // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
        }

    } // CustomWindow inner class
} 