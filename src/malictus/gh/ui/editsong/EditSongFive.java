package malictus.gh.ui.editsong;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.io.*;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;

import malictus.gh.ark.ExpandARK;
import malictus.gh.audio.*;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.ui.editsettings.EditSettingsOne;
import malictus.gh.midi.*;
import javax.swing.JComboBox;
import java.awt.GridBagConstraints;

/**
 * EditSongFive
 * Step 5 in edit song
 * Edit sync
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongFive extends JDialog implements PlayerUI {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnNext = null;
	private JLabel lblWhatToDo = null;
	private JLabel lblOffset1 = null;
	private JComboBox combAddSubtract = null;
	private JTextField txtfAddSubtract = null;
	private JLabel lblOffset2 = null;
	private JPanel pnlPreview = null;
	private JLabel lblPreview = null;
	private JLabel lblLeft = null;
	private JLabel lblRight = null;
	private JComboBox combLeft = null;
	private JComboBox combRight = null;
	private JButton btnPreview = null;
	private JLabel lblPreviewTime = null;
	private JComboBox combSongSegment = null;
	private JLabel lblSampRate = null;
	private JComboBox combSampRate = null;

	private AudioPlayer aPlayer = null;

	GWMainWindow theParent;
	EditSongOne esong1;

	File clickTrack;

	boolean playing = false;

	public EditSongFive(EditSongOne editsongone, GWMainWindow parent) {
		super(parent);
		theParent = parent;
		esong1 = editsongone;
		this.setSize(new Dimension(710, 356));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		try {
			aPlayer.stop();
			aPlayer.discard();
		} catch(Exception err) {
			err.printStackTrace();
		}
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doPreview() {
		try {
			if (!playing) {
				//verify offset
				String amt = this.txtfAddSubtract.getText();
				int iAmt = -1;
				try {
					iAmt = Integer.valueOf(amt).intValue();
				} catch (NumberFormatException err) {
					JOptionPane.showMessageDialog(this, "You must choose an offset between 0 and 15000.", "Incorrect offset specified", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if ((iAmt < 0) || (iAmt > 15000)) {
					JOptionPane.showMessageDialog(this, "You must choose an offset between 0 and 15000.", "Incorrect offset specified", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (this.combAddSubtract.getSelectedItem().equals("add")) {
					iAmt = 0 - iAmt;
				}
				disableInterface();
				int startTime = this.combSongSegment.getSelectedIndex() * 15000;
				int endTime = startTime + 15000;

				int leftChan = AudioPlayer.SOUND_AUDIO;
				if (this.combLeft.getSelectedItem().equals("METRONOME TICKS")) {
					leftChan = AudioPlayer.SOUND_BEATS;
				} else if (this.combLeft.getSelectedItem().equals("GUITAR TICKS (EXPERT)")) {
					leftChan = AudioPlayer.SOUND_NOTES;
				}

				int rightChan = AudioPlayer.SOUND_AUDIO;
				if (this.combRight.getSelectedItem().equals("METRONOME TICKS")) {
					rightChan = AudioPlayer.SOUND_BEATS;
				} else if (this.combRight.getSelectedItem().equals("GUITAR TICKS (EXPERT)")) {
					rightChan = AudioPlayer.SOUND_NOTES;
				}
				aPlayer.setChannels(leftChan, rightChan);

				float srFactor = 1f;
				if (this.combSampRate.getSelectedItem().equals("90% SPEED")) {
					srFactor = 0.9f;
				} else if (this.combSampRate.getSelectedItem().equals("75% SPEED")) {
					srFactor = 0.75f;
				} else if (this.combSampRate.getSelectedItem().equals("50% SPEED")) {
					srFactor = 0.50f;
				}
				aPlayer.setSRFactor(srFactor);
				aPlayer.setDiff(iAmt);
				aPlayer.play(startTime, endTime);
				playing = true;
			} else {
				aPlayer.stop();
				enableInterface();
				playing = false;
			}
		} catch (Exception err) {
			aPlayer.stop();
			enableInterface();
			playing = false;
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error playing audio", "Error playing audio", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void endOfSegment() {
		playing = false;
		enableInterface();
	}

	private void doNext() {
		//verify amount
		String amt = this.txtfAddSubtract.getText();
		int iAmt = -1;
		try {
			iAmt = Integer.valueOf(amt).intValue();
		} catch (NumberFormatException err) {
			JOptionPane.showMessageDialog(this, "You must choose an offset between 0 and 15000.", "Incorrect offset specified", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if ((iAmt < 0) || (iAmt > 15000)) {
			JOptionPane.showMessageDialog(this, "You must choose an offset between 0 and 15000.", "Incorrect offset specified", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (this.combAddSubtract.getSelectedItem().equals("subtract")) {
			iAmt = 0 - iAmt;
		}
		if (iAmt < 0) {
			int response = JOptionPane.showConfirmDialog(this, "You have chosen to remove " + (0 - iAmt) + " milliseconds of audio\nfrom the beginning of your audio track.\nProceed with this action?", "Confirm removing audio", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.NO_OPTION) {
				return;
			}
		}
		try {
			aPlayer.stop();
			aPlayer.discard();
			if (iAmt != 0) {
				AudioPlayer.addSilence(esong1.vgsFile, iAmt);
				if (esong1.vgsFileCoop != null) {
					AudioPlayer.addSilence(esong1.vgsFileCoop, iAmt);
				}
				esong1.hasNewVGSFile = true;
			}
			this.setVisible(false);
			new EditSongSix(esong1, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to next step", "Error going to next step", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void populate() throws Exception {
		//if needed, grab MID file from archive and tell esong1 we have it
		if (esong1.midiFile == null) {
			lblWhatToDo.setText("<html><h4>Please wait... retrieving MIDI file</h4></html>");
			try {
				String node = "";
				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
					node = esong1.selectedSongID + "/midi_file";
				} else {
					node = esong1.selectedSongID + "/song/midi_file";
				}
				String midFileString = esong1.songs.readString(node);
				String midFolder = midFileString.substring(0, midFileString.lastIndexOf("/"));
				String midFile = midFileString.substring(midFileString.lastIndexOf("/") + 1);
				esong1.midiFile = GHUtils.putInTemp(midFile, midFolder);
				MIDIFile mf = new MIDIFile(this, esong1.midiFile);
				lblWhatToDo.setText("<html><h4>Please wait... verifying MIDI file</h4></html>");
				mf.verify();
				if ( !(mf.getFinishedString().equals(MIDIFile.FINISHED_SUCCESSFULLY))) {
					throw new Exception("Error verifying MIDI file:\n" + mf.getFinishedString());
				}
			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception("Error retrieving MIDI file from archive");
			}
		}
		//if needed, grab VGS file(s) from archive and tell esong1 we have it (them)
		boolean useCoop = false;
		if (esong1.vgsFile == null) {
			lblWhatToDo.setText("<html><h4>Please wait... retrieving VGS file</h4></html>");
			try {
				String node = esong1.selectedSongID + "/song/name";
				String vgsFileString = esong1.songs.readString(node);
				vgsFileString = vgsFileString + ".vgs";
				String vgsFolder = vgsFileString.substring(0, vgsFileString.lastIndexOf("/"));
				String vgsFile = vgsFileString.substring(vgsFileString.lastIndexOf("/") + 1);
				esong1.vgsFile = GHUtils.putInTemp(vgsFile, vgsFolder);
				esong1.vgsLocs = vgsFolder;
				//test for coop
				node = esong1.selectedSongID + "/song_coop/name";
				if (esong1.songs.nodeExists(node)) {
					vgsFileString = esong1.songs.readString(node);
					vgsFileString = vgsFileString + ".vgs";
					vgsFolder = vgsFileString.substring(0, vgsFileString.lastIndexOf("/"));
					vgsFile = vgsFileString.substring(vgsFileString.lastIndexOf("/") + 1);
					esong1.vgsFileCoop = GHUtils.putInTemp(vgsFile, vgsFolder);
					String response = null;
					while (response == null) {
						response = (String)JOptionPane.showInputDialog(this, "A separate co-op audio track exists for this song.\nPlease choose " +
							"whether to use the co-op or\n the single player audio for setting sync and preview time.", "Choose co-op or single",
							JOptionPane.INFORMATION_MESSAGE, null, new String[] {"Co-op audio", "Single player audio"}, "Co-op audio");
					}
					if (response.equals("Co-op audio")) {
						useCoop = true;
					}
				}
			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception("Error retrieving VGS file");
			}
		}
		lblWhatToDo.setText("<html><h4>Please wait... getting song duration</h4></html>");
		//find duration for VGS file, since we'll need it for creating click track
		int duration = AudioPlayer.getDurationFor(esong1.vgsFile);
		float samprate = AudioPlayer.getSampRateFor(esong1.vgsFile);
		//read MIDI file and create temporary 'CLICK' wav file
		//has already been verified now
		MIDIFile mf = new MIDIFile(this, esong1.midiFile);
		clickTrack = null;
		try {
			clickTrack = File.createTempFile("clk", ".wav", GuitarWizardMain.TempDir);
		} catch (Exception err) {
			err.printStackTrace();
			throw new Exception("Error creating temp file");
		}
		clickTrack.deleteOnExit();
		try {
			lblWhatToDo.setText("<html><h4>Please wait... creating metronome track</h4></html>");
			mf.createClickTrack(clickTrack, duration, samprate);
			if ( !(mf.getFinishedString().equals(MIDIFile.FINISHED_SUCCESSFULLY))) {
				throw new Exception("Error creating metronome track");
			}
			clickTrack = mf.getCreatedClickTrack();
		} catch (Exception err) {
			err.printStackTrace();
			throw new Exception("Error creating metronome track");
		}
		lblWhatToDo.setText("<html><h4>Please wait... initializing audio player</h4></html>");
		if (useCoop) {
			aPlayer = new AudioPlayer(esong1.vgsFileCoop, clickTrack, this);
		} else {
			aPlayer = new AudioPlayer(esong1.vgsFile, clickTrack, this);
		}
		//populate combo box that specifies what portion of song to listen to
		float numOfSecs = (float)duration / 1000f;
		int numOf15s = (int)(numOfSecs / 15f);
		int counter = 0;
		while (counter < numOf15s) {
			int start = counter * 15000;
			int end = start + 15000;
			this.combSongSegment.addItem("" + GHUtils.convertOffsetToMinutesSeconds(start) + " - " + GHUtils.convertOffsetToMinutesSeconds(end));
			counter = counter + 1;
		}
	}

	private void disableInterface() {
		this.txtfAddSubtract.setEnabled(false);
		this.btnPreview.setText("Stop preview");
		this.btnCancel.setEnabled(false);
		this.btnNext.setEnabled(false);
		btnNext.setForeground((Color) UIManager.get("Label.disabledForeground"));
		this.combAddSubtract.setEnabled(false);
		this.combLeft.setEnabled(false);
		this.combRight.setEnabled(false);
		this.combSongSegment.setEnabled(false);
		this.combSampRate.setEnabled(false);
	}

	private void enableInterface() {
		this.txtfAddSubtract.setEnabled(true);
		this.btnPreview.setText("Play preview");
		this.btnCancel.setEnabled(true);
		this.btnNext.setEnabled(true);
		btnNext.setForeground((Color) UIManager.get("Label.enabledForeground"));
		this.combAddSubtract.setEnabled(true);
		this.combLeft.setEnabled(true);
		this.combRight.setEnabled(true);
		this.combSongSegment.setEnabled(true);
		this.combSampRate.setEnabled(true);
	}

	private void disableWindow() {
		this.txtfAddSubtract.setEnabled(false);
		this.btnCancel.setEnabled(false);
		this.btnNext.setEnabled(false);
		btnNext.setForeground((Color) UIManager.get("Label.disabledForeground"));
		this.btnPreview.setEnabled(false);
		this.combAddSubtract.setEnabled(false);
		this.combLeft.setEnabled(false);
		this.combRight.setEnabled(false);
		this.combSongSegment.setEnabled(false);
		this.combSampRate.setEnabled(false);
		lblWhatToDo.setText("<html><center><h4>Loading Audio... Please Wait (this might take a minute or two)</h4></center></html>");
	}

	private void enableWindow() {
		this.txtfAddSubtract.setEnabled(true);
		this.btnCancel.setEnabled(true);
		this.btnNext.setEnabled(true);
		btnNext.setForeground((Color) UIManager.get("Label.enabledForeground"));
		this.btnPreview.setEnabled(true);
		this.combAddSubtract.setEnabled(true);
		this.combLeft.setEnabled(true);
		this.combRight.setEnabled(true);
		this.combSongSegment.setEnabled(true);
		this.combSampRate.setEnabled(true);
		lblWhatToDo.setText("<html><center>Use this page to adjust the sync of your audio to your midi file. Choose an offset value, then listen to a preview of the audio and MIDI sync. When you have an appropriate offset, press 'Next' to continue</center></html>");
	}

	private void initialize() {
		this.setTitle("Set sync");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.txtfAddSubtract.setText("0");
        this.combLeft.setSelectedItem("GUITAR TICKS (EXPERT)");
        this.combRight.setSelectedItem("AUDIO");
        this.setResizable(false);
        this.disableWindow();
        final EditSongFive x = this;
        Runnable q = new Runnable() {
            public void run() {
            	try {
                	populate();
                } catch (Exception err) {
                	err.printStackTrace();
                	JOptionPane.showMessageDialog(x, "ERROR reading files:\n" + err.getMessage(), "Error reading files", JOptionPane.ERROR_MESSAGE);
                	GHUtils.cleanupTempDir();
                	x.setVisible(false);
                	return;
                }
                x.enableWindow();
            }
        };
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblOffset2 = new JLabel();
			lblOffset2.setBounds(new Rectangle(341, 71, 253, 20));
			lblOffset2.setText("milliseconds to audio track");
			lblOffset2.setFont(GuitarWizardMain.REGFONT);
			lblOffset1 = new JLabel();
			lblOffset1.setBounds(new Rectangle(111, 72, 46, 20));
			lblOffset1.setFont(GuitarWizardMain.REGFONT);
			lblOffset1.setText("Offset:");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			int pos = 12;
			lblWhatToDo = new JLabel();
			lblWhatToDo.setBounds(new Rectangle(74, 7, 574, 46));
			lblWhatToDo.setFont(GuitarWizardMain.REGFONT);
			lblWhatToDo.setText("<html><center>Use this page to adjust the sync of your audio to your midi file. Choose an offset value, then listen to a preview of the audio and MIDI sync. When you have an appropriate offset, press 'Next' to continue</center></html>");
			pos = pos + 25;

			jContentPane.add(lblWhatToDo, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
			jContentPane.add(lblOffset1, null);
			jContentPane.add(getCombAddSubtract(), null);
			jContentPane.add(getTxtfAddSubtract(), null);
			jContentPane.add(lblOffset2, null);
			jContentPane.add(getPnlPreview(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(621, 298, 74, 22));
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
			btnNext.setBounds(new Rectangle(296, 277, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

	private JComboBox getCombAddSubtract() {
		if (combAddSubtract == null) {
			combAddSubtract = new JComboBox();
			combAddSubtract.setFont(GuitarWizardMain.REGFONT);
			combAddSubtract.setEditable(false);
			combAddSubtract.addItem("subtract");
			combAddSubtract.addItem("add");
			combAddSubtract.setSelectedItem("add");
			combAddSubtract.setBounds(new Rectangle(165, 71, 95, 22));
		}
		return combAddSubtract;
	}

	private JTextField getTxtfAddSubtract() {
		if (txtfAddSubtract == null) {
			txtfAddSubtract = new JTextField();
			txtfAddSubtract.setFont(GuitarWizardMain.REGFONT);
			txtfAddSubtract.setDocument(new LimitedTextField(5));
			txtfAddSubtract.setHorizontalAlignment(JTextField.CENTER);
			txtfAddSubtract.setBounds(new Rectangle(270, 71, 58, 22));
			txtfAddSubtract.setText("0");
		}
		return txtfAddSubtract;
	}

	private JPanel getPnlPreview() {
		if (pnlPreview == null) {
			lblPreviewTime = new JLabel();
			lblPreviewTime.setBounds(new Rectangle(95, 76, 91, 18));
			lblPreviewTime.setFont(GuitarWizardMain.REGFONT);
			lblPreviewTime.setText("Song segment:");
			lblSampRate = new JLabel();
			lblSampRate.setBounds(new Rectangle(325, 76, 91, 18));
			lblSampRate.setFont(GuitarWizardMain.REGFONT);
			lblSampRate.setText("Song speed:");
			lblRight = new JLabel();
			lblRight.setBounds(new Rectangle(330, 36, 83, 23));
			lblRight.setText("Right Channel:");
			lblRight.setFont(GuitarWizardMain.REGFONT);
			lblLeft = new JLabel();
			lblLeft.setBounds(new Rectangle(43, 35, 76, 23));
			lblLeft.setText("Left Channel:");
			lblLeft.setFont(GuitarWizardMain.REGFONT);
			lblPreview = new JLabel();
			lblPreview.setText("<html><h4>Song Preview</h4></html>");
			lblPreview.setBounds(new Rectangle(256, 7, 85, 16));
			pnlPreview = new JPanel();
			pnlPreview.setLayout(null);
			pnlPreview.setBounds(new Rectangle(48, 106, 623, 158));
			pnlPreview.setBorder(BorderFactory.createEtchedBorder());
			pnlPreview.add(lblPreview, null);
			pnlPreview.add(lblLeft, null);
			pnlPreview.add(lblRight, null);
			pnlPreview.add(getCombLeft(), null);
			pnlPreview.add(getCombRight(), null);
			pnlPreview.add(getBtnPreview(), null);
			pnlPreview.add(lblPreviewTime, null);
			pnlPreview.add(lblSampRate, null);
			pnlPreview.add(getCombSongSegment(), null);
			pnlPreview.add(getCombSampRate(), null);
		}
		return pnlPreview;
	}

	private JComboBox getCombLeft() {
		if (combLeft == null) {
			combLeft = new JComboBox();
			combLeft.setFont(GuitarWizardMain.REGFONT);
			combLeft.addItem("METRONOME TICKS");
			combLeft.addItem("GUITAR TICKS (EXPERT)");
			combLeft.addItem("AUDIO");
			combLeft.setBounds(new Rectangle(122, 37, 183, 20));
		}
		return combLeft;
	}

	private JComboBox getCombRight() {
		if (combRight == null) {
			combRight = new JComboBox();
			combRight.setFont(GuitarWizardMain.REGFONT);
			combRight.addItem("METRONOME TICKS");
			combRight.addItem("GUITAR TICKS (EXPERT)");
			combRight.addItem("AUDIO");
			combRight.setBounds(new Rectangle(421, 38, 183, 20));
		}
		return combRight;
	}

	private JButton getBtnPreview() {
		if (btnPreview == null) {
			btnPreview = new JButton();
			btnPreview.setFont(GuitarWizardMain.REGFONT);
			btnPreview.setText("Play preview");
			btnPreview.setBounds(new Rectangle(237, 113, 144, 30));
			btnPreview.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doPreview();
				}
			});
		}
		return btnPreview;
	}

	private JComboBox getCombSongSegment() {
		if (combSongSegment == null) {
			combSongSegment = new JComboBox();
			combSongSegment.setFont(GuitarWizardMain.REGFONT);
			combSongSegment.setEditable(false);
			combSongSegment.setBounds(new Rectangle(189, 76, 113, 20));
		}
		return combSongSegment;
	}

	private JComboBox getCombSampRate() {
		if (combSampRate == null) {
			combSampRate = new JComboBox();
			combSampRate.setFont(GuitarWizardMain.REGFONT);
			combSampRate.setEditable(false);
			combSampRate.setBounds(new Rectangle(419, 76, 133, 20));
			combSampRate.addItem("NORMAL SPEED");
			combSampRate.addItem("90% SPEED");
			combSampRate.addItem("75% SPEED");
			combSampRate.addItem("50% SPEED");
		}
		return combSampRate;
	}
}