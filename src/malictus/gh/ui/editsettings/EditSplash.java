package malictus.gh.ui.editsettings;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditSplash
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSplash extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblSplashAnyButton = null;
	protected JTextField txtfSplashAnyButton = null;
	private JLabel lblSplashLegal1 = null;
	protected JTextField txtfSplashLegal1 = null;
	private JLabel lblSplashLegal2 = null;
	protected JTextField txtfSplashLegal2 = null;
	private JLabel lblSplashLegal3 = null;
	protected JTextField txtfSplashLegal3 = null;
	boolean canceled = false;

	EditSettingsOne theParent;

	private final static int SPLASH_ANYBUTT_LIMIT = 32;
	private final static int SPLASH_LEGAL_LIMIT = 173;

	public EditSplash(EditSettingsOne parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(833, 184));
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
		this.setTitle("Edit Splash Screen Text");
		this.setSize(new Dimension(833, 184));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		txtfSplashAnyButton.setText(theParent.splashAnyButton);
		txtfSplashLegal1.setText(theParent.splashLegal1);
		txtfSplashLegal2.setText(theParent.splashLegal2);
		txtfSplashLegal3.setText(theParent.splashLegal3);
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH80) {
			txtfSplashLegal2.setEnabled(false);
			txtfSplashLegal3.setEnabled(false);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblSplashAnyButton = new JLabel();
			lblSplashAnyButton.setBounds(new Rectangle(9, pos, 103, 19));
			lblSplashAnyButton.setText("ANY BUTTON Text:");
			txtfSplashAnyButton = new JTextField();
			txtfSplashAnyButton.setDocument (new LimitedTextField(SPLASH_ANYBUTT_LIMIT));
			txtfSplashAnyButton.setFont(GuitarWizardMain.REGFONT);
			txtfSplashAnyButton.setBounds(new Rectangle(110, pos, 694, 21));
			pos = pos + 25;

			lblSplashLegal1 = new JLabel();
			lblSplashLegal1.setBounds(new Rectangle(9, pos, 103, 19));
			lblSplashLegal1.setText("Legal Text Line 1:");
			txtfSplashLegal1 = new JTextField();
			txtfSplashLegal1.setDocument (new LimitedTextField(SPLASH_LEGAL_LIMIT));
			txtfSplashLegal1.setFont(GuitarWizardMain.REGFONT);
			txtfSplashLegal1.setBounds(new Rectangle(110, pos, 694, 21));
			pos = pos + 25;

			lblSplashLegal2 = new JLabel();
			lblSplashLegal2.setBounds(new Rectangle(9, pos, 103, 19));
			lblSplashLegal2.setText("Legal Text Line 2:");
			txtfSplashLegal2 = new JTextField();
			txtfSplashLegal2.setDocument (new LimitedTextField(SPLASH_LEGAL_LIMIT));
			txtfSplashLegal2.setFont(GuitarWizardMain.REGFONT);
			txtfSplashLegal2.setBounds(new Rectangle(110, pos, 694, 21));
			pos = pos + 25;

			lblSplashLegal3 = new JLabel();
			lblSplashLegal3.setBounds(new Rectangle(9, pos, 103, 19));
			lblSplashLegal3.setText("Legal Text Line 3:");
			txtfSplashLegal3 = new JTextField();
			txtfSplashLegal3.setDocument (new LimitedTextField(SPLASH_LEGAL_LIMIT));
			txtfSplashLegal3.setFont(GuitarWizardMain.REGFONT);
			txtfSplashLegal3.setBounds(new Rectangle(110, pos, 694, 21));
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblSplashAnyButton, null);
			jContentPane.add(txtfSplashAnyButton, null);
			jContentPane.add(lblSplashLegal1, null);
			jContentPane.add(txtfSplashLegal1, null);
			jContentPane.add(lblSplashLegal2, null);
			jContentPane.add(txtfSplashLegal2, null);
			jContentPane.add(lblSplashLegal3, null);
			jContentPane.add(txtfSplashLegal3, null);

		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(740, 129, 74, 22));
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
			btnSave.setBounds(new Rectangle(648, 129, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"