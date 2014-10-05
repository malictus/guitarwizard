package malictus.gh;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Vector;
import java.awt.*;
import javax.swing.*;
import java.util.*;

import malictus.gh.ark.HDR_Data;
import malictus.gh.ark.Section3Entry;

/**
 * GHUtils
 * A collection of simple utility methods for the other classes
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GHUtils {

	private GHUtils() {}

	/**
	 * Pull the file with this name from the ARK, and put it in the temp directory
	 */
	static public File putInTemp(String fileName, String dirName) throws Exception {
		File headerFile = GuitarWizardMain.HDRFile;
		File arkFile = GuitarWizardMain.ARKFile;
		String tempDir = GuitarWizardMain.TempDir + File.separator;
		HDR_Data headerData = new HDR_Data(headerFile);
		int counter = 0;
		Vector section3Entries = headerData.getSection3Entries();
		while (counter < section3Entries.size()) {
			Section3Entry s = (Section3Entry)section3Entries.get(counter);
			if ( (headerData.getNameFor(s.getFileID()).equals(fileName)) &&
					(headerData.getNameFor(s.getDirectoryID()).equals(dirName)) ) {
				String fullPath = tempDir + headerData.getNameFor(s.getFileID());
				File outfile = new File(fullPath);
				outfile.createNewFile();
				FileChannel fos = new FileOutputStream(outfile).getChannel();
				FileChannel inARK = new FileInputStream(arkFile).getChannel();
	    	    inARK.position(s.getFileOffset());
	    	    long x = fos.transferFrom(inARK, 0, s.getFileSize());
	    	    if (x != s.getFileSize()) {
	    	    	throw new Exception("File not transferred correctly");
	    	    }
				fos.close();
				inARK.close();
				return outfile;
			}
			counter = counter + 1;
		}
		throw new Exception("File not found");
	}

	/**
	 * Find out if a given file exists in the HDR/ARK
	 */
	static public boolean fileExists(String fileName, String dirName) throws Exception {
		File headerFile = GuitarWizardMain.HDRFile;
		HDR_Data headerData = new HDR_Data(headerFile);
		int counter = 0;
		Vector section3Entries = headerData.getSection3Entries();
		while (counter < section3Entries.size()) {
			Section3Entry s = (Section3Entry)section3Entries.get(counter);
			if ( (headerData.getNameFor(s.getFileID()).equals(fileName)) &&
					(headerData.getNameFor(s.getDirectoryID()).equals(dirName)) ) {
				return true;
			}
			counter = counter + 1;
		}
		return false;
	}

	/**
	 * Read beginning of an ARK file to see if it's all zeroes, which means it's invalid because it
	 * was transferred on a mac. Returns false if it isn't valid
	 */
	public static boolean arkIsValid() throws Exception {
		RandomAccessFile raf = new RandomAccessFile(GuitarWizardMain.ARKFile, "r");
		try {
			byte[] samples = new byte[100];
			raf.read(samples);
			int counter = 0;
			boolean valid = false;
			while (counter < samples.length) {
				byte test = samples[counter];
				if (test != 0) {
					valid = true;
				}
				counter = counter + 1;
			}
			raf.close();
			return valid;
		} catch (Exception err) {
			raf.close();
			throw err;
		}
	}

	/**
     * Center the specified window on the screen
     */
    public static void centerWindow(JFrame window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
          frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
          frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    /**
     * Center the specified window on the screen
     */
    public static void centerWindow(JDialog window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        if (frameSize.height > screenSize.height) {
          frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
          frameSize.width = screenSize.width;
        }
        window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

	/**
	 * Remove all files from the temp directory (assuming file refs were properly closed).
	 * Doesn't delete directory itself
	 */
    static public void cleanupTempDir() {
		//don't delete directory itself
		File temp = GuitarWizardMain.TempDir;
		if (temp == null) {
			return;
		}
		File[] f = temp.listFiles();
		int counter = 0;
		if (f == null) {
			return;
		}
		while (counter < f.length) {
			File fil = f[counter];
			if (fil.isDirectory()) {
				deleteDirectory(fil);
			} else {
				fil.delete();
			}
			counter = counter + 1;
		}
	}

	/**
	 * Recursive helper method for cleanupTempDir()
	 */
    static private void deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
	    }
	    path.delete();
	  }

	/**
	 * Reads and returns a null-terminated, 8-bit ANSI string from a file. Does not append the null character
	 * itself to the returned string.
	 */
	static public String readNullTerminatedString(RandomAccessFile raf) throws Exception {
		String x = "";
		byte[] w = new byte[1];
		byte next = raf.readByte();
		while (next != 0) {
			w[0] = next;
			x = x + new String(w);
			next = raf.readByte();
		}
		return x;
	}

	/**
	 * Writes a null-terminated, 8-bit ANSI string to a file. Input string should not include null character
	 */
	static public void writeNullTerminatedString(RandomAccessFile raf, String input) throws Exception {
		byte[] buf = input.getBytes();
		raf.write(buf);
		byte x = 0;
		raf.write(x);
	}

	/**
	 * Do necessary conversions to convert 32-bit ints read directly from a file
	 * into 32-bit, unsigned little-endian format.
	 * Since java doesn't use unsigned ints, value is stored as a long
	 */
	static public long readNumber(int inputNumber) {
		inputNumber = Integer.reverseBytes(inputNumber);
		long output = inputNumber & 0xffffffffL;
		return output;
	}

	/**
	 * Do necessary conversions to convert 32-bit ints read directly from a file
	 * into 32-bit, unsigned big-endian format.
	 * Since java doesn't use unsigned ints, value is stored as a long
	 */
	static public long readBigEndNumber(int inputNumber) {
		long output = inputNumber & 0xffffffffL;
		return output;
	}

	/**
	 * Do necessary conversions to write a 32-bit, unsigned, little-endian int based on a given long value
	 * Returns a byte array that represents the int
	 */
	static public byte[] writeNumber(long inputNumber) {
		byte[] array = new byte[4];
		array[3] = (byte)((inputNumber & 0xFF000000L) >> 24);
		array[2] = (byte)((inputNumber & 0x00FF0000L) >> 16);
		array[1] = (byte)((inputNumber & 0x0000FF00L) >> 8);
		array[0] = (byte)((inputNumber & 0x000000FFL));
		return array;
	}

	/**
	 * Do necessary conversion to convert 16-bit shorts read directly from a file
	 * into 16-bit, unsigned, little-endian format.
	 * Since java doesn't use unsigned shorts, value is stored as an int
	 */
	static public int read16BitNumber(short inputNumber) {
		inputNumber = Short.reverseBytes(inputNumber);
		int output = inputNumber & 0xffff;
		return output;
	}

	/**
	 * Do necessary conversions to write a 16-bit, unsigned, little-endian short based on a given int value
	 * Returns a byte array that represents the short
	 */
	static public byte[] write16BitNumber(int inputNumber) {
		byte[] array = new byte[2];
		array[1] = (byte)((inputNumber & 0x0000FF00L) >> 8);
		array[0] = (byte)((inputNumber & 0x000000FFL));
		return array;
	}

	static public byte convertToUnsignedByte(int input) {
		//this actually converts to a SIGNED byte whoops
		return (byte)(input & 0xFF);
	}

	/**
	 * Convert a given millisecond offset into mm:ss string format
	 */
	static public String convertOffsetToMinutesSeconds(int offset) {
        int seconds = (int)(offset / 1000);
        int minutes = seconds / 60;
        String strSeconds = new String();
        seconds = seconds - (minutes * 60);
        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }
        String strMinutes = "" + minutes;
        return (strMinutes + ":" + strSeconds);
	}

	static public String convertOffsetToMinutesSecondsMillis(int offset) {
        int millis = offset;
        int seconds = (int)(millis / 1000);
        int minutes = seconds / 60;
        String strMillis = new String();
        String strSeconds = new String();
        String strMinutes = new String();
        millis = millis - (seconds * 1000);
        seconds = seconds - (minutes * 60);
        if (millis < 10) {
            strMillis = "00" + millis;
        } else if (millis < 100) {
            strMillis = "0" + millis;
        } else {
            strMillis = "" + millis;
        }
        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }
        strMinutes = "" + minutes;
        return(strMinutes + ":" + strSeconds + ":" + strMillis);
    }

    static public int convertMinutesSecondsMillisToMilliseconds(String offset) throws NumberFormatException {
        boolean isOK = true;
        StringTokenizer tokenizer = new StringTokenizer(offset,":");
        String token = null;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { minutes = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }
        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { seconds = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }
        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { }    ///still ok, since we have mins and secs now
        try { milliseconds = Integer.parseInt(token); } catch (NumberFormatException e) { }

        if (!isOK) {
            throw new NumberFormatException();
        }
        return((minutes * 60000) + (seconds * 1000) + milliseconds);
    }

	/**
     * Given a string of the form mm:ss, returns the total milliseconds
     */
    static public int convertMinutesSecondsToMilliseconds(String offset) throws NumberFormatException {
        boolean isOK = true;
        StringTokenizer tokenizer = new StringTokenizer(offset, ":");
        String token = null;

        int first = -1;
        int second = -1;

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { first = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }

        try { token = tokenizer.nextToken(); } catch (NoSuchElementException e) { isOK = false; }
        try { second = Integer.parseInt(token); } catch (NumberFormatException e) { isOK = false; }

        if (!isOK) {
            throw new NumberFormatException();
        }
        return((first * 60000) + (second * 1000));
    }
}
