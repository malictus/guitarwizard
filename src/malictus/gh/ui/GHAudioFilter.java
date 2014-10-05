package malictus.gh.ui;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * GHAudioFilter
 * A file filter to display only audio files
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GHAudioFilter extends FileFilter {

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
        } else {
        	return false;
        }
    }

    // The description of this filter
    public String getDescription() {
        return "Audio Files (WAV, AIFF, MP3, OGG)";
    }
}