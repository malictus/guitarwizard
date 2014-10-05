package malictus.gh.ui.editsong;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.io.*;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.editsettings.*;
import malictus.gh.ui.*;

/**
 * EditSongOne
 * Step 1 in edit song
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongOne extends JDialog {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnNext = null;
	private JLabel lblLoading = null;
	private JComboBox combSongs = null;

	/******************** DTB FILES ****************************/
	DTBFile locale;
	String LOCALE_FILE_LOC_REAL;
	String LOCALE_FILENAME_REAL;

	DTBFile store;
	final static String STORE_FILE_LOC = "config/gen";
	final static String STORE_FILENAME = "store.dtb";

	DTBFile songs;
	final static public String SONGS_FILE_LOC = "config/gen";
	final static String SONGS_FILENAME = "songs.dtb";

	DTBFile campaign;
	final static String CAMPAIGN_FILE_LOC = "config/gen";
	final static String CAMPAIGN_FILENAME = "campaign.dtb";

	DTBFile tips;
	final static String TIPS_FILE_LOC = "config/gen";
	final static String TIPS_FILENAME = "tips.dtb";

	/******************** READ-ONLY VALS *************/
	String tier1Name;
	String tier2Name;
	String tier3Name;
	String tier4Name;
	String tier5Name;
	String tier6Name;
	String tier7Name;
	String tier8Name;
	String tier9Name;
	String perfByText;
	String famousByText;

	//keys are number (order) values in combo box; values are song ids
	Hashtable songHash = new Hashtable();

	/******************** CHANGING VALS ***************/
	String selectedSongID = "";
	String newSongTitle = "";
	String newArtistName = "";
	boolean isPerfBy = false;
	int newPrice = -1;
	String newStoreDesc = "";
	boolean hasNewStoreDesc = false;
	String bandConfig = "";
	String loadingTip = "";
	int startTime = 0;
	int endTime = 0;
	boolean hasNewVGSFile = false;
	boolean hasNewMIDFile = false;

	String vgsLocs;
	File vgsFile = null;
	File vgsFileCoop = null;
	File midiFile = null;
	String midLocs = null;

	GWMainWindow theParent;

	public EditSongOne(GWMainWindow parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(341, 163));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doNext() {
		selectedSongID = (String)songHash.get(new Integer(combSongs.getSelectedIndex()));
		this.setVisible(false);
		try {
			new EditSongTwo(this, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to step 2", "Error going to step 2", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void populate() throws Exception {
		try {
			locale = new DTBFile(LOCALE_FILENAME_REAL, LOCALE_FILE_LOC_REAL);
		} catch (Exception err) {
			throw new Exception("Error reading locale.dtb");
		}
		try {
			songs = new DTBFile(SONGS_FILENAME, SONGS_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error reading songs.dtb");
		}
		try {
			store = new DTBFile(STORE_FILENAME, STORE_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error reading store.dtb");
		}
		try {
			campaign = new DTBFile(CAMPAIGN_FILENAME, CAMPAIGN_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error reading campaign.dtb");
		}
		try {
			tips = new DTBFile(TIPS_FILENAME, TIPS_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error reading tips.dtb");
		}

		//misc text we'll use need later
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				perfByText = "";
			} else {
				perfByText = locale.readString(EditSettingsOne.perfByKey_2_80);
			}
			famousByText = locale.readString(EditSettingsOne.famousByKey_ALL);
		} catch (Exception err) {
			throw new Exception("Error reading perfby text");
		}

		//read tiers (read-only here)
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				tier1Name = locale.readString(EditSettingsOne.tier1Key_GH1);
				tier2Name = locale.readString(EditSettingsOne.tier2Key_GH1);
				tier3Name = locale.readString(EditSettingsOne.tier3Key_GH1);
				tier4Name = locale.readString(EditSettingsOne.tier4Key_GH1);
				tier5Name = locale.readString(EditSettingsOne.tier5Key_GH1);
				tier6Name = locale.readString(EditSettingsOne.tier6Key_GH1);
				tier7Name = locale.readString(EditSettingsOne.tier7Key_GH1);
				tier8Name = "";
				tier9Name = "";
			} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
				tier1Name = locale.readString(EditSettingsOne.tier1Key_GH2);
				tier2Name = locale.readString(EditSettingsOne.tier2Key_GH2);
				tier3Name = locale.readString(EditSettingsOne.tier3Key_GH2);
				tier4Name = locale.readString(EditSettingsOne.tier4Key_GH2);
				tier5Name = locale.readString(EditSettingsOne.tier5Key_GH2);
				tier6Name = locale.readString(EditSettingsOne.tier6Key_GH2);
				tier7Name = locale.readString(EditSettingsOne.tier7Key_GH2);
				tier8Name = locale.readString(EditSettingsOne.tier8Key_GH2);
				tier9Name = locale.readString(EditSettingsOne.tier9Key_GH2);
			} else {
				tier1Name = locale.readString(EditSettingsOne.tier1Key_GH80);
				tier2Name = locale.readString(EditSettingsOne.tier2Key_GH80);
				tier3Name = locale.readString(EditSettingsOne.tier3Key_GH80);
				tier4Name = locale.readString(EditSettingsOne.tier4Key_GH80);
				tier5Name = locale.readString(EditSettingsOne.tier5Key_GH80);
				tier6Name = locale.readString(EditSettingsOne.tier6Key_GH80);
				tier7Name = "";
				tier8Name = "";
				tier9Name = "";
			}
		} catch (Exception err) {
			throw new Exception("Error reading tiers");
		}

		this.combSongs.addItem("Select a song...");

		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				this.combSongs.addItem("      " + tier1Name);
				populateCombSongsFor("order/basement");
				this.combSongs.addItem("      " + tier2Name);
				populateCombSongsFor("order/small_club");
				this.combSongs.addItem("      " + tier3Name);
				populateCombSongsFor("order/big_club");
				this.combSongs.addItem("      " + tier4Name);
				populateCombSongsFor("order/theatre");
				this.combSongs.addItem("      " + tier5Name);
				populateCombSongsFor("order/fest");
				this.combSongs.addItem("      " + tier6Name);
				populateCombSongsFor("order/arena");
				this.combSongs.addItem("      " + tier7Name);
				Vector bonustier = store.getChildNodesFor("song");
				int counter = 0;
				while (counter < bonustier.size()) {
					if (bonustier.get(counter) instanceof String) {
						combSongs.addItem(getSongTitleFor((String)bonustier.get(counter)));
						songHash.put(new Integer(combSongs.getItemCount() - 1), (String)bonustier.get(counter));
					} else {
						throw new Exception("Unexpected non-string value found.");
					}
					counter = counter + 1;
				}

			} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
				this.combSongs.addItem("      " + tier1Name);
				populateCombSongsFor("order/battle");
				this.combSongs.addItem("      " + tier2Name);
				populateCombSongsFor("order/small1");
				this.combSongs.addItem("      " + tier3Name);
				populateCombSongsFor("order/small2");
				this.combSongs.addItem("      " + tier4Name);
				populateCombSongsFor("order/big");
				this.combSongs.addItem("      " + tier5Name);
				populateCombSongsFor("order/theatre");
				this.combSongs.addItem("      " + tier6Name);
				populateCombSongsFor("order/fest");
				this.combSongs.addItem("      " + tier7Name);
				populateCombSongsFor("order/arena");
				this.combSongs.addItem("      " + tier8Name);
				populateCombSongsFor("order/stone");
				this.combSongs.addItem("      " + tier9Name);
				Vector bonustier = store.getChildNodesFor("song");
				int counter = 0;
				while (counter < bonustier.size()) {
					if (bonustier.get(counter) instanceof String) {
						combSongs.addItem(getSongTitleFor((String)bonustier.get(counter)));
						songHash.put(new Integer(combSongs.getItemCount() - 1), (String)bonustier.get(counter));
					} else {
						throw new Exception("Unexpected non-string value found.");
					}
					counter = counter + 1;
				}

			} else {
				this.combSongs.addItem("      " + tier1Name);
				populateCombSongsFor("order/battle");
				this.combSongs.addItem("      " + tier2Name);
				populateCombSongsFor("order/small1");
				this.combSongs.addItem("      " + tier3Name);
				populateCombSongsFor("order/small2");
				this.combSongs.addItem("      " + tier4Name);
				populateCombSongsFor("order/theatre");
				this.combSongs.addItem("      " + tier5Name);
				populateCombSongsFor("order/fest");
				this.combSongs.addItem("      " + tier6Name);
				populateCombSongsFor("order/arena");
			}
			this.combSongs.setMaximumRowCount(20);
		} catch (Exception err) {
			throw new Exception("Error creating song list");
		}
	}

	private void populateCombSongsFor(String tierName) throws Exception {
		Vector tier = campaign.readVector(tierName);
		int counter = 0;
		while (counter < tier.size()) {
			DTBValue val = (DTBValue)tier.get(counter);
			if (val.getValue() instanceof String) {
				combSongs.addItem(getSongTitleFor((String)val.getValue()));
				songHash.put(new Integer(combSongs.getItemCount() - 1), (String)val.getValue());
			} else {
				throw new Exception("Unexpected non-string value found.");
			}
			counter = counter + 1;
		}
	}

	String getSongTitleFor(String songID) throws Exception {
		return songs.readString(songID + "/name");
	}

	private void disableWindow() {
		this.btnCancel.setEnabled(false);
		btnNext.setForeground((Color) UIManager.get("Label.disabledForeground"));
        this.btnNext.setEnabled(false);
        this.combSongs.setEnabled(false);
        lblLoading.setText("Loading: Please wait...");
	}

	private void enableWindow() {
		this.btnCancel.setEnabled(true);
        this.combSongs.setEnabled(true);
        lblLoading.setText("Select a song to modify or replace:");
	}

	private void initialize() {
		this.setTitle("Edit/Replace Song");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        final EditSongOne x = this;
        disableWindow();
        try {
        	//language check
        	boolean showLanguageDialog = true;
        	if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
        		if ( !(GHUtils.fileExists(EditSettingsOne.LOCALE_FILENAME, EditSettingsOne.LOCALE_FILE_LOC_2_80_DEU)) ) {
        			showLanguageDialog = false;
        		}
        	} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
        		if ( !(GHUtils.fileExists(EditSettingsOne.LOCALE_FILENAME, EditSettingsOne.LOCALE_FILE_LOC_1_DEU)) ) {
        			showLanguageDialog = false;
        		}
        	}
        	if (showLanguageDialog) {
	        	LanguagePicker lp = new LanguagePicker(this);
	        	LOCALE_FILE_LOC_REAL = lp.getFileLoc();
	        	LOCALE_FILENAME_REAL = lp.getFilename();
        	} else {
        		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
        			LOCALE_FILE_LOC_REAL = EditSettingsOne.LOCALE_FILE_LOC_1_ENG;
        		} else {
        			LOCALE_FILE_LOC_REAL = EditSettingsOne.LOCALE_FILE_LOC_2_80_ENG;
        		}
	        	LOCALE_FILENAME_REAL = EditSettingsOne.LOCALE_FILENAME;
        	}
        } catch (Exception err) {
        	err.printStackTrace();
        	JOptionPane.showMessageDialog(x, "ERROR setting language", "Error setting language", JOptionPane.ERROR_MESSAGE);
        	GHUtils.cleanupTempDir();
        	x.setVisible(false);
        	return;
        }
        Runnable q = new Runnable() {
            public void run() {
            	try {
                	populate();
                } catch (Exception err) {
                	err.printStackTrace();
                	JOptionPane.showMessageDialog(x, "ERROR reading DTB files:\n" + err.getMessage(), "Error reading DTB files", JOptionPane.ERROR_MESSAGE);
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

	private void doSongListChange() {
		if (songHash.containsKey(new Integer(combSongs.getSelectedIndex()))) {
			this.btnNext.setEnabled(true);
			btnNext.setForeground((Color) UIManager.get("Label.enabledForeground"));

		} else {
			this.btnNext.setEnabled(false);
			btnNext.setForeground((Color) UIManager.get("Label.disabledForeground"));
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			lblLoading = new JLabel();
			lblLoading.setBounds(new Rectangle(5, 5, 200, 20));
			lblLoading.setFont(GuitarWizardMain.REGFONT);
			lblLoading.setText("Loading: Please wait...");

			int pos = 35;
			combSongs = new JComboBox();
			combSongs.setBounds(new Rectangle(10, pos, 313, 19));
			combSongs.setFont(GuitarWizardMain.REGFONT);
			combSongs.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSongListChange();
				}
			});
			pos = pos + 23;

			jContentPane.add(lblLoading, null);
			jContentPane.add(combSongs, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(254, 103, 74, 22));
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
			btnNext.setBounds(new Rectangle(102, 79, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"