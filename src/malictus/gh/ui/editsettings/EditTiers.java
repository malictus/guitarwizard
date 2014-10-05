package malictus.gh.ui.editsettings;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditTiers
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditTiers extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblTier1 = null;
	protected JTextField txtfTier1 = null;
	private JLabel lblTier2 = null;
	protected JTextField txtfTier2 = null;
	private JLabel lblTier3 = null;
	protected JTextField txtfTier3 = null;
	private JLabel lblTier4 = null;
	protected JTextField txtfTier4 = null;
	private JLabel lblTier5 = null;
	protected JTextField txtfTier5 = null;
	private JLabel lblTier6 = null;
	protected JTextField txtfTier6 = null;
	private JLabel lblTier7 = null;
	protected JTextField txtfTier7 = null;
	private JLabel lblTier8 = null;
	protected JTextField txtfTier8 = null;
	private JLabel lblTier9 = null;
	protected JTextField txtfTier9 = null;

	boolean canceled = false;

	EditSettingsOne theParent;

	private final static int TIER_LIMIT = 40;

	public EditTiers(EditSettingsOne parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(233, 324));
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
		this.setTitle("Edit Song Tiers");
		this.setSize(new Dimension(233, 324));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		txtfTier1.setText(theParent.tier1Name);
		txtfTier2.setText(theParent.tier2Name);
		txtfTier3.setText(theParent.tier3Name);
		txtfTier4.setText(theParent.tier4Name);
		txtfTier5.setText(theParent.tier5Name);
		txtfTier6.setText(theParent.tier6Name);
		txtfTier7.setText(theParent.tier7Name);
		txtfTier8.setText(theParent.tier8Name);
		txtfTier9.setText(theParent.tier9Name);
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			txtfTier8.setEnabled(false);
			txtfTier9.setEnabled(false);
		} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH80) {
			txtfTier7.setEnabled(false);
			txtfTier8.setEnabled(false);
			txtfTier9.setEnabled(false);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblTier1 = new JLabel();
			lblTier1.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier1.setText("Tier 1:");
			txtfTier1 = new JTextField();
			txtfTier1.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier1.setFont(GuitarWizardMain.REGFONT);
			txtfTier1.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier2 = new JLabel();
			lblTier2.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier2.setText("Tier 2:");
			txtfTier2 = new JTextField();
			txtfTier2.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier2.setFont(GuitarWizardMain.REGFONT);
			txtfTier2.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier3 = new JLabel();
			lblTier3.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier3.setText("Tier 3:");
			txtfTier3 = new JTextField();
			txtfTier3.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier3.setFont(GuitarWizardMain.REGFONT);
			txtfTier3.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier4 = new JLabel();
			lblTier4.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier4.setText("Tier 4:");
			txtfTier4 = new JTextField();
			txtfTier4.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier4.setFont(GuitarWizardMain.REGFONT);
			txtfTier4.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier5 = new JLabel();
			lblTier5.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier5.setText("Tier 5:");
			txtfTier5 = new JTextField();
			txtfTier5.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier5.setFont(GuitarWizardMain.REGFONT);
			txtfTier5.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier6 = new JLabel();
			lblTier6.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier6.setText("Tier 6:");
			txtfTier6 = new JTextField();
			txtfTier6.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier6.setFont(GuitarWizardMain.REGFONT);
			txtfTier6.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier7 = new JLabel();
			lblTier7.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier7.setText("Tier 7:");
			txtfTier7 = new JTextField();
			txtfTier7.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier7.setFont(GuitarWizardMain.REGFONT);
			txtfTier7.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier8 = new JLabel();
			lblTier8.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier8.setText("Tier 8:");
			txtfTier8 = new JTextField();
			txtfTier8.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier8.setFont(GuitarWizardMain.REGFONT);
			txtfTier8.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			lblTier9 = new JLabel();
			lblTier9.setBounds(new Rectangle(9, pos, 43, 19));
			lblTier9.setText("Tier 9:");
			txtfTier9 = new JTextField();
			txtfTier9.setDocument (new LimitedTextField(TIER_LIMIT));
			txtfTier9.setFont(GuitarWizardMain.REGFONT);
			txtfTier9.setBounds(new Rectangle(55, pos, 144, 21));
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblTier1, null);
			jContentPane.add(txtfTier1, null);
			jContentPane.add(lblTier2, null);
			jContentPane.add(txtfTier2, null);
			jContentPane.add(lblTier3, null);
			jContentPane.add(txtfTier3, null);
			jContentPane.add(lblTier4, null);
			jContentPane.add(txtfTier4, null);
			jContentPane.add(lblTier5, null);
			jContentPane.add(txtfTier5, null);
			jContentPane.add(lblTier6, null);
			jContentPane.add(txtfTier6, null);
			jContentPane.add(lblTier7, null);
			jContentPane.add(txtfTier7, null);
			jContentPane.add(lblTier8, null);
			jContentPane.add(txtfTier8, null);
			jContentPane.add(lblTier9, null);
			jContentPane.add(txtfTier9, null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(140, 269, 74, 22));
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
			btnSave.setBounds(new Rectangle(48, 269, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"