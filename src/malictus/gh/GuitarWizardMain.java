package malictus.gh;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import malictus.gh.ui.*;

/**
 * GuitarWizardMain
 * Main method and important constants for Guitar Wizard
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GuitarWizardMain {

	final public static String VERSION = "0.22";
	final public static Font HEADERFONT = new Font("Arial", Font.PLAIN, 18);
	final public static Font REGFONT = new Font("Arial", Font.PLAIN, 12);

	public static GWMainWindow mainWindow;

	final public static char LINEBREAKCHAR = (char)10;
	final public static String LINEBREAKSTRING = LINEBREAKCHAR + "";

	public static File ARKFile;
	public static File HDRFile;
	public static File TempDir;
	public static File ELFFile;
	public static File SystemFile;

	public static File BLANK_VGS_FILE = new File("blankvgs.vgs");
	public static File CLICKTRACK_FILE = new File("clik.wav");

	public static int Version;
	public static final int VERSION_GH1 = 1;
	public static final int VERSION_GH2 = 2;
	public static final int VERSION_GH80 = 3;

	public static void main(String[] args) {
		new GuitarWizardMain();
	}

	public GuitarWizardMain() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			String version = System.getProperty("java.version");
			version = version.substring(0, 3);
			Float v = new Float(version);
			if (v.floatValue() < 1.5f) {
				JOptionPane.showMessageDialog(null, "This program requires Java 1.5 or higher. " +
						"\nYou are using Java version " + System.getProperty("java.version") +
						", which is incompatible. \nPlease download the latest Java version from www.java.com.", "Incompatible java version", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}

		} catch (Exception err) {
			System.exit(0);
		}
		mainWindow = new GWMainWindow();
    }

	public static void doShutdown() {
		GHUtils.cleanupTempDir();
		System.exit(0);
	}

}
