package malictus.gh.ui.editsettings;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditMainMenu
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditMainMenu extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblCareer = null;
	protected JTextField txtfCareer = null;
	private JLabel lblQuickPlay = null;
	protected JTextField txtfQuickPlay = null;
	private JLabel lblMultiplay = null;
	protected JTextField txtfMultiplay = null;
	private JLabel lblTraining = null;
	protected JTextField txtfTraining = null;
	private JLabel lblOptions = null;
	protected JTextField txtfOptions = null;

	boolean canceled = false;

	EditSettingsOne theParent;

	private final static int MENU_LIMIT = 16;

	public EditMainMenu(EditSettingsOne parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(253, 264));
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
		this.setTitle("Edit Main Menu Text");
		this.setSize(new Dimension(253, 264));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		txtfCareer.setText(theParent.mainCareer);
		txtfQuickPlay.setText(theParent.mainQuickPlay);
		txtfMultiplay.setText(theParent.mainMultiplay);
		txtfTraining.setText(theParent.mainTraining);
		txtfOptions.setText(theParent.mainOptions);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblCareer = new JLabel();
			lblCareer.setBounds(new Rectangle(9, pos, 63, 19));
			lblCareer.setText("Career:");
			txtfCareer = new JTextField();
			txtfCareer.setDocument (new LimitedTextField(MENU_LIMIT));
			txtfCareer.setFont(GuitarWizardMain.REGFONT);
			txtfCareer.setBounds(new Rectangle(75, pos, 154, 21));
			pos = pos + 25;

			lblQuickPlay = new JLabel();
			lblQuickPlay.setBounds(new Rectangle(9, pos, 63, 19));
			lblQuickPlay.setText("Quick Play:");
			txtfQuickPlay = new JTextField();
			txtfQuickPlay.setDocument (new LimitedTextField(MENU_LIMIT));
			txtfQuickPlay.setFont(GuitarWizardMain.REGFONT);
			txtfQuickPlay.setBounds(new Rectangle(75, pos, 154, 21));
			pos = pos + 25;

			lblMultiplay = new JLabel();
			lblMultiplay.setBounds(new Rectangle(9, pos, 63, 19));
			lblMultiplay.setText("Multiplay:");
			txtfMultiplay = new JTextField();
			txtfMultiplay.setDocument (new LimitedTextField(MENU_LIMIT));
			txtfMultiplay.setFont(GuitarWizardMain.REGFONT);
			txtfMultiplay.setBounds(new Rectangle(75, pos, 154, 21));
			pos = pos + 25;

			lblTraining = new JLabel();
			lblTraining.setBounds(new Rectangle(9, pos, 63, 19));
			lblTraining.setText("Training:");
			txtfTraining = new JTextField();
			txtfTraining.setDocument (new LimitedTextField(MENU_LIMIT));
			txtfTraining.setFont(GuitarWizardMain.REGFONT);
			txtfTraining.setBounds(new Rectangle(75, pos, 154, 21));
			pos = pos + 25;

			lblOptions = new JLabel();
			lblOptions.setBounds(new Rectangle(9, pos, 63, 19));
			lblOptions.setText("Options:");
			txtfOptions = new JTextField();
			txtfOptions.setDocument (new LimitedTextField(MENU_LIMIT));
			txtfOptions.setFont(GuitarWizardMain.REGFONT);
			txtfOptions.setBounds(new Rectangle(75, pos, 154, 21));
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblCareer, null);
			jContentPane.add(txtfCareer, null);
			jContentPane.add(lblQuickPlay, null);
			jContentPane.add(txtfQuickPlay, null);
			jContentPane.add(lblMultiplay, null);
			jContentPane.add(txtfMultiplay, null);
			jContentPane.add(lblTraining, null);
			jContentPane.add(txtfTraining, null);
			jContentPane.add(lblOptions, null);
			jContentPane.add(txtfOptions, null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(140, 209, 74, 22));
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
			btnSave.setBounds(new Rectangle(48, 209, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"