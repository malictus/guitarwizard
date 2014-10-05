package malictus.gh.ui.editsong;

import javax.swing.*;
import javax.sound.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.util.*;
import javax.swing.UIManager;
import java.io.*;
import java.nio.channels.FileChannel;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;

import malictus.gh.ark.ReinsertFile;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.midi.*;
import malictus.gh.ui.editsettings.EditSettingsOne;

/**
 * EditSongFour
 * Step 4 in edit song
 * Choose a new midi or chart file to overwrite the current one
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongFour extends JDialog {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	protected JButton btnNext = null;
	protected JRadioButton radNothing = null;
	protected JRadioButton radDoSwitch = null;
	protected JTextField txtfNewMIDIPath = null;
	protected JButton btnSelect = null;

	GWMainWindow theParent;
	EditSongOne esong1;

	JFileChooser jfc = new JFileChooser();

	public EditSongFour(EditSongOne editsongone, GWMainWindow parent) {
		super(parent);
		theParent = parent;
		esong1 = editsongone;
		this.setSize(new Dimension(581, 208));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doNext() {
		if (!this.radNothing.isSelected()) {
			//verify, convert, and move MIDI/chart file
			//first, verify
			File origMidFile = new File(this.txtfNewMIDIPath.getText());
			if (!origMidFile.exists() || !origMidFile.isFile()) {
				JOptionPane.showMessageDialog(this, "Invalid MIDI/chart file selected.", "Invalid MIDI/chart file selected.", JOptionPane.ERROR_MESSAGE);
	        	return;
			}
			//create mid file
			String node = "";
			String midFileReal = "";
			String midFolder = "";
			try {
				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
					node = esong1.selectedSongID + "/midi_file";
				} else {
					node = esong1.selectedSongID + "/song/midi_file";
				}
				String midFileString = esong1.songs.readString(node);
				midFolder = midFileString.substring(0, midFileString.lastIndexOf("/"));
				midFileReal = midFileString.substring(midFileString.lastIndexOf("/") + 1);
			} catch (Exception err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File midFile = null;
			try {
				midFile = new File(GuitarWizardMain.TempDir + File.separator + midFileReal);
				midFile.createNewFile();
				midFile.deleteOnExit();
			} catch (Exception err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				//copy contents of MID file
				FileChannel srcChannel = new FileInputStream(origMidFile).getChannel();
	    	    FileChannel dstChannel = new FileOutputStream(midFile).getChannel();
	    	    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	    	    srcChannel.close();
	    	    dstChannel.close();
				MIDIFile mFile = new MIDIFile(this, midFile);
				mFile.verify();
				if ( !(mFile.getFinishedString().equals(MIDIFile.FINISHED_SUCCESSFULLY))) {
					throw new Exception("Error verifying MIDI file:\n" + mFile.getFinishedString());
				}
			} catch (Exception err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, "Invalid MIDI/chart file selected.", "Invalid MIDI/chart file selected.", JOptionPane.ERROR_MESSAGE);
	        	return;
			}
			esong1.midiFile = midFile;
			esong1.hasNewMIDFile = true;
			esong1.midLocs = midFolder;
		}
		//move on
		this.setVisible(false);
		try {
			//to sync
			new EditSongFive(esong1, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to next step", "Error going to next step", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void initialize() {
		this.setTitle("Substitute new MIDI");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        radDoSwitch.setSelected(true);
        this.setVisible(true);
	}

	private void selectMIDI() {
		JOptionPane.showMessageDialog(this, "NOTE: For now, you can only select MIDI files.\n A future version will include support for chart files.", "Chart files not supported", JOptionPane.WARNING_MESSAGE);
		GHMIDIFilter GHfilt = new GHMIDIFilter();
		if (jfc == null) {
			jfc = new JFileChooser();
		}
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(GHfilt);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        //have to be careful with removeChoosableFileFilter since it seems to cause problems on mac
        if (returnVal == JFileChooser.CANCEL_OPTION) {
        	jfc.removeChoosableFileFilter(GHfilt);
            return;
        }
        this.txtfNewMIDIPath.setText(jfc.getSelectedFile().getPath());
        jfc.removeChoosableFileFilter(GHfilt);
	}

	private void selectNothing() {
		this.btnSelect.setEnabled(false);
		this.txtfNewMIDIPath.setForeground(Color.gray);
		this.txtfNewMIDIPath.setEnabled(false);
	}

	private void selectSomething() {
		this.btnSelect.setEnabled(true);
		this.txtfNewMIDIPath.setForeground(Color.black);
		this.txtfNewMIDIPath.setEnabled(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			int pos = 9;

			radNothing = new JRadioButton();
			radNothing.setBounds(new Rectangle(9, pos, 403, 19));
			radNothing.setFont(GuitarWizardMain.REGFONT);
			radNothing.setText("Keep existing MIDI");
			radNothing.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectNothing();
				}
			});
			pos = pos + 25;

			radDoSwitch = new JRadioButton();
			radDoSwitch.setBounds(new Rectangle(9, pos, 403, 19));
			radDoSwitch.setFont(GuitarWizardMain.REGFONT);
			radDoSwitch.setText("Replace existing MIDI");
			radDoSwitch.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectSomething();
				}
			});
			pos = pos + 25;

			txtfNewMIDIPath = new JTextField();
			txtfNewMIDIPath.setEditable(false);
			txtfNewMIDIPath.setBounds(new Rectangle(59, pos, 303, 19));
			txtfNewMIDIPath.setFont(GuitarWizardMain.REGFONT);
			btnSelect = new JButton();
			btnSelect.setBounds(new Rectangle(379, pos - 2, 144, 22));
			btnSelect.setFont(GuitarWizardMain.REGFONT);
			btnSelect.setText("Select MIDI/chart");
			btnSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectMIDI();
				}
			});
			pos = pos + 25;

			ButtonGroup bgrp = new ButtonGroup();
			bgrp.add(radNothing);
			bgrp.add(radDoSwitch);

			jContentPane.add(radNothing, null);
			jContentPane.add(txtfNewMIDIPath, null);
			jContentPane.add(radDoSwitch, null);
			jContentPane.add(btnSelect, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(479, 153, 74, 22));
			btnCancel.setFont(GuitarWizardMain.REGFONT);
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doCancel();
				}
			});
		}
		return btnCancel;
	}

	private JButton getBtnNext() {
		if (btnNext == null) {
			btnNext = new JButton();
			btnNext.setText("<html><h4>Next</h4></html>");
			btnNext.setBounds(new Rectangle(212, 129, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

}