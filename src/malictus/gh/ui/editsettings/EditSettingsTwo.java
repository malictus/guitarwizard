package malictus.gh.ui.editsettings;

import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.ark.*;
import malictus.gh.dtb.DTBFile;

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
 * EditSettingsTwo
 * Step 2 in Edit Settings
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSettingsTwo extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnClose = null;
	private JLabel lblExplanation = null;
	public JLabel lblStep1 = null;
	public JLabel lblStep2 = null;
	public JLabel lblStep3 = null;
	public JLabel lblStep4 = null;

	EditSettingsOne es1;

	public EditSettingsTwo(EditSettingsOne es1, GWMainWindow parent) {
		super(parent);
		this.es1 = es1;
		this.setSize(new Dimension(341, 333));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doEditSettings() {
		/************* STEP 1 **************************/
		lblStep1.setText("<html>Editing DTB Files...</html>");
		try {
			if (es1.tiersEdited) {
				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
					es1.locale.writeString(EditSettingsOne.tier1Key_GH1, es1.tier1Name, 18);
					es1.locale.writeString(EditSettingsOne.tier2Key_GH1, es1.tier2Name, 18);
					es1.locale.writeString(EditSettingsOne.tier3Key_GH1, es1.tier3Name, 18);
					es1.locale.writeString(EditSettingsOne.tier4Key_GH1, es1.tier4Name, 18);
					es1.locale.writeString(EditSettingsOne.tier5Key_GH1, es1.tier5Name, 18);
					es1.locale.writeString(EditSettingsOne.tier6Key_GH1, es1.tier6Name, 18);
					es1.locale.writeString(EditSettingsOne.tier7Key_GH1, es1.tier7Name, 18);
				} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
					es1.locale.writeString(EditSettingsOne.tier1Key_GH2, es1.tier1Name, 18);
					es1.locale.writeString(EditSettingsOne.tier2Key_GH2, es1.tier2Name, 18);
					es1.locale.writeString(EditSettingsOne.tier3Key_GH2, es1.tier3Name, 18);
					es1.locale.writeString(EditSettingsOne.tier4Key_GH2, es1.tier4Name, 18);
					es1.locale.writeString(EditSettingsOne.tier5Key_GH2, es1.tier5Name, 18);
					es1.locale.writeString(EditSettingsOne.tier6Key_GH2, es1.tier6Name, 18);
					es1.locale.writeString(EditSettingsOne.tier7Key_GH2, es1.tier7Name, 18);
					es1.locale.writeString(EditSettingsOne.tier8Key_GH2, es1.tier8Name, 18);
					es1.locale.writeString(EditSettingsOne.tier9Key_GH2, es1.tier9Name, 18);
				} else {
					es1.locale.writeString(EditSettingsOne.tier1Key_GH80, es1.tier1Name, 18);
					es1.locale.writeString(EditSettingsOne.tier2Key_GH80, es1.tier2Name, 18);
					es1.locale.writeString(EditSettingsOne.tier3Key_GH80, es1.tier3Name, 18);
					es1.locale.writeString(EditSettingsOne.tier4Key_GH80, es1.tier4Name, 18);
					es1.locale.writeString(EditSettingsOne.tier5Key_GH80, es1.tier5Name, 18);
					es1.locale.writeString(EditSettingsOne.tier6Key_GH80, es1.tier6Name, 18);
				}
			}
			if (es1.diffsEdited) {
				es1.locale.writeString(EditSettingsOne.diffEasyKey_ALL, es1.diffEasy.toUpperCase(), 18);
				es1.locale.writeString(EditSettingsOne.diffMediumKey_ALL, es1.diffMedium.toUpperCase(), 18);
				es1.locale.writeString(EditSettingsOne.diffHardKey_ALL, es1.diffHard.toUpperCase(), 18);
				es1.locale.writeString(EditSettingsOne.diffExpertKey_ALL, es1.diffExpert.toUpperCase(), 18);
			}
			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1)) {
				if (es1.splashEdited) {
					String splashLegal = es1.splashLegal1 + GuitarWizardMain.LINEBREAKSTRING + es1.splashLegal2 +
							GuitarWizardMain.LINEBREAKSTRING + es1.splashLegal3;
					es1.locale.writeString(EditSettingsOne.splashLegalKey_2_80, splashLegal, 18);
					es1.locale.writeString(EditSettingsOne.splashAnyButtonKey_2_80, es1.splashAnyButton.toUpperCase(), 18);
				}
			}
			if (es1.mainMenuEdited) {
				es1.locale.writeString(EditSettingsOne.mainCareerKey_ALL, es1.mainCareer, 18);
				es1.locale.writeString(EditSettingsOne.mainMultiplayKey_ALL, es1.mainMultiplay, 18);
				es1.locale.writeString(EditSettingsOne.mainOptionsKey_ALL, es1.mainOptions, 18);
				es1.locale.writeString(EditSettingsOne.mainQuickPlayKey_ALL, es1.mainQuickPlay, 18);
				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
					es1.locale.writeString(EditSettingsOne.mainTrainingKey_1, es1.mainTraining, 18);
				} else {
					es1.locale.writeString(EditSettingsOne.mainTrainingKey_2_80, es1.mainTraining, 18);
				}
			}
			if (es1.perfByEdited) {
				if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
					es1.locale.writeString(EditSettingsOne.perfByKey_2_80, es1.perfBy, 18);
				}
				es1.locale.writeString(EditSettingsOne.famousByKey_ALL, es1.famousBy, 18);
			}
			if (es1.gameplayOptsEdited) {
				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
					if (es1.neverFail) {
						es1.scoring.write1Float(EditSettingsOne.neverFailKey1_1, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey2_1, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey3_1, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey4_1, new Float(0f));
					} else {
						//write default values instead
						es1.scoring.write1Float(EditSettingsOne.neverFailKey1_1, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey2_1, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey3_1, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey4_1, new Float(EditSettingsOne.neverFailDefaultValue));
					}
				} else {
					if (es1.neverFail) {
						es1.scoring.write1Float(EditSettingsOne.neverFailKey1_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey2_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey3_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey4_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey5_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey6_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey7_2_80, new Float(0f));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey8_2_80, new Float(0f));
					} else {
						//write default values instead
						es1.scoring.write1Float(EditSettingsOne.neverFailKey1_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey2_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey3_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey4_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey5_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey6_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey7_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
						es1.scoring.write1Float(EditSettingsOne.neverFailKey8_2_80, new Float(EditSettingsOne.neverFailDefaultValue));
					}
				}
				es1.track_graphics.write0Int(EditSettingsOne.NOTE_SPEED_EASY_KEY_ALL, new Integer(es1.noteSpeedEasyMedium));
				es1.track_graphics.write0Int(EditSettingsOne.NOTE_SPEED_MEDIUM_KEY_ALL, new Integer(es1.noteSpeedEasyMedium));
				es1.track_graphics.write1Float(EditSettingsOne.NOTE_SPEED_HARD_KEY_ALL, new Float(es1.noteSpeedHardExpert));
				es1.track_graphics.write1Float(EditSettingsOne.NOTE_SPEED_EXPERT_KEY_ALL, new Float(es1.noteSpeedHardExpert));
			}
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error editing DTB files; the archive was not altered", "Error editing DTB files", JOptionPane.ERROR_MESSAGE);
			GHUtils.cleanupTempDir();
			lblExplanation.setText("Edit Settings - ERROR");
			this.setTitle("Edit Settings - ERROR");
			lblStep1.setText("<html><h4>Editing DTB Files... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep1.setText("<html><h4>Editing DTB Files... COMPLETED</h4></html>");

		/************* STEP 2 **************************/
		lblStep2.setText("<html>Encrypting...</html>");
		try {
			if ( (es1.perfByEdited) || (es1.tiersEdited) || (es1.diffsEdited) || (es1.splashEdited) || (es1.mainMenuEdited) ) {
				es1.locale.encrypt();
			}
			if (es1.gameplayOptsEdited) {
				es1.scoring.encrypt();
				es1.track_graphics.encrypt();
			}
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error encrypting DTB files; the archive was not altered", "Error encrypting DTB files", JOptionPane.ERROR_MESSAGE);
			GHUtils.cleanupTempDir();
			lblExplanation.setText("Edit Settings - ERROR");
			this.setTitle("Edit Settings - ERROR");
			lblStep2.setText("<html><h4>Encrypting... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep2.setText("<html><h4>Encrypting... COMPLETED</h4></html>");

		/************* STEP 3 **************************/
		lblStep3.setText("<html>Reinserting to ARK File...</html>");
		if ( (es1.perfByEdited) || (es1.tiersEdited) || (es1.diffsEdited) || (es1.splashEdited)|| (es1.mainMenuEdited) ) {
			ReinsertFile rf = new ReinsertFile(this, es1.locale.getDTBFile(), es1.LOCALE_FILE_LOC_REAL);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Settings - ERROR");
				this.setTitle("Edit Settings - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
		}
		if (es1.gameplayOptsEdited) {
			ReinsertFile rf = new ReinsertFile(this, es1.scoring.getDTBFile(), EditSettingsOne.SCORING_FILE_LOC);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Settings - ERROR");
				this.setTitle("Edit Settings - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
			rf = new ReinsertFile(this, es1.track_graphics.getDTBFile(), EditSettingsOne.TRACK_GRAPHICS_FILE_LOC);
			if ( !(rf.getFinishedString().equals(ReinsertFile.FINISHED_SUCCESSFULLY)) ) {
				GHUtils.cleanupTempDir();
				JOptionPane.showMessageDialog(this, "ERROR reinserting DTB file:\n" + rf.getFinishedString(), "Error reinserting DTB file", JOptionPane.ERROR_MESSAGE);
				lblExplanation.setText("Edit Settings - ERROR");
				this.setTitle("Edit Settings - ERROR");
				lblStep3.setText("<html><h4>Reinserting to ARK File... ERROR</h4></html>");
				btnClose.setEnabled(true);
				return;
			}
		}
		lblStep3.setText("<html><h4>Reinserting to ARK File... COMPLETED</h4></html>");

		/************* STEP 4 **************************/
		lblStep4.setText("<html>Removing temp files...</html>");
		GHUtils.cleanupTempDir();
		lblStep4.setText("<html><h4>Removing temp files... COMPLETED</h4></html>");

		lblExplanation.setText("<html><center>Changes completed successfully</center></html>");

		this.setTitle("Edit Settings Completed");
		btnClose.setEnabled(true);
	}

	private void initialize() {
		this.setTitle("Changing Settings...");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize(new Dimension(341, 333));
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        btnClose.setEnabled(false);
        Runnable q = new Runnable() {
            public void run() {
            	doEditSettings();
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