package malictus.gh.ui.editsong;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.io.*;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.audio.*;
import malictus.gh.ui.editsettings.EditSettingsOne;

/**
 * EditSongSix
 * Step 6 in edit song
 * Edit song settings
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongSix extends JDialog implements PlayerUI {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnNext = null;
	private JLabel lblLoading = null;

	private JLabel lblSongTitle = null;
	private JTextField txtfSongTitle = null;
	private JLabel lblArtistName = null;
	private JTextField txtfArtistName = null;
	private JComboBox combPerfBy = null;
	private JLabel lblPrice = null;
	private JTextField txtfPrice = null;
	private JLabel lblStoreDesc = null;
	private JTextField txtfStoreDesc = null;
	private JLabel lblBandConfig = null;
	private JComboBox combBandConfig = null;
	private JLabel lblLoadingTip = null;
	private JTextField txtfLoadingTip = null;
	private JLabel lblPreview = null;
	private JButton btnPlayPreview = null;
	private JButton btnChangePreview = null;

	private AudioPlayer player = null;

	GWMainWindow theParent;
	EditSongOne esong1;

	final static int SONG_TITLE_LIMIT = 50;
	final static int ARTIST_NAME_LIMIT = 50;
	final static int STORE_DESC_LIMIT = 265;
	final static int LOADING_TIP_LIMIT = 265;

	final static String BAND_FEMALE = "Female singer";
	final static String BAND_MALE = "Male singer";
	final static String BAND_NONE = "No singer";
	final static String BAND_KEYS = "No singer - add keyboards";

	boolean hasStorePrice = false;
	boolean hasStoreDesc = false;

	int duration = -1;
	int startTime = -1;
	int endTime = -1;

	public EditSongSix(EditSongOne editsongone, GWMainWindow parent) {
		super(parent);
		theParent = parent;
		esong1 = editsongone;
		this.setSize(new Dimension(742, 324));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		try {
			player.stop();
			player.discard();
		} catch(Exception err) {
			err.printStackTrace();
		}
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doNext() {
		int price = -1;
		if (this.hasStorePrice) {
			//verify store price is a usable value
			String priceString = this.txtfPrice.getText();
			price = -1;
			if (priceString.equals("")) {
				price = 0;
			} else {
				try {
					price = Integer.parseInt(priceString);
				} catch (Exception err) {
					JOptionPane.showMessageDialog(this, "Incorrect store price value", "Incorrect store price value", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (price < 0) {
					JOptionPane.showMessageDialog(this, "Incorrect store price value", "Incorrect store price value", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
		try {
			player.stop();
			player.discard();
		} catch(Exception err) {
			err.printStackTrace();
		}
		//song title
		String newone = this.txtfSongTitle.getText();
		if (newone.equals("")) {
			newone = " ";
		}
		esong1.newSongTitle = newone;
		//artist name
		newone = this.txtfArtistName.getText();
		if (newone.equals("")) {
			newone = " ";
		}
		esong1.newArtistName = newone;
		//perf by
		if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
			if (combPerfBy.getSelectedIndex() == 0) {
				esong1.isPerfBy = true;
			} else {
				esong1.isPerfBy = false;
			}
		}
		//price
		if (this.hasStorePrice) {
			esong1.newPrice = price;
		}
		//store desc
		if (this.hasStoreDesc) {
			esong1.hasNewStoreDesc = true;
			esong1.newStoreDesc = this.txtfStoreDesc.getText();
		} else {
			esong1.hasNewStoreDesc = false;
		}
		//bandconfig
		esong1.bandConfig = (String)this.combBandConfig.getSelectedItem();
		//loading tip
		esong1.loadingTip = this.txtfLoadingTip.getText().trim();
		//start/end time
		esong1.endTime = this.endTime;
		esong1.startTime = this.startTime;
		this.setVisible(false);
		try {
			new EditSongFinal(esong1, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to next step", "Error going to next step", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void doChangePreview() {
		if (player.isPlaying()) {
			this.btnPlayPreview.setText("Play preview clip");
			player.stop();
		}
		EditSongPreviewTimes e = new EditSongPreviewTimes(this, startTime, endTime, duration);
		if (e.canceled) {
			return;
		}
		this.startTime = e.getStartTime();
		this.endTime = e.getEndTime();
		this.redoPreviewLabel();
	}

	private void doPlayPreview() {
		if (player.isPlaying()) {
			this.btnPlayPreview.setText("Play preview clip");
			player.stop();
		} else {
			this.btnPlayPreview.setText("Stop preview clip");
			player.play(startTime, endTime);
		}
	}

	public void endOfSegment() {
		this.btnPlayPreview.setText("Play preview clip");
	}

	private void populate() throws Exception {
		try {
			//song title
			this.txtfSongTitle.setText(esong1.getSongTitleFor(esong1.selectedSongID));
		} catch (Exception err) {
			throw new Exception("Error retrieving song title");
		}
		try {
			//artist name
			this.txtfArtistName.setText(esong1.songs.readString(esong1.selectedSongID + "/artist"));
		} catch (Exception err) {
			throw new Exception("Error retrieving artist name");
		}
		try {
			//perf by
			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
				//populate the combo box
				this.combPerfBy.addItem(esong1.perfByText);
				this.combPerfBy.addItem(esong1.famousByText);
				if ( !(esong1.songs.nodeExists(esong1.selectedSongID + "/caption")) ) {
					//default is being used
					this.combPerfBy.setSelectedIndex(1);
					esong1.isPerfBy = false;
				} else {
					String caption = esong1.songs.readString(esong1.selectedSongID + "/caption");
					if (caption.equals(EditSettingsOne.perfByKey_2_80)) {
						this.combPerfBy.setSelectedIndex(0);
						esong1.isPerfBy = true;
					} else {
						this.combPerfBy.setSelectedIndex(1);
						esong1.isPerfBy = false;
					}
				}
			}
		} catch (Exception err) {
			throw new Exception("Error retrieving perfby value");
		}
		//store price
		try {
			String storePriceNode = "song/" + this.esong1.selectedSongID + "/price";
			if (esong1.store.nodeExists(storePriceNode)) {
				hasStorePrice = true;
				this.txtfPrice.setText("" + esong1.store.readInt(storePriceNode));
			} else {
				hasStorePrice = false;
				this.txtfPrice.setText("");
			}
		} catch (Exception err) {
			throw new Exception("Error retrieving store price");
		}
		//store desc
		try {
			String storeDescNode = esong1.selectedSongID + "_shop_desc";
			if (esong1.locale.nodeExists(storeDescNode)) {
				hasStoreDesc = true;
				this.txtfStoreDesc.setText("" + esong1.locale.readString(storeDescNode));
			} else {
				hasStoreDesc = false;
				this.txtfStoreDesc.setText("");
			}
		} catch (Exception err) {
			throw new Exception("Error retrieving store description");
		}
		try {
			//band config
			String bandConfigNode = esong1.selectedSongID + "/band";
			this.combBandConfig.setSelectedItem(BAND_MALE);		//default
			if (esong1.songs.nodeExists(bandConfigNode)) {
				Vector bandStrings = esong1.songs.readVector(bandConfigNode);
				int counter = 0;
				if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
					this.combBandConfig.setSelectedItem(BAND_NONE);
				} else {
					this.combBandConfig.setSelectedItem(BAND_MALE);
				}
				while (counter < bandStrings.size()) {
					DTBValue x = (DTBValue)bandStrings.get(counter);
					String val = (String)x.getValue();
					if (val.equals("female_singer") || (val.equals("SINGER_FEMALE_METAL")) ) {
						this.combBandConfig.setSelectedItem(BAND_FEMALE);
					} else if (val.equals("metal_keyboard") || val.equals("KEYBOARD_METAL") ) {
						this.combBandConfig.setSelectedItem(BAND_KEYS);
					}
					counter = counter + 1;
				}
			}
		} catch (Exception err) {
			throw new Exception("Error retrieving band config information");
		}
		//loading tip
		try {
			String loadingTipNode = "loading_tip_" + esong1.selectedSongID;
			if (esong1.locale.nodeExists(loadingTipNode)) {
				this.txtfLoadingTip.setText(esong1.locale.readString(loadingTipNode));
			} else {
				this.txtfLoadingTip.setText("");
			}
		} catch (Exception err) {
			err.printStackTrace();
			throw new Exception("Error retrieving loading tip information");
		}
		//song preview stuff
		//get current preview times
		int startTime = -1;
		int endTime = -1;
		try {
			String previewNode = esong1.selectedSongID + "/preview";
			Vector previewTimes = esong1.songs.readVector(previewNode);
			startTime = ((Integer)((DTBValue)previewTimes.get(0)).getValue()).intValue();
			endTime = ((Integer)((DTBValue)previewTimes.get(1)).getValue()).intValue();
		} catch (Exception err) {
			throw new Exception("Error retrieving preview times");
		}
		//get appropriate VGS File to use for preview (query to use co-op if it exists)
		//unless we already grabbed it in the previous step
		boolean useCoop = false;
		if (esong1.vgsFile == null) {
			try {
				String node = esong1.selectedSongID + "/song/name";
				String vgsFileString = esong1.songs.readString(node);
				vgsFileString = vgsFileString + ".vgs";
				String vgsFolder = vgsFileString.substring(0, vgsFileString.lastIndexOf("/"));
				String vgsFile = vgsFileString.substring(vgsFileString.lastIndexOf("/") + 1);
				esong1.vgsFile = GHUtils.putInTemp(vgsFile, vgsFolder);
				esong1.vgsLocs = vgsFolder;
				//test for coop
				node = esong1.selectedSongID + "/song_coop/name";
				if (esong1.songs.nodeExists(node)) {
					vgsFileString = esong1.songs.readString(node);
					vgsFileString = vgsFileString + ".vgs";
					vgsFolder = vgsFileString.substring(0, vgsFileString.lastIndexOf("/"));
					vgsFile = vgsFileString.substring(vgsFileString.lastIndexOf("/") + 1);
					esong1.vgsFileCoop = GHUtils.putInTemp(vgsFile, vgsFolder);
					String response = null;
					while (response == null) {
						response = (String)JOptionPane.showInputDialog(this, "A separate co-op audio track exists for this song.\nPlease choose " +
							"whether to use the co-op or\n the single player audio for setting sync and preview time.", "Choose co-op or single",
							JOptionPane.INFORMATION_MESSAGE, null, new String[] {"Co-op audio", "Single player audio"}, "Co-op audio");
					}
					if (response.equals("Co-op audio")) {
						useCoop = true;
					}
				}
			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception("Error retrieving VGS file");
			}
		}
		try {
			if (useCoop) {
				player = new AudioPlayer(esong1.vgsFileCoop, null, this);
			} else {
				player = new AudioPlayer(esong1.vgsFile, null, this);
			}
			int duration = player.getDuration();
			if (startTime >= endTime) {
				startTime = endTime - 10000;
				if (startTime < 0) {
					startTime = 0;
				}
			}
			if (duration < endTime) {
				endTime = duration;
				startTime = endTime - 10000;
				if (startTime < 0) {
					startTime = 0;
				}
			}
			this.duration = duration;
			this.startTime = startTime;
			this.endTime = endTime;
			redoPreviewLabel();
		} catch (Exception err) {
			err.printStackTrace();
			throw new Exception("Error initializing VGS audio player");
		}
	}

	private void redoPreviewLabel() {
		this.lblPreview.setText("Preview Time: " + GHUtils.convertOffsetToMinutesSeconds(startTime) + " to " +
				GHUtils.convertOffsetToMinutesSeconds(endTime));
	}

	private void initialize() {
		this.setTitle("Edit Song Information");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);

        disableWindow();
        final EditSongSix x = this;
        Runnable q = new Runnable() {
            public void run() {
            	try {
                	populate();
                } catch (Exception err) {
                	err.printStackTrace();
                	JOptionPane.showMessageDialog(x, "ERROR reading files:\n" + err.getMessage(), "Error reading files", JOptionPane.ERROR_MESSAGE);
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

	private void disableWindow() {
		this.btnCancel.setEnabled(false);
		btnNext.setForeground((Color) UIManager.get("Label.disabledForeground"));
        this.btnNext.setEnabled(false);
        if (combPerfBy != null) {
        	this.combPerfBy.setEnabled(false);
        }
        this.txtfArtistName.setEnabled(false);
        this.txtfSongTitle.setEnabled(false);
        lblLoading.setText("<html><b>Loading: Please wait...</b></html>");
        this.txtfPrice.setEnabled(false);
        this.txtfStoreDesc.setEnabled(false);

        this.combBandConfig.setEnabled(false);
        this.txtfLoadingTip.setEnabled(false);
        this.btnPlayPreview.setEnabled(false);
        this.btnChangePreview.setEnabled(false);
	}

	private void enableWindow() {
		this.btnCancel.setEnabled(true);
		btnNext.setForeground((Color) UIManager.get("Label.enabledForeground"));
        this.btnNext.setEnabled(true);
        if (combPerfBy != null) {
        	this.combPerfBy.setEnabled(true);
        }
        this.txtfArtistName.setEnabled(true);
        this.txtfSongTitle.setEnabled(true);
        lblLoading.setText("");
        if (hasStorePrice) {
        	this.txtfPrice.setEnabled(true);
        	this.lblPrice.setEnabled(true);
        } else {
        	this.txtfPrice.setEnabled(false);
        	this.lblPrice.setEnabled(false);
        }
        if (hasStoreDesc) {
        	this.txtfStoreDesc.setEnabled(true);
        	this.lblStoreDesc.setEnabled(true);
        } else {
        	this.txtfStoreDesc.setEnabled(false);
        	this.lblStoreDesc.setEnabled(false);
        }
        this.combBandConfig.setEnabled(true);
        this.txtfLoadingTip.setEnabled(true);
        this.btnPlayPreview.setEnabled(true);
        this.btnChangePreview.setEnabled(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);

			int pos = 3;
			lblLoading = new JLabel();
			lblLoading.setBounds(new Rectangle(316, pos, 144, 19));
			lblLoading.setFont(GuitarWizardMain.REGFONT);
			lblLoading.setText("<html><b>Loading: Please wait...</b></html>");
			pos = pos + 22;

			lblSongTitle = new JLabel();
			lblSongTitle.setBounds(new Rectangle(25, pos, 62, 19));
			lblSongTitle.setFont(GuitarWizardMain.REGFONT);
			lblSongTitle.setText("Song Title:");
			txtfSongTitle = new JTextField();
			txtfSongTitle.setDocument (new LimitedTextField(SONG_TITLE_LIMIT));
			txtfSongTitle.setFont(GuitarWizardMain.REGFONT);
			txtfSongTitle.setBounds(new Rectangle(90, pos, 326, 21));
			pos = pos + 25;

			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
				combPerfBy = new JComboBox();
				combPerfBy.setBounds(new Rectangle(90, pos, 153, 21));
				combPerfBy.setEditable(false);
				combPerfBy.setFont(GuitarWizardMain.REGFONT);
				pos = pos + 25;
			}

			lblArtistName = new JLabel();
			lblArtistName.setBounds(new Rectangle(16, pos, 73, 19));
			lblArtistName.setText("Artist Name:");
			lblArtistName.setFont(GuitarWizardMain.REGFONT);
			txtfArtistName = new JTextField();
			txtfArtistName.setDocument (new LimitedTextField(ARTIST_NAME_LIMIT));
			txtfArtistName.setFont(GuitarWizardMain.REGFONT);
			txtfArtistName.setBounds(new Rectangle(90, pos, 326, 21));
			pos = pos + 25;

			lblPrice = new JLabel();
			lblPrice.setBounds(new Rectangle(16, pos, 73, 19));
			lblPrice.setText("Store Price:");
			lblPrice.setFont(GuitarWizardMain.REGFONT);
			txtfPrice = new JTextField();
			txtfPrice.setDocument(new LimitedTextField(5));
			txtfPrice.setFont(GuitarWizardMain.REGFONT);
			txtfPrice.setBounds(new Rectangle(90, pos, 76, 21));
			pos = pos + 25;

			lblStoreDesc = new JLabel();
			lblStoreDesc.setBounds(new Rectangle(16, pos, 103, 19));
			lblStoreDesc.setText("Store Description:");
			lblStoreDesc.setFont(GuitarWizardMain.REGFONT);
			txtfStoreDesc = new JTextField();
			txtfStoreDesc.setDocument(new LimitedTextField(STORE_DESC_LIMIT));
			txtfStoreDesc.setFont(GuitarWizardMain.REGFONT);
			txtfStoreDesc.setBounds(new Rectangle(120, pos, 602, 21));
			pos = pos + 25;

			lblLoadingTip = new JLabel();
			lblLoadingTip.setBounds(new Rectangle(16, pos, 130, 19));
			lblLoadingTip.setText("Custom Loading Tip:");
			lblLoadingTip.setFont(GuitarWizardMain.REGFONT);
			txtfLoadingTip = new JTextField();
			txtfLoadingTip.setDocument(new LimitedTextField(LOADING_TIP_LIMIT));
			txtfLoadingTip.setFont(GuitarWizardMain.REGFONT);
			txtfLoadingTip.setBounds(new Rectangle(145, pos, 572, 21));
			pos = pos + 25;

			lblBandConfig = new JLabel();
			lblBandConfig.setBounds(new Rectangle(16, pos, 133, 19));
			lblBandConfig.setText("Band Configuration:");
			lblBandConfig.setFont(GuitarWizardMain.REGFONT);
			combBandConfig = new JComboBox();
			combBandConfig.setFont(GuitarWizardMain.REGFONT);
			combBandConfig.setBounds(new Rectangle(145, pos, 182, 21));
			combBandConfig.addItem(EditSongSix.BAND_FEMALE);
			combBandConfig.addItem(EditSongSix.BAND_MALE);
			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
				combBandConfig.addItem(EditSongSix.BAND_NONE);
			}
			combBandConfig.addItem(EditSongSix.BAND_KEYS);
			pos = pos + 32;

			lblPreview = new JLabel();
			lblPreview.setBounds(new Rectangle(16, pos + 1, 183, 19));
			lblPreview.setText("Preview Time:");
			lblPreview.setFont(GuitarWizardMain.REGFONT);
			btnPlayPreview = new JButton();
			btnPlayPreview.setFont(GuitarWizardMain.REGFONT);
			btnPlayPreview.setText("Play preview clip");
			btnPlayPreview.setBounds(new Rectangle(180, pos, 130, 23));
			btnPlayPreview.setMargin(new java.awt.Insets(2,2,2,2));
			btnPlayPreview.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doPlayPreview();
				}
			});
			btnChangePreview = new JButton();
			btnChangePreview.setText("Change preview time");
			btnChangePreview.setFont(GuitarWizardMain.REGFONT);
			btnChangePreview.setBounds(new Rectangle(330, pos, 150, 23));
			btnChangePreview.setMargin(new java.awt.Insets(2,2,2,2));
			btnChangePreview.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doChangePreview();
				}
			});
			pos = pos + 25;

			jContentPane.add(lblLoadingTip, null);
			jContentPane.add(txtfLoadingTip, null);
			jContentPane.add(lblPreview, null);
			jContentPane.add(btnPlayPreview, null);
			jContentPane.add(btnChangePreview, null);
			jContentPane.add(lblBandConfig, null);
			jContentPane.add(combBandConfig, null);
			jContentPane.add(lblPrice, null);
			jContentPane.add(txtfPrice, null);
			jContentPane.add(lblArtistName, null);
			jContentPane.add(lblLoading, null);
			jContentPane.add(txtfArtistName, null);
			jContentPane.add(lblSongTitle, null);
			jContentPane.add(txtfSongTitle, null);
			jContentPane.add(lblStoreDesc, null);
			jContentPane.add(txtfStoreDesc, null);
			if ( !(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) ) {
				jContentPane.add(combPerfBy, null);
			}
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(647, 268, 74, 22));
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
			btnNext.setText("<html><h4>Finish</h4></html>");
			btnNext.setBounds(new Rectangle(331, 245, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

