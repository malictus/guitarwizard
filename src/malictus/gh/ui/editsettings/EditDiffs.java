package malictus.gh.ui.editsettings;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditDiffs
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditDiffs extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblDiffEasy = null;
	protected JTextField txtfDiffEasy = null;
	private JLabel lblDiffMedium = null;
	protected JTextField txtfDiffMedium = null;
	private JLabel lblDiffHard = null;
	protected JTextField txtfDiffHard = null;
	private JLabel lblDiffExpert = null;
	protected JTextField txtfDiffExpert = null;

	boolean canceled = false;

	EditSettingsOne theParent;

	private final static int DIFF_LIMIT = 13;

	public EditDiffs(EditSettingsOne parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(233, 204));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		canceled = true;
		this.setVisible(false);
	}

	private void doSave() {
		canceled = false;
		this.setVisible(false);
	}

	private void initialize() {
		this.setTitle("Edit Difficulty Labels");
		this.setSize(new Dimension(233, 204));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		txtfDiffEasy.setText(theParent.diffEasy);
		txtfDiffMedium.setText(theParent.diffMedium);
		txtfDiffHard.setText(theParent.diffHard);
		txtfDiffExpert.setText(theParent.diffExpert);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblDiffEasy = new JLabel();
			lblDiffEasy.setBounds(new Rectangle(9, pos, 43, 19));
			lblDiffEasy.setText("Easy:");
			txtfDiffEasy = new JTextField();
			txtfDiffEasy.setDocument (new LimitedTextField(DIFF_LIMIT));
			txtfDiffEasy.setFont(GuitarWizardMain.REGFONT);
			txtfDiffEasy.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;
			lblDiffMedium = new JLabel();
			lblDiffMedium.setBounds(new Rectangle(9, pos, 43, 19));
			lblDiffMedium.setText("Medium:");
			txtfDiffMedium = new JTextField();
			txtfDiffMedium.setDocument (new LimitedTextField(DIFF_LIMIT));
			txtfDiffMedium.setFont(GuitarWizardMain.REGFONT);
			txtfDiffMedium.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;
			lblDiffHard = new JLabel();
			lblDiffHard.setBounds(new Rectangle(9, pos, 43, 19));
			lblDiffHard.setText("Hard:");
			txtfDiffHard = new JTextField();
			txtfDiffHard.setDocument (new LimitedTextField(DIFF_LIMIT));
			txtfDiffHard.setFont(GuitarWizardMain.REGFONT);
			txtfDiffHard.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;
			lblDiffExpert = new JLabel();
			lblDiffExpert.setBounds(new Rectangle(9, pos, 43, 19));
			lblDiffExpert.setText("Expert:");
			txtfDiffExpert = new JTextField();
			txtfDiffExpert.setDocument (new LimitedTextField(DIFF_LIMIT));
			txtfDiffExpert.setFont(GuitarWizardMain.REGFONT);
			txtfDiffExpert.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblDiffEasy, null);
			jContentPane.add(txtfDiffEasy, null);
			jContentPane.add(lblDiffMedium, null);
			jContentPane.add(txtfDiffMedium, null);
			jContentPane.add(lblDiffHard, null);
			jContentPane.add(txtfDiffHard, null);
			jContentPane.add(lblDiffExpert, null);
			jContentPane.add(txtfDiffExpert, null);
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