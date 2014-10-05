package malictus.gh.ui.editsong;

import javax.swing.*;
import javax.sound.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.util.*;
import javax.swing.UIManager;
import java.io.*;
import java.nio.channels.FileChannel;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;

import malictus.gh.ark.ReinsertFile;
import malictus.gh.dtb.*;
import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.audio.*;
import malictus.gh.ui.editsettings.EditSettingsOne;

/**
 * EditSongThree
 * Step 3 in edit song
 * Choose a new audio file (or files) to overwrite the current one(s)
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongThree extends JDialog {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	protected JButton btnNext = null;
	protected JRadioButton radNothing = null;
	protected JRadioButton radDoSwitch = null;
	protected JTextField txtfNewAudioPath = null;
	protected JButton btnSelect = null;
	protected JLabel lblClickForMore = null;
	protected JTextField txtfNewAudioPathGuit = null;
	protected JButton btnSelectGuit = null;
	protected JTextField txtfNewAudioPathBass = null;
	protected JButton btnSelectBass = null;

	GWMainWindow theParent;
	EditSongOne esong1;

	JFileChooser jfc = new JFileChooser();

	boolean useGuit = false;

	public EditSongThree(EditSongOne editsongone, GWMainWindow parent) {
		super(parent);
		theParent = parent;
		esong1 = editsongone;
		this.setSize(new Dimension(581, 258));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doNext() {
		if (!this.radNothing.isSelected()) {
			//verify, move and convert WAV file(s)
			//first, verify
			File backing = null;
			File guitar = null;
			File bass = null;
			backing = new File(this.txtfNewAudioPath.getText());
			if (!backing.exists() || !backing.isFile()) {
				JOptionPane.showMessageDialog(this, "Invalid audio file selected.", "Invalid audio file selected.", JOptionPane.ERROR_MESSAGE);
	        	return;
			}
			if (this.useGuit) {
				guitar = new File(this.txtfNewAudioPathGuit.getText());
				if (!guitar.exists() || !guitar.isFile()) {
					JOptionPane.showMessageDialog(this, "Invalid audio file selected.", "Invalid audio file selected.", JOptionPane.ERROR_MESSAGE);
		        	return;
				}
				if (!(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1)) {
					if (!txtfNewAudioPathBass.getText().trim().equals("")) {
						bass = new File(this.txtfNewAudioPathBass.getText());
						if (!bass.exists() || !bass.isFile()) {
							JOptionPane.showMessageDialog(this, "Invalid audio file selected.", "Invalid audio file selected.", JOptionPane.ERROR_MESSAGE);
				        	return;
						}
					}
				}
			}
			if ( (guitar != null) && (guitar.getPath().equals(backing.getPath())) ) {
				JOptionPane.showMessageDialog(this, "Your guitar/bass audio cannot be the same as your backing track audio.", "Audio can't be the same", JOptionPane.ERROR_MESSAGE);
	        	return;
			}
			//second, convert and move
			File newBacking = null;
			File newGuit = null;
			File newBass = null;

			if (backing.getPath().toLowerCase().endsWith("vgs")) {
				try {
					newBacking = File.createTempFile("xyz", null, GuitarWizardMain.TempDir);
					newBacking.deleteOnExit();
					//at this stage, just copy over existing file directly
					FileChannel srcChannel = new FileInputStream(backing).getChannel();
				    FileChannel dstChannel = new FileOutputStream(newBacking).getChannel();
				    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				    srcChannel.close();
				    dstChannel.close();
				} catch (Exception err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				try {
					newBacking = File.createTempFile("xyz", null, GuitarWizardMain.TempDir);
				} catch (Exception err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
					return;
				}
				newBacking.deleteOnExit();
				ConvertAudioToWAV c = new ConvertAudioToWAV(this, backing, newBacking);
				if ( !(c.getFinishedString().equals(ConvertAudioToWAV.FINISHED_SUCCESSFULLY)) ) {
					newBacking.delete();
					JOptionPane.showMessageDialog(this, c.getFinishedString(), "Audio not loaded", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (guitar != null) {
					try {
						newGuit = File.createTempFile("abc", null, GuitarWizardMain.TempDir);
					} catch (Exception err) {
						err.printStackTrace();
						JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
						newBacking.delete();
						return;
					}
					newGuit.deleteOnExit();
					ConvertAudioToWAV d = new ConvertAudioToWAV(this, guitar, newGuit);
					if ( !(d.getFinishedString().equals(ConvertAudioToWAV.FINISHED_SUCCESSFULLY)) ) {
						JOptionPane.showMessageDialog(this, d.getFinishedString(), "Audio not loaded", JOptionPane.ERROR_MESSAGE);
						newBacking.delete();
						newGuit.delete();
						return;
					}
				}
				if (bass != null) {
					try {
						newBass = File.createTempFile("cde", null, GuitarWizardMain.TempDir);
					} catch (Exception err) {
						err.printStackTrace();
						JOptionPane.showMessageDialog(this, "Error creating temp files", "Error creating temp files", JOptionPane.ERROR_MESSAGE);
						return;
					}
					newBass.deleteOnExit();
					ConvertAudioToWAV e = new ConvertAudioToWAV(this, bass, newBass);
					if ( !(e.getFinishedString().equals(ConvertAudioToWAV.FINISHED_SUCCESSFULLY)) ) {
						JOptionPane.showMessageDialog(this, e.getFinishedString(), "Audio not loaded", JOptionPane.ERROR_MESSAGE);
						newBacking.delete();
						if (newGuit != null) {
							newGuit.delete();
						}
						return;
					}
				}
			}

			//grab original VGS file(s) and tell esong1 about them and that we're altering them
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
				}
				esong1.hasNewVGSFile = true;
			} catch (Exception err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error getting original VGS files", "Audio not loaded", JOptionPane.ERROR_MESSAGE);
				newBacking.delete();
				if (newGuit != null) {
					newGuit.delete();
				}
				if (newBass != null) {
					newBass.delete();
				}
				return;
			}

			//modify original VGS file(s) to match new content
			if (backing.getPath().toLowerCase().endsWith("vgs")) {
				//stuff here!
			} else {
				ConvertWAVToVGS v = new ConvertWAVToVGS(this, newBacking, newGuit, newBass, esong1.vgsFile);
				if ( !(v.getFinishedString().equals(ConvertWAVToVGS.FINISHED_SUCCESSFULLY)) ) {
					JOptionPane.showMessageDialog(this, v.getFinishedString(), "VGS file not created", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (esong1.vgsFileCoop != null) {
					v = new ConvertWAVToVGS(this, newBacking, newGuit, newBass, esong1.vgsFileCoop);
					if ( !(v.getFinishedString().equals(ConvertWAVToVGS.FINISHED_SUCCESSFULLY)) ) {
						JOptionPane.showMessageDialog(this, v.getFinishedString(), "VGS coop file not created", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
		}
		//all audio loaded successfully
		this.setVisible(false);
		try {
			//to MIDI options page
			new EditSongFour(esong1, theParent);
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to next step", "Error going to next step", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void initialize() {
		this.setTitle("Substitute new audio");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        radDoSwitch.setSelected(true);
        hideExtras();
        this.setVisible(true);
	}

	private void selectMainAudio() {
		GHAudioFilterAddVGS GHfilt = new GHAudioFilterAddVGS();
		if (jfc == null) {
			jfc = new JFileChooser();
		}
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(GHfilt);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        //have to be careful with removeChoosableFileFilter since it seems to cause problems on mac
        if (returnVal == JFileChooser.CANCEL_OPTION) {
        	jfc.removeChoosableFileFilter(GHfilt);
            return;
        }
        this.txtfNewAudioPath.setText(jfc.getSelectedFile().getPath());
        jfc.removeChoosableFileFilter(GHfilt);
        if (jfc.getSelectedFile().getPath().trim().toLowerCase().endsWith("vgs")) {
        	//disable other entries
        	this.txtfNewAudioPathBass.setEnabled(false);
        	this.btnSelectBass.setEnabled(false);
        	this.txtfNewAudioPathGuit.setEnabled(false);
        	this.btnSelectGuit.setEnabled(false);
        	this.txtfNewAudioPathBass.setText("");
        	this.txtfNewAudioPathGuit.setText("");
        } else {
        	this.txtfNewAudioPathBass.setEnabled(true);
        	this.btnSelectBass.setEnabled(true);
        	this.txtfNewAudioPathGuit.setEnabled(true);
        	this.btnSelectGuit.setEnabled(true);
        }
	}

	private void selectGuitAudio() {
		GHAudioFilter GHfilt = new GHAudioFilter();
		if (jfc == null) {
			jfc = new JFileChooser();
		}
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(GHfilt);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        //have to be careful with removeChoosableFileFilter since it seems to cause problems on mac
        if (returnVal == JFileChooser.CANCEL_OPTION) {
        	jfc.removeChoosableFileFilter(GHfilt);
            return;
        }
        this.txtfNewAudioPathGuit.setText(jfc.getSelectedFile().getPath());
        jfc.removeChoosableFileFilter(GHfilt);
	}

	private void selectBassAudio() {
		GHAudioFilter GHfilt = new GHAudioFilter();
		if (jfc == null) {
			jfc = new JFileChooser();
		}
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(GHfilt);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(false);
        int returnVal = jfc.showOpenDialog(this);
        //have to be careful with removeChoosableFileFilter since it seems to cause problems on mac
        if (returnVal == JFileChooser.CANCEL_OPTION) {
        	jfc.removeChoosableFileFilter(GHfilt);
            return;
        }
        this.txtfNewAudioPathBass.setText(jfc.getSelectedFile().getPath());
        jfc.removeChoosableFileFilter(GHfilt);
	}

	private void selectNothing() {
		this.btnSelect.setEnabled(false);
		this.btnSelectBass.setEnabled(false);
		this.btnSelectGuit.setEnabled(false);
		this.lblClickForMore.setForeground(Color.gray);
		this.txtfNewAudioPath.setForeground(Color.gray);
		this.txtfNewAudioPath.setEnabled(false);
		this.txtfNewAudioPathGuit.setEnabled(false);
		this.txtfNewAudioPathBass.setEnabled(false);
		this.txtfNewAudioPathGuit.setForeground(Color.gray);
		this.txtfNewAudioPathBass.setForeground(Color.gray);
	}

	private void selectSomething() {
		lblClickForMore.setForeground(Color.BLUE);
		this.btnSelect.setEnabled(true);
		this.btnSelectBass.setEnabled(true);
		this.btnSelectGuit.setEnabled(true);
		this.txtfNewAudioPath.setForeground(Color.black);
		this.txtfNewAudioPath.setEnabled(true);
		this.txtfNewAudioPathGuit.setForeground(Color.black);
		this.txtfNewAudioPathBass.setForeground(Color.black);
		this.txtfNewAudioPathGuit.setEnabled(true);
		this.txtfNewAudioPathBass.setEnabled(true);
	}

	private void hideExtras() {
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			this.lblClickForMore.setText("<html><body><u>Click here to add a separate guitar track</u></body></html>");
		} else {
			this.lblClickForMore.setText("<html><body><u>Click here to add separate guitar/bass tracks</u></body></html>");
		}
		this.btnSelectBass.setVisible(false);
		this.btnSelectGuit.setVisible(false);
		this.txtfNewAudioPathBass.setVisible(false);
		this.txtfNewAudioPathGuit.setVisible(false);
	}

	private void showExtras() {
		this.lblClickForMore.setText("<html><body><u>Click here to use a single audio file only</u></body></html>");
		if (!(GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1)) {
			this.btnSelectBass.setVisible(true);
			this.txtfNewAudioPathBass.setVisible(true);
		}
		this.btnSelectGuit.setVisible(true);
		this.txtfNewAudioPathGuit.setVisible(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			int pos = 9;

			radNothing = new JRadioButton();
			radNothing.setBounds(new Rectangle(9, pos, 403, 19));
			radNothing.setFont(GuitarWizardMain.REGFONT);
			radNothing.setText("Keep existing audio");
			radNothing.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectNothing();
				}
			});
			pos = pos + 25;

			radDoSwitch = new JRadioButton();
			radDoSwitch.setBounds(new Rectangle(9, pos, 403, 19));
			radDoSwitch.setFont(GuitarWizardMain.REGFONT);
			radDoSwitch.setText("Replace existing audio");
			radDoSwitch.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectSomething();
				}
			});
			pos = pos + 25;

			txtfNewAudioPath = new JTextField();
			txtfNewAudioPath.setEditable(false);
			txtfNewAudioPath.setBounds(new Rectangle(59, pos, 303, 19));
			txtfNewAudioPath.setFont(GuitarWizardMain.REGFONT);
			btnSelect = new JButton();
			btnSelect.setBounds(new Rectangle(379, pos - 2, 104, 22));
			btnSelect.setFont(GuitarWizardMain.REGFONT);
			btnSelect.setText("Select audio");
			btnSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectMainAudio();
				}
			});
			pos = pos + 25;

			lblClickForMore = new JLabel();
			lblClickForMore.setBounds(new Rectangle(59, pos, 310, 19));
			lblClickForMore.setFont(GuitarWizardMain.REGFONT);
			lblClickForMore.setText("");
			lblClickForMore.setForeground(Color.BLUE);
			lblClickForMore.addMouseListener(new java.awt.event.MouseAdapter(){
				public void mouseEntered(java.awt.event.MouseEvent event) {
					Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
					setCursor(handCursor);
				}
				public void mouseExited(java.awt.event.MouseEvent event) {
					Cursor handCursor = new Cursor(Cursor.DEFAULT_CURSOR);
					setCursor(handCursor);
				}
				public void mouseClicked(java.awt.event.MouseEvent event) {
					if (event.getClickCount() == 1){
						if (useGuit) {
							useGuit = false;
							hideExtras();
						} else {
							useGuit = true;
							showExtras();
						}
					}
				}
			});
			pos = pos + 25;

			txtfNewAudioPathGuit = new JTextField();
			txtfNewAudioPathGuit.setBounds(new Rectangle(59, pos, 303, 19));
			txtfNewAudioPathGuit.setFont(GuitarWizardMain.REGFONT);
			txtfNewAudioPathGuit.setEditable(false);
			btnSelectGuit = new JButton();
			btnSelectGuit.setBounds(new Rectangle(379, pos - 2, 184, 22));
			btnSelectGuit.setFont(GuitarWizardMain.REGFONT);
			btnSelectGuit.setText("Select guitar audio");
			btnSelectGuit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectGuitAudio();
				}
			});
			pos = pos + 25;

			txtfNewAudioPathBass = new JTextField();
			txtfNewAudioPathBass.setBounds(new Rectangle(59, pos, 303, 19));
			txtfNewAudioPathBass.setFont(GuitarWizardMain.REGFONT);
			txtfNewAudioPathBass.setEditable(false);
			btnSelectBass = new JButton();
			btnSelectBass.setBounds(new Rectangle(379, pos - 2, 184, 22));
			btnSelectBass.setFont(GuitarWizardMain.REGFONT);
			btnSelectBass.setText("Select rhythm/bass audio");
			btnSelectBass.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectBassAudio();
				}
			});
			pos = pos + 25;

			ButtonGroup bgrp = new ButtonGroup();
			bgrp.add(radNothing);
			bgrp.add(radDoSwitch);

			jContentPane.add(radNothing, null);
			jContentPane.add(radDoSwitch, null);
			jContentPane.add(txtfNewAudioPath, null);
			jContentPane.add(btnSelect, null);
			jContentPane.add(lblClickForMore, null);
			jContentPane.add(txtfNewAudioPathGuit, null);
			jContentPane.add(btnSelectGuit, null);
			jContentPane.add(txtfNewAudioPathBass, null);
			jContentPane.add(btnSelectBass, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(479, 203, 74, 22));
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
			btnNext.setBounds(new Rectangle(212, 179, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

}