package malictus.gh.ark;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import javax.swing.*;
import malictus.gh.*;
import malictus.gh.ui.*;

/**
 * ExpandARK
 * A class for expanding an ARK file into its component files. When using this class,
 * be sure to check the value of getFinishedString() to see if the process completed successfully, had an error, or
 * was canceled.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class ExpandARK extends JDialog {

	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;
	private JButton btnCancel = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String status = "";
	private String finishedString = "";
	private int progressCounter = 0;
	private boolean canceled = false;

	static final public String FINISHED_SUCCESSFULLY = "DONE";
	static final public String WAS_CANCELED = "WAS_CANCELED";

	public ExpandARK(JDialog parent) {
		super(parent);
		this.setTitle("Expanding ARK File...");
        this.setSize(new java.awt.Dimension(412,88));
        GHUtils.centerWindow(this);
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
            	doARKExpansion();
            }
        };
        ExpandARKTask lTask = new ExpandARKTask(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doARKExpansion() {
		try {
        	//we'll assume files are actually there, since they should have been checked before this step
    		//needed for later
    		String outputFolderDirPath = GuitarWizardMain.TempDir.getPath();
    		if ( (!outputFolderDirPath.endsWith(File.separator)) ) {
    			outputFolderDirPath = outputFolderDirPath + File.separator;
    		}

    		File headerFile = GuitarWizardMain.HDRFile;
    		File arkFile = GuitarWizardMain.ARKFile;

    		//verify expansion folder location is empty
    		File[] outputFiles = GuitarWizardMain.TempDir.listFiles();
    		if (outputFiles.length > 0) {
    			finishedString = "Expansion folder must be empty.";
    			return;
    		}

    		//copy the entire header file to the output, since we'll need it to rebuild later
    		status = "Copying HDR file";
    		File fil = new File(outputFolderDirPath + "MAIN.HDR");
			fil.createNewFile();
    		FileChannel srcChannel = new FileInputStream(headerFile).getChannel();
    	    FileChannel dstChannel = new FileOutputStream(fil).getChannel();
    	    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    	    srcChannel.close();
    	    dstChannel.close();

    	    if (canceled) {
				finishedString = WAS_CANCELED;
    	    	return;
			}

    		//read the HDR file into a HDR_Data object, so we only have to read it once
    	    status = "Reading HDR data";
    	    HDR_Data headerData = new HDR_Data(headerFile);
    		int counter = 0;
    		Vector section3Entries = headerData.getSection3Entries();
    		prgProg.setMaximum(section3Entries.size() + 1);
    		FileChannel inARK = new FileInputStream(arkFile).getChannel();
    		while (counter < section3Entries.size()) {
    			if (canceled) {
    				inARK.close();
    				finishedString = WAS_CANCELED;
    				return;
    			}
    			status = "Creating file " + (counter + 1) + " of " + section3Entries.size();
    			progressCounter = counter;

    			//read a single file entry
    			Section3Entry s = (Section3Entry)section3Entries.get(counter);
    			//parse directory string and create directories if necessary
    			String[] folders = headerData.getNameFor(s.getDirectoryID()).split("/");
    			int innercounter = 0;
    			String fullPath = outputFolderDirPath;
    			while (innercounter < folders.length) {
    				//apply substitution for folders named '..'
    				if (folders[innercounter].equals("..")) {
    					folders[innercounter] = "dotdot";
    				}
    				fullPath = fullPath + folders[innercounter] + File.separator;
    				innercounter = innercounter + 1;
    			}

    			File f = new File(fullPath);
				if (!(f.exists())) {
					f.mkdirs();
				}

				//create files
				fullPath = fullPath + headerData.getNameFor(s.getFileID());
				File outfile = new File(fullPath);
				outfile.createNewFile();

	    	    FileChannel fos = new FileOutputStream(outfile).getChannel();
	    	    inARK.position(s.getFileOffset());
	    	    fos.transferFrom(inARK, 0, s.getFileSize());
				fos.close();
    			counter = counter + 1;
    		}
    		inARK.close();
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

	private void doCancel() {
		canceled = true;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblProg = new JLabel();
			lblProg.setBounds(new java.awt.Rectangle(7,5,300,16));
			lblProg.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			lblProg.setText("");
			btnCancel = new JButton();
			btnCancel.setBounds(new java.awt.Rectangle(320,28,75,22));
			btnCancel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doCancel();
				}
			});
			prgProg = new JProgressBar();
			prgProg.setMinimum(0);
			prgProg.setMaximum(1000);
			prgProg.setValue(0);
			prgProg.setBounds(new java.awt.Rectangle(7,28,303,23));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblProg, null);
			jContentPane.add(btnCancel, null);
			jContentPane.add(prgProg, null);
		}
		return jContentPane;
	}

	private class ExpandARKTask extends TimerTask {
        java.util.Timer myTimer = null;
        String currStatus = "";

        public ExpandARKTask(java.util.Timer aTimer) {
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
