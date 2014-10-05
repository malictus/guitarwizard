package malictus.gh.ui.editsong;

import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.ui.editsettings.EditSettingsOne;
import malictus.gh.ark.*;
import malictus.gh.dtb.DTBFile;
import malictus.gh.dtb.DTBValue;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.*;
import java.util.Vector;
import java.nio.channels.*;

/**
 * EditSettingsFinal
 * Final in Edit Song, where actual changes are made
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongFinal extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnClose = null;
	private JLabel lblExplanation = null;
	public JLabel lblStep1 = null;
	public JLabel lblStep2 = null;
	public JLabel lblStep3 = null;
	public JLabel lblStep4 = null;

	EditSongOne esong1;

	public EditSongFinal(EditSongOne esong1, GWMainWindow parent) {
		super(parent);
		this.esong1 = esong1;
		this.setSize(new Dimension(341, 333));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doEditSong() {
		/************* STEP 1 **************************/
		lblStep1.setText("<html>Editing DTB Files...</html>");
		boolean tipsAdjusted = false;
		try {
			esong1.songs.writeString(esong1.selectedSongID + "/name", esong1.newSongTitle, 18);
			esong1.songs.writeString(esong1.selectedSongID + "/artist", esong1.newArtistName, 18);
			if (esong1.newPrice != -1) {
				esong1.store.write0Int("song/" + this.esong1.selectedSongID + "/price", new Integer(esong1.newPrice));
			}
			if (esong1.hasNewStoreDesc) {
				esong1.locale.writeString(esong1.selectedSongID + "_shop_desc", esong1.newStoreDesc, 18);
				//also rewrite store version of song name
				esong1.locale.writeString(esong1.selectedSongID, esong1.newSongTitle, 18);
			}
			String bandConfigNode = esong1.selectedSongID + "/band";
			if (esong1.songs.nodeExists(bandConfigNode)) {
				esong1.songs.deleteNode(bandConfigNode);
			}
			if (!(esong1.bandConfig.equals(EditSongSix.BAND_MALE))) {
				if (esong1.bandConfig.equals(EditSongSix.BAND_FEMALE)) {
					String s1 = "metal_bass";
					String s2 = "metal_drummer";
					String s3 = "female_singer";
					if ( GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1 ) {
						s1 = "SINGER_FEMALE_METAL";
						s2 = "BASS_METAL";
						s3 = "DRUMMER_METAL";
					}
					Vector x = new Vector();
					x.add(s1);
					x.add(s2);
					x.add(s3);
					esong1.songs.createStringVectorNode(esong1.selectedSongID + "/band", x, esong1.songs.getChildNodesFor(esong1.selectedSongID).size() + 1);
				}
				if (esong1.bandConfig.equals(EditSongSix.BAND_KEYS)) {
					String s1 = "metal_bass";
					String s2 = "metal_drummer";
					String s3 = "metal_keyboard";
					if ( GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1 ) {
						s1 = "KEYBOARD_METAL";
						s2 = "BASS_METAL";
						s3 = "DRUMMER_METAL";
					}
					Vector x = new Vector();
					x.add(s1);
					x.add(s2);
					x.add(s3);
					esong1.songs.createStringVectorNode(esong1.selectedSongID + "/band", x, esong1.songs.getChildNodesFor(esong1.selectedSongID).size() + 1);
				}
				if (esong1.bandConfig.equals(EditSongSix.BAND_NONE)) {
					String s1 = "metal_bass";
					String s2 = "metal_drummer";
					Vector x = new Vector();
					x.add(s1);
					x.add(s2);
					esong1.songs.createStringVectorNode(esong1.selectedSongID + "/band", x, esong1.songs.getChildNodesFor(esong1.selectedSongID).size() + 1);
				}
			}
			String loadingTipNode = "loading_tip_" + esong1.selectedSongID;
			if (esong1.loadingTip.equals("")) {
				if (esong1.locale.nodeExists(loadingTipNode)) {
					esong1.locale.deleteNode(loadingTipNode);
				}
			} else {
				if (esong1.locale.nodeExists(loadingTipNode)) {
					esong1.locale.deleteNode(loadingTipNode);
				}
				esong1.locale.createStringNode(loadingTipNode, esong1.loadingTip, 1, 18);
				//also delete any extra node refs in tips (for freebird/playwithme)
				String bogusTip = "tips_" + esong1.selectedSongID;
				if (esong1.tips.nodeExists(bogusTip)) {
					esong1.tips.deleteNode(bogusTip);
					tipsAdjusted = true;
				}
				//can't delete encore tips without crashing
				/*  DONT USE
				bogusTip = "tips_encore";
				if (esong1.tips.nodeExists(bogusTip)) {
					esong1.tips.deleteNode(bogusTip);
					tipsAdjusted = true;
				}
				*/
			}

			String previewNode = esong1.selectedSongID + "/preview";
			Integer start = new Integer(esong1.startTime);
			Integer end = new Integer(esong1.endTime);
			Vector startend = new Vector();
			startend.add(start);
			startend.add(end);
			int position = esong1.songs.getNodePosition(previewNode);
			esong1.songs.deleteNode(previewNode);
			esong1.songs.createIntegerVectorNode(previewNode, startend, position);

			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
				if (esong1.isPerfBy) {
					if (!(esong1.songs.nodeExists(esong1.selectedSongID + "/caption"))) {
						esong1.songs.createStringNode(esong1.selectedSongID + "/caption", EditSettingsOne.perfByKey_2_80, 3, 5);
					} else {
						esong1.songs.writeString(esong1.selectedSongID + "/caption", EditSettingsOne.perfByKey_2_80, 5);
					}
				} else {
					if (esong1.songs.nodeExists(esong1.selectedSongID + "/caption")) {
						esong1.songs.deleteNode(esong1.selectedSongID + "/caption");
					}
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error editing DTB files; the archive was not altered", "Error editing DTB files", JOptionPane.ERROR_MESSAGE);
			GHUtils.cleanupTempDir();
			lblExplanation.setText("Edit Song - ERROR");
			this.setTitle("Edit Song - ERROR");
			lblStep1.setText("<html><h4>Editing DTB Files... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep1.setText("<html><h4>Editing DTB Files... COMPLETED</h4></html>");

		/************* STEP 2 **************************/
		lblStep2.setText("<html>Encrypting...</html>");
		try {
			esong1.songs.encrypt();
			esong1.store.encrypt();
			esong1.locale.encrypt();
			if (tipsAdjusted) {
				esong1.tips.encrypt();
			}
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error encrypting DTB files; the archive was not altered", "Error encrypting DTB files", JOptionPane.ERROR_MESSAGE);
			GHUtils.cleanupTempDir();
			lblExplanation.setText("Edit Song - ERROR");
			this.setTitle("Edit Song - ERROR");
			lblStep2.setText("<html><h4>Encrypting... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep2.setText("<html><h4>Encrypting... COMPLETED</h4></html>");

		/************* STEP 3 **************************/
		lblStep3.setText("<html>Reinserting files to ARK file...</html>");
		ReinsertFile rf = new ReinsertFile(this, esong1.songs.getDTBFile(), EditSongOne.SONGS_FILE_LOC);
		if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Edit Song - ERROR");
			this.setTitle("Edit Song - ERROR");
			lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		rf = new ReinsertFile(this, esong1.store.getDTBFile(), EditSongOne.STORE_FILE_LOC);
		if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Edit Song - ERROR");
			this.setTitle("Edit Song - ERROR");
			lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		rf = new ReinsertFile(this, esong1.locale.getDTBFile(), esong1.LOCALE_FILE_LOC_REAL);
		if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Edit Song - ERROR");
			this.setTitle("Edit Song - ERROR");
			lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		if (tipsAdjusted) {
			rf = new ReinsertFile(this, esong1.tips.getDTBFile(), esong1.TIPS_FILE_LOC);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Song - ERROR");
				this.setTitle("Edit Song - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
		}
		if (esong1.hasNewMIDFile) {
			rf = new ReinsertFile(this, esong1.midiFile, esong1.midLocs);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting MIDI file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Song - ERROR");
				this.setTitle("Edit Song - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
		}
		if (esong1.hasNewVGSFile) {
			//coop version
			if (esong1.vgsFileCoop != null) {
				rf = new ReinsertFile(this, esong1.vgsFileCoop, esong1.vgsLocs);
				if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
					GHUtils.cleanupTempDir();
					JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
					lblExplanation.setText("Edit Song - ERROR");
					this.setTitle("Edit Song - ERROR");
					lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
					btnClose.setEnabled(true);
					return;
				}
			}
			//regular version
			rf = new ReinsertFile(this, esong1.vgsFile, esong1.vgsLocs);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Song - ERROR");
				this.setTitle("Edit Song - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
		}
		lblStep3.setText("<html><h4>Reinserting files to ARK file... COMPLETED</h4></html>");

		/************* STEP 4 **************************/
		lblStep4.setText("<html>Removing temp files...</html>");
		GHUtils.cleanupTempDir();
		lblStep4.setText("<html><h4>Removing temp files... COMPLETED</h4></html>");

		lblExplanation.setText("<html><center>Changes completed successfully</center></html>");

		this.setTitle("Edit Song Completed");
		btnClose.setEnabled(true);
	}

	private void initialize() {
		this.setTitle("Altering Song Files...");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize(new Dimension(341, 333));
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        btnClose.setEnabled(false);
        Runnable q = new Runnable() {
            public void run() {
            	doEditSong();
            }
        };
        Thread t = new Thread(q);
        t.start();

        this.setVisible(true);
	}

	private void doClose() {
		this.setVisible(false);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblExplanation = new JLabel();
			lblExplanation.setBounds(new Rectangle(6, 5, 322, 45));
			lblExplanation.setText("<html><center>Please wait while changes are being made...</center></html>");

			int pos = 63;
			lblStep1 = new JLabel();
			lblStep1.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep1.setText("Editing DTB Files...");
			pos = pos + 30;

			lblStep2 = new JLabel();
			lblStep2.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep2.setText("");
			pos = pos + 30;

			lblStep3 = new JLabel();
			lblStep3.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep3.setText("");
			pos = pos + 30;

			lblStep4 = new JLabel();
			lblStep4.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep4.setText("");
			pos = pos + 30;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnClose(), null);
			jContentPane.add(lblExplanation, null);
			jContentPane.add(lblStep1, null);
			jContentPane.add(lblStep2, null);
			jContentPane.add(lblStep3, null);
			jContentPane.add(lblStep4, null);

		}
		return jContentPane;
	}

	private JButton getBtnClose() {
		if (btnClose == null) {
			btnClose = new JButton();
			btnClose.setBounds(new Rectangle(254, 273, 74, 22));
			btnClose.setFont(GuitarWizardMain.REGFONT);
			btnClose.setText("Close");
			btnClose.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doClose();
				}
			});
		}
		return btnClose;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"