package malictus.gh.ui;

import malictus.gh.*;
import malictus.gh.dtb.DTBFile;
import malictus.gh.ui.editsettings.*;
import malictus.gh.ui.optimize.*;
import malictus.gh.ui.editsong.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Insets;
import java.io.*;

/**
 * GWMainWindow
 * Primary UI window for Guitar Wizard
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class GWMainWindow extends JFrame {

	private JPanel jContentPane = null;
	private JLabel lblTitle = null;
	private JLabel lblCredit = null;
	private JLabel lblCurrentArchive = null;
	public JTextField txtfCurrArchive = null;		//public since other classes change this value
	private JButton btnOpenArchive = null;
	private JLabel lblTempDir = null;
	private JTextField txtfTempDir = null;
	private JButton btnChangeTempDir = null;
	private JButton btnOptimize = null;
	private JButton btnEditSong = null;
	private JButton btnEditSettings = null;
	private JLabel lblWarning = null;
	private JLabel lblCurrVersion = null;

	JFileChooser jfc = new JFileChooser();

	public GWMainWindow() {
		super();
		initialize();
		this.setVisible(true);
    }

	void changeTempDir() {
		try {
			JOptionPane.showMessageDialog(this, "Please select an empty folder to use as scratch space.\nAll files that are written here " +
					"will be deleted when the program exits.\nYou should have at least 3GB of free space to use for this directory.", "Set temp directory", JOptionPane.INFORMATION_MESSAGE);
			if (jfc == null) {
				jfc = new JFileChooser();
			}
			jfc.setAcceptAllFileFilterUsed(true);
	        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        jfc.setMultiSelectionEnabled(false);
	        int returnVal = jfc.showOpenDialog(this);
	        if (returnVal == JFileChooser.CANCEL_OPTION) {
	            return;
	        }
	        File tempFile = jfc.getSelectedFile();
	        //verify is empty and directory, and exists
	        if (!tempFile.exists()) {
	        	JOptionPane.showMessageDialog(this, "The specified folder does not exist", "Folder not found", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        if (!tempFile.isDirectory()) {
	        	JOptionPane.showMessageDialog(this, "The specified file is not a directory", "Not a directory", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        if (! (tempFile.listFiles().length == 0)) {
	        	JOptionPane.showMessageDialog(this, "The specified directory is not empty", "Directory contains files", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        //display in UI, and set global var
	        GuitarWizardMain.TempDir = tempFile;
	        this.txtfTempDir.setText(GuitarWizardMain.TempDir.getPath());
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error setting temp directory", "Error setting temp directory", JOptionPane.ERROR_MESSAGE);
		}
	}

	void optimize() {
		new OptimizeAndShrinkOne(this);
	}

	void editSong() {
		new EditSongOne(this);
	}

	void editSettings() {
		new EditSettingsOne(this);
	}

	void openArchive() {
		try {
			if (GuitarWizardMain.ARKFile == null) {
				JOptionPane.showMessageDialog(this, "Locate and select the file that starts with 'SLUS' \nor 'SLES', which should be in the" +
						"\nroot level of your GH archive.\nMake sure to back up your archive first, \nif you have made changes to it already.", "Open archive", JOptionPane.INFORMATION_MESSAGE);
			}
			GHArchiveFilter GHfilt = new GHArchiveFilter();
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
	        GuitarWizardMain.ELFFile = jfc.getSelectedFile();
	        jfc.removeChoosableFileFilter(GHfilt);
	        //verify archive and accompanying files
	        if ((!GuitarWizardMain.ELFFile.exists()) || (!GuitarWizardMain.ELFFile.isFile())) {
	        	JOptionPane.showMessageDialog(this, "GH Archive not found", "File not found", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }

	        //look for ARK/HDR files, and SYSTEM file
	        File parent = GuitarWizardMain.ELFFile.getParentFile();
	        File[] fils = parent.listFiles();
	        int counter = 0;
	        File headerFile = null;
	        File arkFile = null;
	        File systemFile = null;
	        while (counter < fils.length) {
	        	File x = fils[counter];
	        	if (x.getName().toUpperCase().equals("GEN")) {
	        		File[] subfils = x.listFiles();
	        		int innercounter = 0;
	        		while (innercounter < subfils.length) {
	        			File y = subfils[innercounter];
	        			if (y.getName().toUpperCase().equals("MAIN.HDR")) {
	        				headerFile = y;
	        			} else if (y.getName().toUpperCase().equals("MAIN_0.ARK")) {
	        				arkFile = y;
	        			}
	        			innercounter = innercounter + 1;
	        		}
	        	} else if (x.getName().toUpperCase().equals("SYSTEM.CNF")) {
	        		systemFile = x;
	        	}
	        	counter = counter + 1;
	        }
	        if ( (headerFile == null) || (arkFile == null) || (systemFile == null)) {
	        	JOptionPane.showMessageDialog(this, "GH Archive files not found", "Files not found", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        if ( (!headerFile.canWrite()) || (!arkFile.canWrite()) || (!systemFile.canWrite())) {
	        	JOptionPane.showMessageDialog(this, "GH Archive files are set to read-only.\nMake sure to copy archive files to a location on your hard drive,\nand to change permissions so that the files are not read only.", "Files are read only", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        //found them, so set global vars
	        GuitarWizardMain.ARKFile = arkFile;
	        GuitarWizardMain.HDRFile = headerFile;
	        GuitarWizardMain.SystemFile = systemFile;
	        //create temp directory
	        if (GuitarWizardMain.TempDir == null) {
	        	File tempFile = File.createTempFile("GHWiz", "tmp");
		        tempFile.delete();
		        File tempDir = new File(tempFile.getPath().substring(0, tempFile.getPath().length() - 3));
		        if (tempDir.exists()) {
		        	JOptionPane.showMessageDialog(this, "Error creating scratch directory", "Error creating scratch", JOptionPane.ERROR_MESSAGE);
		        	return;
		        }
		        tempDir.mkdirs();
		        tempDir.deleteOnExit();
		        GuitarWizardMain.TempDir = tempDir;
	        }
	        //read hdr and look for existence of certain song files to find version
	        if (GHUtils.fileExists("aceofspades.mid", "songs/aceofspades")) {
	        	GuitarWizardMain.Version = GuitarWizardMain.VERSION_GH1;
	        	this.lblCurrVersion.setText("Current Archive Version: Guitar Hero 1");
	        } else if (GHUtils.fileExists("arterialblack.mid", "songs/arterialblack")) {
	        	GuitarWizardMain.Version = GuitarWizardMain.VERSION_GH2;
	        	this.lblCurrVersion.setText("Current Archive Version: Guitar Hero 2");
	        } else if (GHUtils.fileExists("18andlife.mid", "songs/18andlife")) {
	        	GuitarWizardMain.Version = GuitarWizardMain.VERSION_GH80;
	        	this.lblCurrVersion.setText("Current Archive Version: Guitar Hero Rocks The 80's");
	        } else {
	        	JOptionPane.showMessageDialog(this, "Unknown version of Guitar Hero", "Unknown GH version", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        //test for mac zeroes bug
	        if (!GHUtils.arkIsValid()) {
	        	JOptionPane.showMessageDialog(this, "Your ARK appears to be all zeroes, probably because it was transferred on a Mac.\nTry transferring your files from your GH disc using a Windows machine.", "Bad ARK", JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        this.txtfTempDir.setText(GuitarWizardMain.TempDir.getPath());
	        this.txtfCurrArchive.setText(GuitarWizardMain.ELFFile.getPath());
	        this.btnOpenArchive.setText("Change");
	        this.lblTempDir.setEnabled(true);
	        this.btnChangeTempDir.setEnabled(true);
	        lblWarning.setForeground((Color) UIManager.get("Label.enabledForeground"));
	        btnOptimize.setForeground((Color) UIManager.get("Label.enabledForeground"));
	        this.btnEditSong.setForeground((Color) UIManager.get("Label.enabledForeground"));
	        this.btnEditSettings.setForeground((Color) UIManager.get("Label.enabledForeground"));
	        btnEditSettings.setEnabled(true);
	        btnOptimize.setEnabled(true);
	        btnEditSong.setEnabled(true);
		} catch (Exception err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error opening GH Archive", "Error opening GH Archive", JOptionPane.ERROR_MESSAGE);
		}
	}

	void this_windowClosing(WindowEvent e) {
        GuitarWizardMain.doShutdown();
    }

	private void initialize() {
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
	    });
		this.setSize(new Dimension(550, 295));
        GHUtils.centerWindow(this);
        this.setResizable(false);
        this.setTitle("GUITAR WIZARD " + GuitarWizardMain.VERSION);
        this.setContentPane(getJContentPane());
	}

	private JTextField getTxtfCurrArchive() {
		if (txtfCurrArchive == null) {
			txtfCurrArchive = new JTextField();
			txtfCurrArchive.setEditable(false);
			txtfCurrArchive.setFont(GuitarWizardMain.REGFONT);
			txtfCurrArchive.setBounds(new Rectangle(101, 60, 328, 22));
		}
		return txtfCurrArchive;
	}

	private JButton getBtnOpenArchive() {
		if (btnOpenArchive == null) {
			btnOpenArchive = new JButton();
			btnOpenArchive.setMargin(new Insets(2,2,2,2));
			btnOpenArchive.setBounds(new Rectangle(439, 60, 99, 21));
			btnOpenArchive.setText("Open Archive");
			btnOpenArchive.setFont(GuitarWizardMain.REGFONT);
			btnOpenArchive.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openArchive();
				}
			});
		}
		return btnOpenArchive;
	}

	private JTextField getTxtfTempDir() {
		if (txtfTempDir == null) {
			txtfTempDir = new JTextField();
			txtfTempDir.setFont(GuitarWizardMain.REGFONT);
			txtfTempDir.setBounds(new Rectangle(101, 85, 328, 22));
			txtfTempDir.setEditable(false);
		}
		return txtfTempDir;
	}

	private JButton getBtnChangeTempDir() {
		if (btnChangeTempDir == null) {
			btnChangeTempDir = new JButton();
			btnChangeTempDir.setBounds(new Rectangle(439, 85, 99, 21));
			btnChangeTempDir.setText("Change");
			btnChangeTempDir.setFont(GuitarWizardMain.REGFONT);
			btnChangeTempDir.setMargin(new Insets(2, 2, 2, 2));
			btnChangeTempDir.setEnabled(false);
			btnChangeTempDir.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					changeTempDir();
				}
			});
		}
		return btnChangeTempDir;
	}

	private JButton getBtnOptimize() {
		if (btnOptimize == null) {
			btnOptimize = new JButton();
			btnOptimize.setBounds(new Rectangle(69, 158, 112, 48));
			btnOptimize.setForeground((Color) UIManager.get("Label.disabledForeground"));
			btnOptimize.setText("<html><center><h4>Optimize and Shrink</h4></center></html>");
			btnOptimize.setEnabled(false);
			btnOptimize.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					optimize();
				}
			});
		}
		return btnOptimize;
	}

	private JButton getBtnEditSong() {
		if (btnEditSong == null) {
			btnEditSong = new JButton();
			btnEditSong.setBounds(new Rectangle(219, 158, 112, 48));
			btnEditSong.setText("<html><center><h4>Edit/Replace Song</h4></center></html>");
			btnEditSong.setForeground((Color) UIManager.get("Label.disabledForeground"));
			btnEditSong.setEnabled(false);
			btnEditSong.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					editSong();
				}
			});
		}
		return btnEditSong;
	}

	private JButton getBtnEditSettings() {
		if (btnEditSettings == null) {
			btnEditSettings = new JButton();
			btnEditSettings.setBounds(new Rectangle(367, 158, 112, 48));
			btnEditSettings.setForeground((Color) UIManager.get("Label.disabledForeground"));
			btnEditSettings.setText("<html><center><h4>Edit Other GH Settings</h4></center></html>");
			btnEditSettings.setEnabled(false);
			btnEditSettings.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					editSettings();
				}
			});
		}
		return btnEditSettings;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblCurrVersion = new JLabel();
			lblCurrVersion.setBounds(new Rectangle(26, 116, 480, 21));
			lblCurrVersion.setText("");
			lblCurrVersion.setFont(GuitarWizardMain.REGFONT);
			lblCurrVersion.setHorizontalAlignment(SwingConstants.CENTER);
			lblWarning = new JLabel();
			lblWarning.setBounds(new Rectangle(11, 215, 522, 48));
			lblWarning.setFont(GuitarWizardMain.REGFONT);
			lblWarning.setText("<html>Note: It is highly recommended that you Optimize and Shrink all archives before doing other operations. Optimize and Shrink needs to be performed only once per archive.</html>");
			lblWarning.setForeground((Color) UIManager.get("Label.disabledForeground"));
			lblTempDir = new JLabel();
			lblTempDir.setBounds(new Rectangle(10, 88, 92, 16));
			lblTempDir.setEnabled(false);
			lblTempDir.setText("Temp Directory:");
			lblTempDir.setFont(GuitarWizardMain.REGFONT);
			lblCurrentArchive = new JLabel();
			lblCurrentArchive.setBounds(new Rectangle(8, 60, 93, 20));
			lblCurrentArchive.setText("Current Archive:");
			lblCurrentArchive.setFont(GuitarWizardMain.REGFONT);
			lblCredit = new JLabel();
			lblCredit.setBounds(new Rectangle(109, 30, 351, 22));
			lblCredit.setFont(GuitarWizardMain.REGFONT);
			lblCredit.setText("");
			lblTitle = new JLabel();
			lblTitle.setBounds(new Rectangle(183, 7, 165, 27));
			lblTitle.setText("Guitar Wizard " + GuitarWizardMain.VERSION);
			lblTitle.setFont(GuitarWizardMain.HEADERFONT);
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblTitle, null);
			jContentPane.add(lblCredit, null);
			jContentPane.add(lblCurrentArchive, null);
			jContentPane.add(getTxtfCurrArchive(), null);
			jContentPane.add(getBtnOpenArchive(), null);
			jContentPane.add(lblTempDir, null);
			jContentPane.add(getTxtfTempDir(), null);
			jContentPane.add(getBtnChangeTempDir(), null);
			jContentPane.add(getBtnOptimize(), null);
			jContentPane.add(getBtnEditSong(), null);
			jContentPane.add(getBtnEditSettings(), null);
			jContentPane.add(lblWarning, null);
			jContentPane.add(lblCurrVersion, null);
		}
		return jContentPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
