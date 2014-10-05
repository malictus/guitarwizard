package malictus.gh.ui;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * GHMIDIFilter
 * A file filter to display only MIDI and (later) chart files
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GHMIDIFilter extends FileFilter {

    public boolean accept(File f) {
    	//Accept all directories and readable audio files
    	if (f.isDirectory()) {
            return true;
        }
        String s = f.getName();
        if (s.toUpperCase().endsWith("MIDI")) {
        	return true;
        } else if (s.toUpperCase().endsWith("MID")) {
        	return true;
        //} else if (s.toUpperCase().endsWith("CHART")) {
        //	return true;
        } else {
        	return false;
        }
    }

    // The description of this filter
    public String getDescription() {
    	return "Midi Files (MID, MIDI)";
        //return "Midi and Chart Files (MID, MIDI, CHART)";
    }
}