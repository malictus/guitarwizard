package malictus.gh.ui;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * GHAudioFilterAddVGS
 * A file filter to display only audio files incl. VGS
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GHAudioFilterAddVGS extends FileFilter {

    public boolean accept(File f) {
    	//Accept all directories and readable audio files
    	if (f.isDirectory()) {
            return true;
        }
        String s = f.getName();
        if (s.toUpperCase().endsWith("WAV")) {
        	return true;
        } else if (s.toUpperCase().endsWith("AIFF")) {
        	return true;
        } else if (s.toUpperCase().endsWith("MP3")) {
        	return true;
        } else if (s.toUpperCase().endsWith("OGG")) {
        	return true;
        //} else if (s.toUpperCase().endsWith("VGS")) {
        //	return true;
        } else {
        	return false;
        }
    }

    // The description of this filter
    public String getDescription() {
        return "Audio Files (WAV, AIFF, MP3, OGG)";
        //return "Audio Files (VGS, WAV, AIFF, MP3, OGG)";
    }
}