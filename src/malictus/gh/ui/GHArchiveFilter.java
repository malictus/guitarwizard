package malictus.gh.ui;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * GHArchiveFilter
 * A file filter to display only Guitar Hero archive files
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GHArchiveFilter extends FileFilter {

    public boolean accept(File f) {
    	//Accept all directories and GH Archive files
    	if (f.isDirectory()) {
            return true;
        }
        String s = f.getName();
        if (s.toUpperCase().startsWith("SLUS_")) {
        	return true;
        } else if (s.toUpperCase().startsWith("SLES_")) {
        	return true;
        } else {
        	return false;
        }
    }

    // The description of this filter
    public String getDescription() {
        return "Guitar Hero Archive";
    }
}