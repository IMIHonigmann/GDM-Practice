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
public class GRDM_U2_592027 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;


    public static void main(String args[]) {
        //new ImageJ();
        //IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
        IJ.open("C:\\Users\\Homam\\Downloads\\orchid.jpg");

        GRDM_U2_592027 pw = new GRDM_U2_592027();
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
        private double brightness = 1;
        private double contrast = 1;
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
            jSliderBrightness = makeTitledSilder("Helligkeit", -200, 200, 0);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 100, 10);
            jSliderSaturation = makeTitledSilder("Saettigung", 0, 50, 10);
            jSliderHue = makeTitledSilder("Hue", 0, 360, 180);
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
                contrast = slider.getValue()*0.1;
                String str = "Kontrast " + Math.round(contrast);

                setSliderTitle(jSliderContrast, str);
            }

            if (slider == jSliderSaturation) {
                saturation = slider.getValue()*0.1;
                String str = "Saturation " + Math.round(saturation);

                setSliderTitle(jSliderSaturation, str);
            }

            if (slider == jSliderHue) {
                hue = slider.getValue();
                String str = "Hue " + hue;

                setSliderTitle(jSliderHue, str);
            }

            changePixelValues(imp.getProcessor());

            imp.updateAndDraw();
        }


        private void changePixelValues(ImageProcessor ip) {

            // Array fuer den Zugriff auf die Pixelwerte
            int[] pixels = (int[])ip.getPixels();

            for (int y=0; y<height; y++) {
                for (int x = 0; x < width; x++) {
                    int pos = y * width + x;

                    int argb = origPixels[pos];  // Lesen der Originalwerte

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int U = (int) ((b - Y) * 0.493);
                    int V = (int) ((r - Y) * 0.877);

                    // Brightness
                    Y = (int) (Y + brightness);

                    // Contrast
                    int DY = Y - 128;
                    Y = (int) (DY * contrast + 128);
                    U = (int) (U * contrast);
                    V = (int) (V * contrast);

                    // Saturation
                    U = (int) (U * saturation);
                    V = (int) (V * saturation);

                    // Hue
                    double transformedU = U * Math.cos(hue * Math.PI / 180) - V * Math.sin(hue * Math.PI / 180);
                    double transformedV = U * Math.sin(hue * Math.PI / 180) + V * Math.cos(hue * Math.PI / 180);

                    int rFinal = (int) (Y + transformedV / 0.877);
                    int bFinal = (int) (Y + transformedU / 0.493);
                    int gFinal = (int) ((Y - 0.299 * rFinal - 0.114 * bFinal) / 0.587);

                    // Clamping the values to be within 0-255
                    rFinal = Math.max(Math.min(255, rFinal), 0);
                    gFinal = Math.max(Math.min(255, gFinal), 0);
                    bFinal = Math.max(Math.min(255, bFinal), 0);

                    pixels[pos] = (0xFF << 24) | (rFinal << 16) | (gFinal << 8) | bFinal;
                }
            }
        }

    } // CustomWindow inner class
} 