import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.util.Random;

//erste Uebung (elementare Bilderzeugung)

public class GRDM_U1_592027 implements PlugIn {

    final static String[] choices = {
            "USA Fahne",
            "Schwarzes Bild",
            "Gelbes Bild",
            "Belgische Fahne",
            "Schwarz/Weiss Verlauf",
            "Diagonaler Schwarz/Weiss Verlauf",
            "Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
            "USA Fahne",
            "Japanische Fahne",
            "test",
            "Tschechische Fahne (Manuell)",
            "Tschechische Fahne (Prozedual)"
    };

    private String choice;

    public static void main(String args[]) {
        ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
        ij.exitWhenQuitting(true);

        GRDM_U1_592027 imageGeneration = new GRDM_U1_592027();
        imageGeneration.run("");
    }

    public void run(String arg) {

        int width  = 1920;  // Breite
        int height = 1080;  // Hoehe

        // RGB-Bild erzeugen
        ImagePlus imagePlus = NewImage.createRGBImage("GRDM_U1_592027", width, height, 1, NewImage.FILL_BLACK);
        ImageProcessor ip = imagePlus.getProcessor();

        // Arrays fuer den Zugriff auf die Pixelwerte
        int[] pixels = (int[])ip.getPixels();

        dialog();

        ////////////////////////////////////////////////////////////////
        // Hier bitte Ihre Aenderungen / Erweiterungen

        switch (choice) {
            case "Schwarzes Bild":
                generateBlackImage(width, height, pixels);
                break;
            case "Gelbes Bild":
                generateYellowImage(width, height, pixels);
                break;
            case "Belgische Fahne":
                generateBelgiumFlag(width, height, pixels);
                break;
            case "Schwarz/Weiss Verlauf":
                generateBlackWhiteGradient(width, height, pixels);
                break;
            case "Diagonaler Schwarz/Weiss Verlauf":
                generateDiagonalBlackWhiteGradient(width, height, pixels);
                break;
            case "Lichtartiger Diagonalverlauf":
                generateDiagonalRoundBlackWhiteGradient(width, height, pixels);
                break;
            case "Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf":
                generateHorizontalBlackRedVerticalBlackBlue(width, height, pixels);
                break;
            case "USA Fahne":
                generateUSA(width, height, pixels);
                break;
            case "Tschechische Fahne (Manuell)":
                generateCzechManual(width, height, pixels);
                break;
            case "Tschechische Fahne (Prozedual)":
                generateCzechProcedural(width, height, pixels);
                break;
            case "test":
                generateTest(width, height, pixels);
                break;
            case "Japanische Fahne":
                generateJapan(width, height, pixels);
                break;
            default:
                // Handle the case where none of the choices match
                System.out.println("Invalid choice");
                break;
        }


        ////////////////////////////////////////////////////////////////////

        // neues Bild anzeigen
        imagePlus.show();
        imagePlus.updateAndDraw();
    }

    private void generateTest(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen


                // r = 255 * (x/width)
                int r = (int) Math.floor(0 *   (1-((float) x / width)));
                int g = (int) Math.floor(255 * (1-((float) x / width)));
                int b = (int) Math.floor(0 *   (1-((float) x / width)));

                // y

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateBlackImage(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte
            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = 0;
                int g = 0;
                int b = 0;

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateYellowImage(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte
            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = 255;
                int g = 255;
                int b = 0;

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateBelgiumFlag(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte
            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = 0;
                int g = 0;
                int b = 0;

                if(x <= width / 3) {
                    //
                }
                else if(x <= width / 3 * 2) {
                    r = 255;
                    g = 255;
                }
                else if(x <= width) {
                    r = 255;
                }

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateBlackWhiteGradient(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte

        float fullColor = 255;

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = (int) Math.floor(fullColor * ((float) x / width));
                int g = (int) Math.floor(fullColor * ((float) x / width));
                int b = (int) Math.floor(fullColor * ((float) x / width));


                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateDiagonalBlackWhiteGradient(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte

        float fullColor = 255;

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int xx = (width-x)/12;
                int yy = (height-y)/12;

                int r = xx+yy;
                int g = xx+yy;
                int b = xx+yy;


                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateDiagonalRoundBlackWhiteGradient(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte

        float fullColor = 255;

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = (int) Math.floor(fullColor * (1-(float) x / width) * (1-(float) y / height));
                int g = (int) Math.floor(fullColor * (1-(float) x / width) * (1-(float) y / height));
                int b = (int) Math.floor(fullColor * (1-(float) x / width) * (1-(float) y / height));


                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateHorizontalBlackRedVerticalBlackBlue(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte

        float fullColor = 255;

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = (int) Math.floor(fullColor * ((float) x / width));
                int g = 0;
                int b = (int) Math.floor(fullColor * ((float) y / height));


                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateUSA(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            int stripeLength = (int) Math.floor((float) height / 13);
            int stripeLengthTimesTwo = stripeLength * 2;
            int yMod = y % stripeLengthTimesTwo;
            float bluePartWidth = width * (1/2.5f);
            float bluePartHeight = stripeLength * 7;
            int padding = 15;




            // Schleife ueber die x-Werte
            for (int x=0; x<width; x++) {
                float randomNoise = (new Random().nextFloat() * 2 - 1) * 0.7f;

                int cubeDistance = (int) Math.floor(width * 0.05f);
                int cubeMod = x % cubeDistance;
                int cubeHeight = (int) Math.floor(height * 0.1f);
                int cubeModH = y % cubeHeight;

                int pos = y*width + x; // Arrayposition bestimmen

                int r = 255;
                int g = 255;
                int b = 255;



                if(yMod <= stripeLength) {
//                    int stripeGradient = (int) Math.floor(78 * ((float) y / stripeLength));
//                    r = 100 + stripeGradient;
                    r = 178;
                    g = 34;
                    b = 52;
                }
                else if(yMod <= stripeLengthTimesTwo) {
                    //
                }

                if(y < stripeLength * 7 && x < bluePartWidth) {
                    System.out.println(bluePartHeight);
                    r = 49;
                    g = 47;
                    b = 133;
                }

                if(cubeMod < cubeDistance / 2 && cubeModH < cubeHeight / 2
                        &&
                        y < bluePartHeight - padding && x < bluePartWidth - padding
                        &&
                        y > padding && x > padding
                ) {
                    r = 255;
                    g = 255;
                    b = 255;
                }


                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateCzechManual(int width, int height, int[] pixels) {

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen
                int r = 255;
                int g = 255;
                int b = 255;

                if(y > height/2) {
                    r = 215;
                    g = 20;
                    b = 26;
                }

                float yPadding = x*0.7f;

                if(y > yPadding && y < height-yPadding) {
                    r = 17;
                    g = 69;
                    b = 126;
                }

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateCzechProcedural(int width, int height, int[] pixels) {
        float paddingScale = 0.8f;

        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte

            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen
                int r = 255;
                int g = 255;
                int b = 255;

                if(y > height/2) {
                    r = 215;
                    g = 20;
                    b = 26;
                }

                float yPadding = 0.5f * height * ((float) x/height);
                float scaledPadding = yPadding * (1/paddingScale);

                if(y > scaledPadding && y < height-scaledPadding) {
                    r = 17;
                    g = 69;
                    b = 126;
                }

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    private void generateJapan(int width, int height, int[] pixels) {
        // Schleife ueber die y-Werte
        for (int y=0; y<height; y++) {
            // Schleife ueber die x-Werte
            for (int x=0; x<width; x++) {
                int pos = y*width + x; // Arrayposition bestimmen

                int r = 255;
                int g = 255;
                int b = 255;

                // Mittelpunkt & Radius
                int dx = width/2 - x;
                int dy = height/2 - y;
                int radius = 250;

                if (dx*dx + dy*dy <= radius*radius) {
                    r = 255;
                    g = 0;
                    b = 0;
                }
                else if (dx*dx + dy*dy <= Math.pow(1.01,2)*radius*radius) {
                    r = 0;
                    g = 0;
                    b = 0;
                }

                // Werte zurueckschreiben
                pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
            }
        }
    }

    public int remap(int value, int new_min, int new_max) {
        return new_min + (value) * (new_max - new_min);
    }


    private void dialog() {
        // Dialog fuer Auswahl der Bilderzeugung
        GenericDialog gd = new GenericDialog("Bildart");

        gd.addChoice("Bildtyp", choices, choices[0]);


        gd.showDialog();	// generiere Eingabefenster

        choice = gd.getNextChoice(); // Auswahl uebernehmen

        if (gd.wasCanceled())
            System.exit(0);
    }
}