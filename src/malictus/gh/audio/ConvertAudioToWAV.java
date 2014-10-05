package malictus.gh.audio;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import javax.swing.*;
import javax.sound.*;
import javax.sound.sampled.*;

import malictus.gh.*;

/**
 * ConvertAudioToWAV
 * A class for converting any supported audio file to a standard WAV. When using this class,
 * be sure to check the value of getFinishedString() to see if the process completed successfully or had an error
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class ConvertAudioToWAV extends JDialog {

	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String finishedString = "";
	private File inFile;
	private File outFile;

	static final public String FINISHED_SUCCESSFULLY = "DONE";

	public ConvertAudioToWAV(JDialog parent, File oldFile, File newFile) {
		super(parent);
		inFile = oldFile;
		outFile = newFile;
		this.setTitle("Converting " + oldFile.getName());
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
            	doConvertToWAV();
            }
        };

        ConvertToWAVTask lTask = new ConvertToWAVTask(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doConvertToWAV() {
		try {
			//verify that it's a valid sound file
			AudioFileFormat afback = AudioSystem.getAudioFileFormat(inFile);
			AudioInputStream inFileAIS = AudioSystem.getAudioInputStream(inFile);
			AudioFormat newFormat = new AudioFormat(44100, 16, 2, true, false);
			AudioInputStream realAIS = null;
			realAIS = AudioSystem.getAudioInputStream (newFormat, inFileAIS);
		    AudioSystem.write(realAIS, AudioFileFormat.Type.WAVE, outFile);
		    inFileAIS.close();
			finishedString = FINISHED_SUCCESSFULLY;
    		return;
		} catch (Exception e) {
			if (e.getMessage() == null) {
    			e.printStackTrace();
    			finishedString = "Error converting audio.";
    		} else if (e.getMessage().equals("null")) {
    			finishedString = "Error converting audio.";
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
			prgProg.setIndeterminate(true);
			prgProg.setBounds(new java.awt.Rectangle(7,28,303,23));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblProg, null);
			jContentPane.add(prgProg, null);
		}
		return jContentPane;
	}

	private class ConvertToWAVTask extends TimerTask {
        java.util.Timer myTimer = null;

        public ConvertToWAVTask(java.util.Timer aTimer) {
            super();
            myTimer = aTimer;
        }

        public void run() {
            if (!finishedString.equals("")) {
            	theTimer.cancel();
            	setVisible(false);
            }
        }
	}
}
