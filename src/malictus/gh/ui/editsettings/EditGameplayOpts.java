package malictus.gh.ui.editsettings;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import malictus.gh.ui.LimitedTextField;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditGameplayOpts
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditGameplayOpts extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	protected JCheckBox chkNeverFail = null;
	protected JLabel lblSpeedEasyMedium = null;
	protected JComboBox combSpeedEasyMedium = null;
	protected JLabel lblSpeedHardExpert = null;
	protected JComboBox combSpeedHardExpert = null;

	boolean canceled = false;

	private int easyMediumNoteSpeed;
	private float hardExpertNoteSpeed;

	EditSettingsOne theParent;

	public EditGameplayOpts(EditSettingsOne parent) throws Exception {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(293, 204));
		GHUtils.centerWindow(this);
		initialize();
	}

	public float getNewHardExpertVal() {
		return hardExpertNoteSpeed;
	}

	public int getNewEasyMediumVal() {
		return easyMediumNoteSpeed;
	}

	private void doCancel() {
		canceled = true;
		this.setVisible(false);
	}

	private void doSave() {
		canceled = false;
		this.setVisible(false);
	}

	private void initialize() throws Exception {
		this.setTitle("Edit Gameplay Options");
		this.setSize(new Dimension(293, 204));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void comboBoxChanged() {
		if (combSpeedEasyMedium.getSelectedIndex() == 0) {
			easyMediumNoteSpeed = EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_DEFAULT;
		} else if (combSpeedEasyMedium.getSelectedIndex() == 1) {
			easyMediumNoteSpeed = EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_HYPERSPEED;
		} else if (combSpeedEasyMedium.getSelectedIndex() == 2) {
			easyMediumNoteSpeed = EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_ULTRASPEED;
		}
		if (combSpeedHardExpert.getSelectedIndex() == 0) {
			hardExpertNoteSpeed = EditSettingsOne.NOTE_SPEED_HARD_EXPERT_DEFAULT;
		} else if (combSpeedHardExpert.getSelectedIndex() == 1) {
			hardExpertNoteSpeed = EditSettingsOne.NOTE_SPEED_HARD_EXPERT_HYPERSPEED;
		} else if (combSpeedHardExpert.getSelectedIndex() == 2) {
			hardExpertNoteSpeed = EditSettingsOne.NOTE_SPEED_HARD_EXPERT_ULTRASPEED;
		}
	}

	void populate() throws Exception {
		chkNeverFail.setSelected(theParent.neverFail);
		if (theParent.noteSpeedEasyMedium == EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_DEFAULT) {
			combSpeedEasyMedium.setSelectedIndex(0);
		} else if (theParent.noteSpeedEasyMedium == EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_HYPERSPEED) {
			combSpeedEasyMedium.setSelectedIndex(1);
		} else if (theParent.noteSpeedEasyMedium == EditSettingsOne.NOTE_SPEED_EASY_MEDIUM_ULTRASPEED) {
			combSpeedEasyMedium.setSelectedIndex(2);
		} else {
			throw new Exception ("Error setting note speed");
		}
		if (theParent.noteSpeedHardExpert == EditSettingsOne.NOTE_SPEED_HARD_EXPERT_DEFAULT) {
			combSpeedHardExpert.setSelectedIndex(0);
		} else if (theParent.noteSpeedHardExpert == EditSettingsOne.NOTE_SPEED_HARD_EXPERT_HYPERSPEED) {
			combSpeedHardExpert.setSelectedIndex(1);
		} else if (theParent.noteSpeedHardExpert == EditSettingsOne.NOTE_SPEED_HARD_EXPERT_ULTRASPEED) {
			combSpeedHardExpert.setSelectedIndex(2);
		} else {
			throw new Exception ("Error setting note speed");
		}
		easyMediumNoteSpeed = theParent.noteSpeedEasyMedium;
		hardExpertNoteSpeed = theParent.noteSpeedHardExpert;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			chkNeverFail = new JCheckBox();
			chkNeverFail.setBounds(new Rectangle(9, pos, 303, 19));
			chkNeverFail.setText("Enable 'Never Fail' Cheat");
			chkNeverFail.setFont(GuitarWizardMain.REGFONT);
			pos = pos + 25;

			lblSpeedEasyMedium = new JLabel();
			lblSpeedEasyMedium.setBounds(new Rectangle(9, pos, 143, 19));
			lblSpeedEasyMedium.setText("Easy/Medium Note Speed:");
			combSpeedEasyMedium = new JComboBox();
			combSpeedEasyMedium.setEditable(false);
			combSpeedEasyMedium.addItem("Default");
			combSpeedEasyMedium.addItem("Hyperspeed");
			combSpeedEasyMedium.addItem("Ultraspeed");
			combSpeedEasyMedium.setFont(GuitarWizardMain.REGFONT);
			combSpeedEasyMedium.setBounds(new Rectangle(155, pos, 104, 21));
			combSpeedEasyMedium.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					comboBoxChanged();
				}
			});
			pos = pos + 25;

			lblSpeedHardExpert = new JLabel();
			lblSpeedHardExpert.setBounds(new Rectangle(9, pos, 143, 19));
			lblSpeedHardExpert.setText("Hard/Expert Note Speed:");
			combSpeedHardExpert = new JComboBox();
			combSpeedHardExpert.setEditable(false);
			combSpeedHardExpert.addItem("Default");
			combSpeedHardExpert.addItem("Hyperspeed");
			combSpeedHardExpert.addItem("Ultraspeed");
			combSpeedHardExpert.setFont(GuitarWizardMain.REGFONT);
			combSpeedHardExpert.setBounds(new Rectangle(155, pos, 104, 21));
			combSpeedHardExpert.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					comboBoxChanged();
				}
			});
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(chkNeverFail, null);
			jContentPane.add(combSpeedHardExpert, null);
			jContentPane.add(lblSpeedHardExpert, null);
			jContentPane.add(combSpeedEasyMedium, null);
			jContentPane.add(lblSpeedEasyMedium, null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(140, 149, 74, 22));
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doCancel();
				}
			});
		}
		return btnCancel;
	}

	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setText("<html><h4>Save</h4></html>");
			btnSave.setBounds(new Rectangle(48, 149, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"