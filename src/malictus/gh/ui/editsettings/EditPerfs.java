package malictus.gh.ui.editsettings;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditPerfs
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditPerfs extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblPerfBy = null;
	protected JTextField txtfPerfBy = null;
	private JLabel lblFamousBy = null;
	protected JTextField txtfFamousBy = null;

	boolean canceled = false;

	EditSettingsOne theParent;

	private final static int PERF_LIMIT = 23;

	public EditPerfs(EditSettingsOne parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(283, 154));
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
		this.setTitle("Edit Performed/Made Famous By Text");
		this.setSize(new Dimension(283, 154));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		txtfPerfBy.setText(theParent.perfBy);
		txtfFamousBy.setText(theParent.famousBy);
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			txtfPerfBy.setEnabled(false);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblPerfBy = new JLabel();
			lblPerfBy.setBounds(new Rectangle(9, pos, 93, 19));
			lblPerfBy.setText("Performed By:");
			txtfPerfBy = new JTextField();
			txtfPerfBy.setDocument (new LimitedTextField(PERF_LIMIT));
			txtfPerfBy.setFont(GuitarWizardMain.REGFONT);
			txtfPerfBy.setBounds(new Rectangle(115, pos, 144, 21));
			pos = pos + 25;

			lblFamousBy = new JLabel();
			lblFamousBy.setBounds(new Rectangle(9, pos, 93, 19));
			lblFamousBy.setText("Made Famous By:");
			txtfFamousBy = new JTextField();
			txtfFamousBy.setDocument (new LimitedTextField(PERF_LIMIT));
			txtfFamousBy.setFont(GuitarWizardMain.REGFONT);
			txtfFamousBy.setBounds(new Rectangle(115, pos, 144, 21));
			pos = pos + 25;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblFamousBy, null);
			jContentPane.add(lblPerfBy, null);
			jContentPane.add(txtfFamousBy, null);
			jContentPane.add(txtfPerfBy, null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(140, 99, 74, 22));
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
			btnSave.setBounds(new Rectangle(48, 99, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"