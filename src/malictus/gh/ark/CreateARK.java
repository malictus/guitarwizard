package malictus.gh.ark;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import javax.swing.*;
import malictus.gh.*;

/**
 * CreateARK
 * A class for creating an ARK file from its component files, with a progress bar GUI. When using this class,
 * be sure to check the value of getFinishedString() to see if the process completed successfully or had an error
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class CreateARK extends JDialog {

	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String status = "";		//used to update the status message in the progress bar
	private String finishedString = "";
	private int progressCounter = 0;

	Vector allFiles = new Vector();		//vectors of all files that will be packaged into an ARK file
	Vector waitFiles = new Vector();
	Vector waitLongerFiles = new Vector();
	Vector waitFileNames = new Vector();

	static final public String FINISHED_SUCCESSFULLY = "DONE";

	public CreateARK(JDialog parent) {
		super(parent);
		this.setTitle("Creating ARK File...");
        this.setSize(new java.awt.Dimension(370,88));
        GHUtils.centerWindow(this);
        waitFileNames.add("campaign.dtb");
        waitFileNames.add("guitars.dtb");
        waitFileNames.add("modes.dtb");
        waitFileNames.add("scoring.dtb");
        waitFileNames.add("songs.dtb");
        waitFileNames.add("store.dtb");
        waitFileNames.add("track_graphics.dtb");
        waitFileNames.add("default.dtb");
        waitFileNames.add("gh2.dtb");
        waitFileNames.add("locale.dtb");
        waitFileNames.add("uk_locale.dtb");
        waitFileNames.add("mc.dtb");
        waitFileNames.add("gh2_pal.dtb");
        waitFileNames.add("tips.dtb");

		initialize();
	}

	public String getFinishedString() {
		return finishedString;
	}

	private void initialize() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);

		Runnable q = new Runnable() {
            public void run() {
            	doARKCreation();
            }
        };

        CreateARKTask lTask = new CreateARKTask(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doARKCreation() {
		try {
        	//assume files are valid at this point
    		File arkFile = GuitarWizardMain.ARKFile;
    		File headerFile = GuitarWizardMain.HDRFile;
    		File expandFolder = GuitarWizardMain.TempDir;
    		//copy the entire existing header file to the output
    		File oldheader = new File(expandFolder.getPath() + File.separator + "MAIN.HDR");
    		if (!oldheader.exists()) {
    			finishedString = "ERROR: MAIN.HDR file must be present in expanded folder.";
    			return;
    		}
    		headerFile.delete();
    		headerFile.createNewFile();
    		//transfer old header file (parts of it to be written over later)
    		FileChannel srcChannel = new FileInputStream(oldheader).getChannel();
    	    FileChannel dstChannel = new FileOutputStream(headerFile).getChannel();
    	    long x = dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    	    if (x != srcChannel.size()) {
    	    	throw new Exception("Incorrect transfer of HDR file");
    	    }
    	    srcChannel.close();
    	    dstChannel.close();

    	    //create HDR information class
    	    HDR_Data headerData = new HDR_Data(headerFile);
    	    //recursively populate the file vector
    		status = "Populating file vector";
    		allFiles.clear();
    		getFilesFor(expandFolder);

    		//output data to ARK file
    		arkFile.delete();
    		arkFile.createNewFile();
    		FileChannel fos = new FileOutputStream(arkFile).getChannel();
    		prgProg.setMaximum(allFiles.size());
    		int total = allFiles.size();
    		int counter = allFiles.size() - 1;
    		//copy some files to a special vector that we'll wait until last to add
    		while (counter >= 0) {
    			File f = (File)allFiles.get(counter);
    			String s = f.getName();
    			String path = f.getPath();
    			path = getPathNameFor(path, s, expandFolder.getPath());
    			if (waitFileNames.contains(s)) {
    				allFiles.remove(f);
    				waitLongerFiles.add(f);
    			} else if (path.startsWith("songs")) {
    				if ( (!(s.endsWith("voc"))) && (!(s.endsWith("p50.vgs"))) && (!(s.endsWith("p65.vgs")))
    						&& (!(s.endsWith("p85.vgs"))) && (!(s.endsWith("p60.vgs")))
    						&& (!(s.endsWith("p75.vgs"))) && (!(s.endsWith("p90.vgs"))) ) {
    					allFiles.remove(f);
    					waitFiles.add(f);
    				}
    			}
    			counter = counter - 1;
    		}

    		counter = 0;
    		long pos = 0;
    		while (counter < allFiles.size()) {
    			status = "Adding file " + (counter + 1) + " of " + total;
    			progressCounter = counter;
    			File f = (File)allFiles.get(counter);
    			FileChannel fin = new FileInputStream(f).getChannel();
    			fos.position(pos);
	    	    pos = pos + fos.transferFrom(fin, pos, fin.size());
    	        fin.close();
    			counter = counter + 1;
    		}

    		int addon = counter;
    		counter = 0;
    		while (counter < waitFiles.size()) {
    			status = "Adding file " + (addon + counter + 1) + " of " + total;
    			progressCounter = addon + counter;
    			File f = (File)waitFiles.get(counter);
    			FileChannel fin = new FileInputStream(f).getChannel();
    			fos.position(pos);
	    	    pos = pos + fos.transferFrom(fin, pos, fin.size());
    	        fin.close();
    			counter = counter + 1;
    		}

    		addon = counter;
    		counter = 0;
    		while (counter < waitLongerFiles.size()) {
    			status = "Adding file " + (addon + counter + 1) + " of " + total;
    			progressCounter = addon + counter;
    			File f = (File)waitLongerFiles.get(counter);
    			FileChannel fin = new FileInputStream(f).getChannel();
    			fos.position(pos);
	    	    pos = pos + fos.transferFrom(fin, pos, fin.size());
    	        fin.close();
    			counter = counter + 1;
    		}

    		//cleanup
    		fos.close();
    		//now, overwrite HDR file contents where necessary
    		status = "Modifying HDR file";
    		headerData.setArkSize(arkFile.length());
    		counter = 0;
    		long offset = 0;
    		while (counter < allFiles.size()) {
    			File f = (File)allFiles.get(counter);
    			String name = f.getName();
    			String pathName = ((File)allFiles.get(counter)).getPath();
    			pathName = getPathNameFor(pathName, name, expandFolder.getPath());
    			headerData.overwriteSection3Entry(name, pathName, offset, f.length());
    			//increment offset
    			offset = offset + f.length();
    			counter = counter + 1;
    		}

    		counter = 0;
    		while (counter < waitFiles.size()) {
    			File f = (File)waitFiles.get(counter);
    			String name = f.getName();
    			String pathName = ((File)waitFiles.get(counter)).getPath();
    			pathName = getPathNameFor(pathName, name, expandFolder.getPath());
    			headerData.overwriteSection3Entry(name, pathName, offset, f.length());
    			//increment offset
    			offset = offset + f.length();
    			counter = counter + 1;
    		}

    		counter = 0;
    		while (counter < waitLongerFiles.size()) {
    			File f = (File)waitLongerFiles.get(counter);
    			String name = f.getName();
    			String pathName = ((File)waitLongerFiles.get(counter)).getPath();
    			pathName = getPathNameFor(pathName, name, expandFolder.getPath());
    			headerData.overwriteSection3Entry(name, pathName, offset, f.length());
    			//increment offset
    			offset = offset + f.length();
    			counter = counter + 1;
    		}

    		finishedString = FINISHED_SUCCESSFULLY;
    	} catch (Exception e) {
    		if (e.getMessage() == null) {
    			e.printStackTrace();
    			finishedString = "Error creating files.";
    		} else if (e.getMessage().equals("null")) {
    			finishedString = "Error creating files.";
    		} else {
    			finishedString = e.getMessage();
    		}
			return;
    	}
	}

	private String getPathNameFor(String pathName, String fileName, String expandFolderString) {
		//trim off file name itself
		pathName = pathName.substring(0, pathName.length() - fileName.length() - 1);
		//trim off beginning of dir name
		pathName = pathName.substring(expandFolderString.length() + 1);
		//change 'dotdot' back to '..'
		pathName = pathName.replace("dotdot", "..");
		//replace folder indicators
		pathName = pathName.replace("\\", "/");
		return pathName;
	}

	private void getFilesFor(File expandFolder) {
		File[] children = expandFolder.listFiles();
		int counter = 0;
		while (counter < children.length) {
			File child = children[counter];
			if (child.isFile()) {
				//don't include header file!
				if (!child.getName().toUpperCase().equals("MAIN.HDR")) {
					allFiles.add(child);
				}
			} else {
				//recurse
				getFilesFor(child);
			}
			counter = counter + 1;
		}
		return;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblProg = new JLabel();
			lblProg.setBounds(new java.awt.Rectangle(7,5,300,16));
			lblProg.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			lblProg.setText("");

			prgProg = new JProgressBar();
			prgProg.setMinimum(0);
			prgProg.setMaximum(100);
			prgProg.setValue(0);
			prgProg.setBounds(new java.awt.Rectangle(7,28,303,23));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblProg, null);
			jContentPane.add(prgProg, null);
		}
		return jContentPane;
	}

	private class CreateARKTask extends TimerTask {
        java.util.Timer myTimer = null;
        String currStatus = "";

        public CreateARKTask(java.util.Timer aTimer) {
            super();
            myTimer = aTimer;
        }

        public void run() {
            if (!finishedString.equals("")) {
            	theTimer.cancel();
            	setVisible(false);
            }
            if (!(currStatus.equals(status))) {
            	currStatus = status;
            	lblProg.setText(status);
            	prgProg.setValue(progressCounter);
            }
        }
	}
}
