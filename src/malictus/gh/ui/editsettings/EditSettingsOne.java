package malictus.gh.ui.editsettings;

import javax.swing.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.io.*;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.*;

/**
 * EditSettingsOne
 * Step 1 in edit settings
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSettingsOne extends JDialog {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnApplyChanges = null;
	private JLabel lblInfo = null;
	private JLabel lblInfo2 = null;
	private JButton btnEditTier = null;
	private JButton btnDiffText = null;
	private JButton btnEditSplash = null;
	private JButton btnEditGameplayOpts = null;
	private JButton btnEditMainMenu = null;
	private JButton btnPerfBy = null;

	/******************* EDIT TIER ******************************/
	String tier1Name;
	String tier2Name;
	String tier3Name;
	String tier4Name;
	String tier5Name;
	String tier6Name;
	String tier7Name;
	String tier8Name;
	String tier9Name;

	public final static String tier1Key_GH2 = "song_header_battle";
	public final static String tier2Key_GH2 = "song_header_small1";
	public final static String tier3Key_GH2 = "song_header_small2";
	public final static String tier4Key_GH2 = "song_header_big";
	public final static String tier5Key_GH2 = "song_header_theatre";
	public final static String tier6Key_GH2 = "song_header_fest";
	public final static String tier7Key_GH2 = "song_header_arena";
	public final static String tier8Key_GH2 = "song_header_stone";
	public final static String tier9Key_GH2 = "song_header_store";

	public final static String tier1Key_GH1 = "song_header_basement";
	public final static String tier2Key_GH1 = "song_header_small_club";
	public final static String tier3Key_GH1 = "song_header_big_club";
	public final static String tier4Key_GH1 = "song_header_theatre";
	public final static String tier5Key_GH1 = "song_header_fest";
	public final static String tier6Key_GH1 = "song_header_arena";
	public final static String tier7Key_GH1 = "song_header_store";

	public final static String tier1Key_GH80 = "song_header_battle";
	public final static String tier2Key_GH80 = "song_header_small1";
	public final static String tier3Key_GH80 = "song_header_small2";
	public final static String tier4Key_GH80 = "song_header_theatre";
	public final static String tier5Key_GH80 = "song_header_fest";
	public final static String tier6Key_GH80 = "song_header_arena";

	boolean tiersEdited = false;

	/******************* EDIT DIFF TEXT **********************/
	String diffEasy;
	String diffMedium;
	String diffHard;
	String diffExpert;
	public final static String diffEasyKey_ALL = "easy";
	public final static String diffMediumKey_ALL = "medium";
	public final static String diffHardKey_ALL = "hard";
	public final static String diffExpertKey_ALL = "expert";
	boolean diffsEdited = false;

	/******************** EDIT SPLASH SCREEN TEXT ****************/
	String splashAnyButton;
	String splashLegal1;
	String splashLegal2;
	String splashLegal3;
	public final static String splashAnyButtonKey_2_80 = "splash_anybutton";
	public final static String splashLegalKey_2_80 = "splash_legal";
	boolean splashEdited = false;

	/******************** EDIT MAIN MENU TEXT *******************/
	String mainCareer;
	String mainQuickPlay;
	String mainMultiplay;
	String mainTraining;
	String mainOptions;
	public final static String mainCareerKey_ALL = "CAREER";
	public final static String mainQuickPlayKey_ALL = "QUICK_PLAY";
	public final static String mainMultiplayKey_ALL = "MULTIPLAYER";
	public final static String mainTrainingKey_2_80 = "TRAINING";
	public final static String mainTrainingKey_1 = "tutorials";
	public final static String mainOptionsKey_ALL = "OPTIONS";
	boolean mainMenuEdited = false;

	/******************** EDIT PEFORMED/FAMOUS BY TEXT *******************/
	String perfBy;
	String famousBy;
	public final static String perfByKey_2_80 = "performed_by";
	public final static String famousByKey_ALL = "mtv_made_famous";
	boolean perfByEdited = false;

	/******************* EDIT GAMEPLAY OPTS ********************/
	boolean neverFail;
	public final static String neverFailKey1_2_80 = "default_config/crowd/kDifficultyEasy/lose_level";
	public final static String neverFailKey2_2_80 = "default_config/crowd/kDifficultyMedium/lose_level";
	public final static String neverFailKey3_2_80 = "default_config/crowd/kDifficultyHard/lose_level";
	public final static String neverFailKey4_2_80 = "default_config/crowd/kDifficultyExpert/lose_level";
	public final static String neverFailKey5_2_80 = "coop_config/crowd/kDifficultyEasy/lose_level";
	public final static String neverFailKey6_2_80 = "coop_config/crowd/kDifficultyMedium/lose_level";
	public final static String neverFailKey7_2_80 = "coop_config/crowd/kDifficultyHard/lose_level";
	public final static String neverFailKey8_2_80 = "coop_config/crowd/kDifficultyExpert/lose_level";
	public final static String neverFailKey1_1 = "crowd/kDifficultyEasy/lose_level";
	public final static String neverFailKey2_1 = "crowd/kDifficultyMedium/lose_level";
	public final static String neverFailKey3_1 = "crowd/kDifficultyHard/lose_level";
	public final static String neverFailKey4_1 = "crowd/kDifficultyExpert/lose_level";
	public final static float neverFailDefaultValue = 0.01f;

	int noteSpeedEasyMedium;
	float noteSpeedHardExpert;

	public final static int NOTE_SPEED_EASY_MEDIUM_DEFAULT = 1;
	public final static int NOTE_SPEED_EASY_MEDIUM_HYPERSPEED = 2;
	public final static int NOTE_SPEED_EASY_MEDIUM_ULTRASPEED = 3;
	public final static float NOTE_SPEED_HARD_EXPERT_DEFAULT = 1.4f;
	public final static float NOTE_SPEED_HARD_EXPERT_HYPERSPEED = 2.1f;
	public final static float NOTE_SPEED_HARD_EXPERT_ULTRASPEED = 2.5f;
	public final static String NOTE_SPEED_EASY_KEY_ALL = "track_speed/kDifficultyEasy";
	public final static String NOTE_SPEED_MEDIUM_KEY_ALL = "track_speed/kDifficultyMedium";
	public final static String NOTE_SPEED_HARD_KEY_ALL = "track_speed/kDifficultyHard";
	public final static String NOTE_SPEED_EXPERT_KEY_ALL = "track_speed/kDifficultyExpert";
	boolean gameplayOptsEdited = false;

	/******************** DTB FILES ****************************/
	DTBFile locale;
	public final static String LOCALE_FILE_LOC_1_ENG = "ghui/eng/gen";
	public final static String LOCALE_FILE_LOC_1_DEU = "ghui/deu/gen";
	public final static String LOCALE_FILE_LOC_1_ESL = "ghui/esl/gen";
	public final static String LOCALE_FILE_LOC_1_FRE = "ghui/fre/gen";
	public final static String LOCALE_FILE_LOC_1_ITA = "ghui/ita/gen";
	public final static String LOCALE_FILE_LOC_2_80_ENG = "ui/eng/gen";
	public final static String LOCALE_FILE_LOC_2_80_DEU = "ui/deu/gen";
	public final static String LOCALE_FILE_LOC_2_80_ESL = "ui/esl/gen";
	public final static String LOCALE_FILE_LOC_2_80_FRE = "ui/fre/gen";
	public final static String LOCALE_FILE_LOC_2_80_ITA = "ui/ita/gen";
	public final static String LOCALE_FILENAME = "locale.dtb";
	public final static String LOCALE_FILENAME_UK = "uk_locale.dtb";
	String LOCALE_FILE_LOC_REAL;
	String LOCALE_FILENAME_REAL;

	DTBFile scoring;
	public final static String SCORING_FILE_LOC = "config/gen";
	public final static String SCORING_FILENAME = "scoring.dtb";

	DTBFile track_graphics;
	public final static String TRACK_GRAPHICS_FILE_LOC = "config/gen";
	public final static String TRACK_GRAPHICS_FILENAME = "track_graphics.dtb";

	GWMainWindow theParent;

	public EditSettingsOne(GWMainWindow parent) {
		super(parent);
		theParent = parent;
		this.setSize(new Dimension(341, 333));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void applyChanges() {
		this.setVisible(false);
		try {
			new EditSettingsTwo(this, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR making requested changes", "Error changing GH archive", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void populate() throws Exception {
		/********************* GAMEPLAY OPTS *****************/
		//read current values for neverfail; if at least one if 0, assume never fail is set to one
		try {
			scoring = new DTBFile(SCORING_FILENAME, SCORING_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error retrieving scoring.dtb file");
		}
		neverFail = false;
		Float val1 = new Float(0.01f);
		Float val2 = new Float(0.01f);
		Float val3 = new Float(0.01f);
		Float val4 = new Float(0.01f);
		Float val5 = new Float(0.01f);
		Float val6 = new Float(0.01f);
		Float val7 = new Float(0.01f);
		Float val8 = new Float(0.01f);
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				val1 = scoring.readFloat(neverFailKey1_1);
				val2 = scoring.readFloat(neverFailKey2_1);
				val3 = scoring.readFloat(neverFailKey3_1);
				val4 = scoring.readFloat(neverFailKey4_1);
			} else {
				val1 = scoring.readFloat(neverFailKey1_2_80);
				val2 = scoring.readFloat(neverFailKey2_2_80);
				val3 = scoring.readFloat(neverFailKey3_2_80);
				val4 = scoring.readFloat(neverFailKey4_2_80);
				val5 = scoring.readFloat(neverFailKey5_2_80);
				val6 = scoring.readFloat(neverFailKey6_2_80);
				val7 = scoring.readFloat(neverFailKey7_2_80);
				val8 = scoring.readFloat(neverFailKey8_2_80);
			}
		} catch (Exception err) {
			throw new Exception("Error reading never fail values");
		}
		if ( (val1.floatValue() == 0) || (val2.floatValue() == 0) || (val3.floatValue() == 0) || (val4.floatValue() == 0) ||
				(val5.floatValue() == 0) || (val6.floatValue() == 0) || (val7.floatValue() == 0) || (val8.floatValue() == 0) ) {
			neverFail = true;
		}
		//track speed
		try {
			track_graphics = new DTBFile(TRACK_GRAPHICS_FILENAME, TRACK_GRAPHICS_FILE_LOC);
		} catch (Exception err) {
			throw new Exception("Error reading track_graphics.dtb file");
		}
		//we'll base our defaults on the current Medium and Expert settings
		try {
			int speedmedium = track_graphics.readInt(NOTE_SPEED_MEDIUM_KEY_ALL).intValue();
			float speedexpert = track_graphics.readFloat(NOTE_SPEED_EXPERT_KEY_ALL).floatValue();
			if (speedmedium <= NOTE_SPEED_EASY_MEDIUM_DEFAULT) {
				noteSpeedEasyMedium = NOTE_SPEED_EASY_MEDIUM_DEFAULT;
			} else if (speedmedium <= NOTE_SPEED_EASY_MEDIUM_HYPERSPEED) {
				noteSpeedEasyMedium = NOTE_SPEED_EASY_MEDIUM_HYPERSPEED;
			} else {
				noteSpeedEasyMedium = NOTE_SPEED_EASY_MEDIUM_ULTRASPEED;
			}
			if (speedexpert <= NOTE_SPEED_HARD_EXPERT_DEFAULT) {
				noteSpeedHardExpert = NOTE_SPEED_HARD_EXPERT_DEFAULT;
			} else if (speedexpert <= NOTE_SPEED_HARD_EXPERT_HYPERSPEED) {
				noteSpeedHardExpert = NOTE_SPEED_HARD_EXPERT_HYPERSPEED;
			} else {
				noteSpeedHardExpert = NOTE_SPEED_HARD_EXPERT_ULTRASPEED;
			}
		} catch (Exception err) {
			throw new Exception("Error reading note speeds");
		}

		/****************** TIERS ***************/
		try {
			locale = new DTBFile(LOCALE_FILENAME_REAL, LOCALE_FILE_LOC_REAL);
		} catch (Exception err) {
			throw new Exception("Error reading locale.dtb");
		}
		//read tier names
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				tier1Name = locale.readString(tier1Key_GH1);
				tier2Name = locale.readString(tier2Key_GH1);
				tier3Name = locale.readString(tier3Key_GH1);
				tier4Name = locale.readString(tier4Key_GH1);
				tier5Name = locale.readString(tier5Key_GH1);
				tier6Name = locale.readString(tier6Key_GH1);
				tier7Name = locale.readString(tier7Key_GH1);
				tier8Name = "";
				tier9Name = "";
			} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
				tier1Name = locale.readString(tier1Key_GH2);
				tier2Name = locale.readString(tier2Key_GH2);
				tier3Name = locale.readString(tier3Key_GH2);
				tier4Name = locale.readString(tier4Key_GH2);
				tier5Name = locale.readString(tier5Key_GH2);
				tier6Name = locale.readString(tier6Key_GH2);
				tier7Name = locale.readString(tier7Key_GH2);
				tier8Name = locale.readString(tier8Key_GH2);
				tier9Name = locale.readString(tier9Key_GH2);
			} else {
				tier1Name = locale.readString(tier1Key_GH80);
				tier2Name = locale.readString(tier2Key_GH80);
				tier3Name = locale.readString(tier3Key_GH80);
				tier4Name = locale.readString(tier4Key_GH80);
				tier5Name = locale.readString(tier5Key_GH80);
				tier6Name = locale.readString(tier6Key_GH80);
				tier7Name = "";
				tier8Name = "";
				tier9Name = "";
			}
		} catch (Exception err) {
			throw new Exception("Error reading tier names");
		}
			
		/**************** DIFFICULTY TEXT *************/
		try {
			diffEasy = locale.readString(diffEasyKey_ALL);
			diffMedium = locale.readString(diffMediumKey_ALL);
			diffHard = locale.readString(diffHardKey_ALL);
			diffExpert = locale.readString(diffExpertKey_ALL);
		} catch (Exception err) {
			throw new Exception("Error reading difficulty text");
		}
			
		/**************** PERF BY *************/
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				perfBy = "";
			} else {
				perfBy = locale.readString(perfByKey_2_80);
			}
			famousBy = locale.readString(famousByKey_ALL);
		} catch (Exception err) {
			throw new Exception("Error reading perfby text");
		}

		/**************** SPLASH SCREEN TEXT ************/
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				splashAnyButton = "";
				splashLegal1 = "";
				splashLegal2 = "";
				splashLegal3 = "";
			} else {
				splashAnyButton = locale.readString(splashAnyButtonKey_2_80);
				String wholeSplash = locale.readString(splashLegalKey_2_80);
				String[] splosh = wholeSplash.split(GuitarWizardMain.LINEBREAKSTRING);
				splashLegal1 = " ";
				splashLegal2 = " ";
				splashLegal3 = " ";
				if (splosh.length > 0) {
					splashLegal1 = splosh[0];
				}
				if (splosh.length > 1) {
					splashLegal2 = splosh[1];
				}
				if (splosh.length > 2) {
					splashLegal3 = splosh[2];
				}
			}
		} catch (Exception err) {
			throw new Exception("Error reading splash screen text");
		}

		/******************* MAIN MENU TEXT ***********/
		try {
			mainCareer = locale.readString(mainCareerKey_ALL);
			mainQuickPlay = locale.readString(mainQuickPlayKey_ALL);
			mainMultiplay = locale.readString(mainMultiplayKey_ALL);
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				mainTraining = locale.readString(mainTrainingKey_1);
			} else {
				mainTraining = locale.readString(mainTrainingKey_2_80);
			}
			mainOptions = locale.readString(mainOptionsKey_ALL);
		} catch (Exception err) {
			throw new Exception("Error reading main menu text");
		}
	}

	private void doEditPerfBy() {
		EditPerfs ep = new EditPerfs(this);
		if (!ep.canceled) {
			perfBy = ep.txtfPerfBy.getText();
			famousBy = ep.txtfFamousBy.getText();
			if (perfBy.equals("")) {
				perfBy = " ";
			}
			if (famousBy.equals("")) {
				famousBy = " ";
			}
			perfByEdited = true;
		}
	}

	private void doEditDiffs() {
		EditDiffs ed = new EditDiffs(this);
		if (!ed.canceled) {
			diffEasy = ed.txtfDiffEasy.getText();
			diffMedium = ed.txtfDiffMedium.getText();
			diffHard = ed.txtfDiffHard.getText();
			diffExpert = ed.txtfDiffExpert.getText();
			if (diffEasy.equals("")) {
				diffEasy = " ";
			}
			if (diffMedium.equals("")) {
				diffMedium = " ";
			}
			if (diffHard.equals("")) {
				diffHard = " ";
			}
			if (diffExpert.equals("")) {
				diffExpert = " ";
			}
			diffsEdited = true;
		}
	}

	private void doEditGameplayOpts() {
		try {
			EditGameplayOpts eg = new EditGameplayOpts(this);
			if (!eg.canceled) {
				neverFail = eg.chkNeverFail.isSelected();
				noteSpeedHardExpert = eg.getNewHardExpertVal();
				noteSpeedEasyMedium = eg.getNewEasyMediumVal();
				gameplayOptsEdited = true;
			}
		}	catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "ERROR changing DTB for gameplay", "Error changing gameplay options", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void doEditMainMenu() {
		EditMainMenu em = new EditMainMenu(this);
		if (!em.canceled) {
			mainCareer = em.txtfCareer.getText();
			mainQuickPlay = em.txtfQuickPlay.getText();
			mainMultiplay = em.txtfMultiplay.getText();
			mainTraining = em.txtfTraining.getText();
			mainOptions = em.txtfOptions.getText();
			if (mainCareer.equals("")) {
				mainCareer = " ";
			}
			if (mainQuickPlay.equals("")) {
				mainQuickPlay = " ";
			}
			if (mainMultiplay.equals("")) {
				mainMultiplay = " ";
			}
			if (mainTraining.equals("")) {
				mainTraining = " ";
			}
			if (mainOptions.equals("")) {
				mainOptions = " ";
			}
			mainMenuEdited = true;
		}
	}

	private void doEditSplash() {
		EditSplash es = new EditSplash(this);
		if (!es.canceled) {
			splashAnyButton = es.txtfSplashAnyButton.getText();
			splashLegal1 = es.txtfSplashLegal1.getText();
			splashLegal2 = es.txtfSplashLegal2.getText();
			splashLegal3 = es.txtfSplashLegal3.getText();
			if (splashAnyButton.equals("")) {
				splashAnyButton = " ";
			}
			if (splashLegal1.equals("")) {
				splashLegal1 = " ";
			}
			if (splashLegal2.equals("")) {
				splashLegal2 = " ";
			}
			if (splashLegal3.equals("")) {
				splashLegal3 = " ";
			}
			splashEdited = true;
		}
	}

	private void doEditTiers() {
		EditTiers et = new EditTiers(this);
		if (!et.canceled) {
			tier1Name = et.txtfTier1.getText();
			tier2Name = et.txtfTier2.getText();
			tier3Name = et.txtfTier3.getText();
			tier4Name = et.txtfTier4.getText();
			tier5Name = et.txtfTier5.getText();
			tier6Name = et.txtfTier6.getText();
			tier7Name = et.txtfTier7.getText();
			tier8Name = et.txtfTier8.getText();
			tier9Name = et.txtfTier9.getText();
			if (tier1Name.equals("")) {
				tier1Name = " ";
			}
			if (tier2Name.equals("")) {
				tier2Name = " ";
			}
			if (tier3Name.equals("")) {
				tier3Name = " ";
			}
			if (tier4Name.equals("")) {
				tier4Name = " ";
			}
			if (tier5Name.equals("")) {
				tier5Name = " ";
			}
			if (tier6Name.equals("")) {
				tier6Name = " ";
			}
			if (tier7Name.equals("")) {
				tier7Name = " ";
			}
			if (tier8Name.equals("")) {
				tier8Name = " ";
			}
			if (tier9Name.equals("")) {
				tier9Name = " ";
			}
			tiersEdited = true;
		}
	}

	private void disableWindow() {
		this.btnCancel.setEnabled(false);
		btnApplyChanges.setForeground((Color) UIManager.get("Label.disabledForeground"));
        this.btnApplyChanges.setEnabled(false);
        lblInfo.setText("Loading: Please wait...");
        lblInfo2.setText("");
        btnEditTier.setEnabled(false);
        btnDiffText.setEnabled(false);
        btnEditSplash.setEnabled(false);
        btnEditGameplayOpts.setEnabled(false);
        btnEditMainMenu.setEnabled(false);
        btnPerfBy.setEnabled(false);
	}

	private void enableWindow() {
		this.btnCancel.setEnabled(true);
        this.btnApplyChanges.setEnabled(true);
        btnApplyChanges.setForeground((Color) UIManager.get("Label.enabledForeground"));
        lblInfo.setText("<html><b>Edit Game Text</b></html>");
        lblInfo2.setText("<html><b>Edit Game Settings</b></html>");
        btnEditTier.setEnabled(true);
        btnDiffText.setEnabled(true);
        if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
        	btnEditSplash.setEnabled(true);
        }
        btnEditGameplayOpts.setEnabled(true);
        btnEditMainMenu.setEnabled(true);
        btnPerfBy.setEnabled(true);
	}

	private void initialize() {
		this.setTitle("Edit GH Settings");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        final EditSettingsOne x = this;
        disableWindow();
        //check for language support
        try {
        	//check version first, and existence of non-English languages
        	boolean showLanguageDialog = true;
        	if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
        		if ( !(GHUtils.fileExists(LOCALE_FILENAME, LOCALE_FILE_LOC_2_80_DEU)) ) {
        			showLanguageDialog = false;
        		}
        	} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
        		if ( !(GHUtils.fileExists(LOCALE_FILENAME, LOCALE_FILE_LOC_1_DEU)) ) {
        			showLanguageDialog = false;
        		}
        	}
        	if (showLanguageDialog) {
	        	LanguagePicker lp = new LanguagePicker(this);
	        	LOCALE_FILE_LOC_REAL = lp.getFileLoc();
	        	LOCALE_FILENAME_REAL = lp.getFilename();
        	} else {
        		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
        			LOCALE_FILE_LOC_REAL = LOCALE_FILE_LOC_1_ENG;
        		} else {
        			LOCALE_FILE_LOC_REAL = LOCALE_FILE_LOC_2_80_ENG;
        		}
	        	LOCALE_FILENAME_REAL = LOCALE_FILENAME;
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

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			lblInfo = new JLabel();
			lblInfo.setBounds(new Rectangle(45, 5, 200, 20));
			lblInfo.setFont(GuitarWizardMain.REGFONT);
			lblInfo.setText("Loading: Please wait...");
			lblInfo.setFont(GuitarWizardMain.REGFONT);
			lblInfo2 = new JLabel();
			lblInfo2.setBounds(new Rectangle(195, 5, 200, 20));
			lblInfo2.setFont(GuitarWizardMain.REGFONT);
			lblInfo2.setText("");
			lblInfo2.setFont(GuitarWizardMain.REGFONT);
			jContentPane.add(lblInfo, null);
			jContentPane.add(lblInfo2, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnApplyChanges(), null);
			jContentPane.add(getBtnEditTier(), null);
			jContentPane.add(getBtnDiffText(), null);
			jContentPane.add(getBtnEditSplash(), null);
			jContentPane.add(getBtnEditMainMenu(), null);
			jContentPane.add(getBtnEditGameplayOpts(), null);
			jContentPane.add(getBtnPerfBy(), null);
		}
		return jContentPane;
	}

	private JButton getBtnEditTier() {
		if (btnEditTier == null) {
			btnEditTier = new JButton();
			btnEditTier.setBounds(new Rectangle(15, 30, 150, 22));
			btnEditTier.setFont(GuitarWizardMain.REGFONT);
			btnEditTier.setText("Song Tiers");
			btnEditTier.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditTiers();
				}
			});
		}
		return btnEditTier;
	}

	private JButton getBtnDiffText() {
		if (btnDiffText == null) {
			btnDiffText = new JButton();
			btnDiffText.setFont(GuitarWizardMain.REGFONT);
			btnDiffText.setBounds(new Rectangle(15, 60, 150, 22));
			btnDiffText.setText("Difficulty Labels");
			btnDiffText.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditDiffs();
				}
			});
		}
		return btnDiffText;
	}

	private JButton getBtnEditSplash() {
		if (btnEditSplash == null) {
			btnEditSplash = new JButton();
			btnEditSplash.setFont(GuitarWizardMain.REGFONT);
			btnEditSplash.setBounds(new Rectangle(15, 90, 150, 22));
			btnEditSplash.setText("Splash Screen Text");
			btnEditSplash.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditSplash();
				}
			});
		}
		return btnEditSplash;
	}

	private JButton getBtnEditMainMenu() {
		if (btnEditMainMenu == null) {
			btnEditMainMenu = new JButton();
			btnEditMainMenu.setFont(GuitarWizardMain.REGFONT);
			btnEditMainMenu.setBounds(new Rectangle(15, 120, 150, 22));
			btnEditMainMenu.setText("Main Menu Text");
			btnEditMainMenu.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditMainMenu();
				}
			});
		}
		return btnEditMainMenu;
	}

	private JButton getBtnPerfBy() {
		if (btnPerfBy == null) {
			btnPerfBy = new JButton();
			btnPerfBy.setFont(GuitarWizardMain.REGFONT);
			btnPerfBy.setBounds(new Rectangle(15, 150, 150, 22));
			btnPerfBy.setText("'Performed By' Text");
			btnPerfBy.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditPerfBy();
				}
			});
		}
		return btnPerfBy;
	}

	private JButton getBtnEditGameplayOpts() {
		if (btnEditGameplayOpts == null) {
			btnEditGameplayOpts = new JButton();
			btnEditGameplayOpts.setFont(GuitarWizardMain.REGFONT);
			btnEditGameplayOpts.setBounds(new Rectangle(175, 30, 150, 22));
			btnEditGameplayOpts.setText("Gameplay Options");
			btnEditGameplayOpts.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doEditGameplayOpts();
				}
			});
		}
		return btnEditGameplayOpts;
	}

	private JButton getBtnApplyChanges() {
		if (btnApplyChanges == null) {
			btnApplyChanges = new JButton();
			btnApplyChanges.setText("<html><h4>Apply Changes</h4></html>");
			btnApplyChanges.setBounds(new Rectangle(102, 249, 114, 47));
			btnApplyChanges.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					applyChanges();
				}
			});
		}
		return btnApplyChanges;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setFont(GuitarWizardMain.REGFONT);
			btnCancel.setBounds(new Rectangle(254, 273, 74, 22));
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doCancel();
				}
			});
		}
		return btnCancel;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"