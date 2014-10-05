package malictus.gh.ark;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import javax.swing.*;
import malictus.gh.*;

/**
 * ReinsertFile
 * A class for replacing an existing file in an ARK with a new one, with a progress bar GUI. When using this class,
 * be sure to check the value of getFinishedString() to see if the process completed successfully or had an error
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class ReinsertFile extends JDialog {

	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String status = "";		//used to update the status message in the progress bar
	private String finishedString = "";
	private File theFile;
	private String dir;

	static final public String FINISHED_SUCCESSFULLY = "DONE";

	public ReinsertFile(JDialog parent, File newFile, String newDir) {
		super(parent);
		theFile = newFile;
		dir = newDir;
		this.setTitle("Inserting " + newFile.getName() + "...");
        this.setSize(new java.awt.Dimension(370,88));
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
            	doInsertFile();
            }
        };

        InsertFileTask lTask = new InsertFileTask(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doInsertFile() {
		RandomAccessFile raf = null;
		try {
			//assume files are valid at this point
			File arkFile = GuitarWizardMain.ARKFile;
			File headerFile = GuitarWizardMain.HDRFile;
        	//create HDR information class
    		status = "Preparing to insert file";
    	    HDR_Data headerData = new HDR_Data(headerFile);
    	    //find the file location of the file we're going to write over
    	    int counter = 0;
    		Vector section3Entries = headerData.getSection3Entries();
    		boolean foundit = false;
    		Section3Entry s = null;
    		while ( (counter < section3Entries.size()) && (foundit == false) ) {
    			s = (Section3Entry)section3Entries.get(counter);
    			if ( (headerData.getNameFor(s.getFileID()).equals(theFile.getName())) &&
    					(headerData.getNameFor(s.getDirectoryID()).equals(dir)) ) {
    				foundit = true;
    			}
    			counter = counter + 1;
    		}
    		if (!foundit) {
    			throw new Exception("File not found");
    		}
			raf = new RandomAccessFile(arkFile, "rw");
    		raf.seek(s.getFileOffset());
    		long diff = theFile.length() - s.getFileSize();
    		if (diff == 0) {
    			//we got lucky; new file is same length as old one!
    			RandomAccessFile rafNew = new RandomAccessFile(theFile, "r");
    			byte[] buf = new byte[65536];
    			long curpos = 0;
    			while ((curpos + 65536) < rafNew.length()) {
    				int x = rafNew.read(buf);
    				if (x != buf.length) {
    					throw new Exception("Read error 3 while inserting file");
    				}
    				raf.write(buf);
    				curpos = rafNew.getFilePointer();
    			}
    			//finish up any remaining bytes
    			if (rafNew.length() != curpos) {
					buf = new byte[(int)(rafNew.length() - curpos)];
					int x = rafNew.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error 4 while inserting file");
					}
    				raf.write(buf);
    			}
    			//done with writing to ARK file
    			rafNew.close();
    			raf.close();
    			//no need to rewrite header file
    			prgProg.setValue(2);
    			prgProg.setMaximum(2);
    			finishedString = FINISHED_SUCCESSFULLY;
    			return;
    		} else {
    			status = "Moving data";
    			//move the rest of the file up
    			raf.seek(s.getFileOffset() + s.getFileSize());
    			long curpos = raf.getFilePointer();
    			int amount = (int) ((float)((raf.length() - curpos) / 65546f)) + 4;;
    			prgProg.setMaximum(amount);
    			prgProg.setValue(0);
    			byte[] buf = new byte[65536];
    			while ((curpos + 65536) < raf.length()) {
    				int x = raf.read(buf);
    				if (x != buf.length) {
    					throw new Exception("Read error while inserting file");
    				}
    				raf.seek(curpos - s.getFileSize());
    				raf.write(buf);
    				raf.seek(curpos + 65536);
    				curpos = raf.getFilePointer();
    				prgProg.setValue(prgProg.getValue() + 1);
    			}
    			//finish up any remaining bytes
    			if (raf.length() != curpos) {
					buf = new byte[(int)(raf.length() - curpos)];
					int x = raf.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error 2 while inserting file");
					}
					raf.seek(curpos - s.getFileSize());
    				raf.write(buf);
    			}
    			//remove anything after this
    			long startofnewfile = raf.getFilePointer();
    			raf.setLength(startofnewfile);
    			prgProg.setValue(prgProg.getValue() + 1);
    			status = "Inserting file";
    			//insert current file at end
    			RandomAccessFile rafNew = new RandomAccessFile(theFile, "r");
    			buf = new byte[65536];
    			curpos = 0;
    			while ((curpos + 65536) < rafNew.length()) {
    				int x = rafNew.read(buf);
    				if (x != buf.length) {
    					throw new Exception("Read error 3 while inserting file");
    				}
    				raf.write(buf);
    				curpos = rafNew.getFilePointer();
    			}
    			//finish up any remaining bytes
    			if (rafNew.length() != curpos) {
					buf = new byte[(int)(rafNew.length() - curpos)];
					int x = rafNew.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error 4 while inserting file");
					}
    				raf.write(buf);
    			}
    			//done with writing to ARK file
    			rafNew.close();
    			raf.close();
    			prgProg.setValue(prgProg.getValue() + 1);
    			status = "Updating HDR";
    			//tell HDR file that things have changed; first overall ARK size
    			headerData.setArkSize(arkFile.length());
    			//now section 3 entry for all objects after this one, and current file
    			counter = 0;
    			long offs = s.getFileOffset();
    			long size = s.getFileSize();
    			while (counter < section3Entries.size()) {
    				Section3Entry s3e = (Section3Entry)section3Entries.get(counter);
    				if (s3e.getFileOffset() > offs) {
    					s3e.setFileOffset(s3e.getFileOffset() - size);
    					headerData.overwriteSection3Entry(s3e);
    				} else if (s3e.getFileOffset() == offs) {
    					s3e.setFileSize(theFile.length());
    					s3e.setFileOffset(startofnewfile);
    					headerData.overwriteSection3Entry(s3e);
    				}
    				counter = counter + 1;
    			}
    			finishedString = FINISHED_SUCCESSFULLY;
    			return;
    		}
		} catch (Exception e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
			if (e == null) {
				finishedString = "Error creating files.";
			} else if (e.getMessage() == null) {
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

	private class InsertFileTask extends TimerTask {
        java.util.Timer myTimer = null;
        String currStatus = "";

        public InsertFileTask(java.util.Timer aTimer) {
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
            }
        }
	}
}
