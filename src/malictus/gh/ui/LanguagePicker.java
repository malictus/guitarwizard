package malictus.gh.ui;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import malictus.gh.ui.editsettings.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * LanguagePicker
 *
 * Lets the user pick a language to use (when editing locale info) for PAL version of game
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class LanguagePicker extends JDialog {

	protected JPanel jContentPane = null;
	private JButton btnSave = null;
	private JLabel lblExplain = null;
	protected JComboBox combLanguage = null;

	public LanguagePicker(JDialog parent) throws Exception {
		super(parent);
		this.setSize(new Dimension(273, 104));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doSave() {
		this.setVisible(false);
	}

	public String getFileLoc() throws Exception {
		String selected = (String)combLanguage.getSelectedItem();
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			if (selected.equals("German (DEU)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_DEU;
			}
			if (selected.equals("English (ENG)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_ENG;
			}
			if (selected.equals("Spanish (ESL)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_ESL;
			}
			if (selected.equals("French (FRE)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_FRE;
			}
			if (selected.equals("Italian (ITA)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_ITA;
			}
			if (selected.equals("UK English")) {
				return EditSettingsOne.LOCALE_FILE_LOC_1_ENG;
			}
		} else {
			if (selected.equals("German (DEU)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_DEU;
			}
			if (selected.equals("English (ENG)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_ENG;
			}
			if (selected.equals("Spanish (ESL)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_ESL;
			}
			if (selected.equals("French (FRE)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_FRE;
			}
			if (selected.equals("Italian (ITA)")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_ITA;
			}
			if (selected.equals("UK English")) {
				return EditSettingsOne.LOCALE_FILE_LOC_2_80_ENG;
			}
		}
		throw new Exception("Language not found");
	}

	public String getFilename() throws Exception {
		String selected = (String)combLanguage.getSelectedItem();
		if (selected.equals("UK English")) {
			return EditSettingsOne.LOCALE_FILENAME_UK;
		} else {
			return EditSettingsOne.LOCALE_FILENAME;
		}
	}

	private void initialize() throws Exception {
		this.setTitle("Pick a Language");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() throws Exception {
		combLanguage.addItem("English (ENG)");
		combLanguage.addItem("German (DEU)");
		combLanguage.addItem("Spanish (ESL)");
		combLanguage.addItem("French (FRE)");
		combLanguage.addItem("Italian (ITA)");
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			if (GHUtils.fileExists(EditSettingsOne.LOCALE_FILENAME_UK, EditSettingsOne.LOCALE_FILE_LOC_1_ENG)) {
				combLanguage.addItem("UK English");
			}
		} else {
			if (GHUtils.fileExists(EditSettingsOne.LOCALE_FILENAME_UK, EditSettingsOne.LOCALE_FILE_LOC_2_80_ENG)) {
				combLanguage.addItem("UK English");
			}
		}
		combLanguage.setSelectedIndex(0);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblExplain = new JLabel();
			lblExplain.setFont(GuitarWizardMain.REGFONT);
			lblExplain.setBounds(new Rectangle(9, 12, 130, 22));
			lblExplain.setText("Choose a language:");
			combLanguage = new JComboBox();
			combLanguage.setFont(GuitarWizardMain.REGFONT);
			combLanguage.setBounds(new Rectangle(140, 12, 120, 22));
			combLanguage.setEditable(false);

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblExplain, null);
			jContentPane.add(combLanguage, null);
		}
		return jContentPane;
	}

	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setText("<html><h4>OK</h4></html>");
			btnSave.setBounds(new Rectangle(178, 50, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"