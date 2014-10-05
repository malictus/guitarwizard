package malictus.gh.ui.optimize;

import malictus.gh.*;
import malictus.gh.ui.*;
import malictus.gh.ark.*;
import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.*;
import java.util.Vector;
import java.nio.channels.*;

/**
 * OptimizeAndShrinkTwo
 * Step 2 in optimize and shrink
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class OptimizeAndShrinkTwo extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnClose = null;
	private JLabel lblExplanation = null;
	public JLabel lblStep1 = null;
	public JLabel lblStep2 = null;
	public JLabel lblStep3 = null;
	public JLabel lblStep4 = null;

	boolean removeIntro = false;
	boolean removeBonusVideos = false;
	boolean removeMenuMusic = false;
	boolean removePracticeMode = false;
	boolean removeTutorial = false;
	boolean removeCredits = false;

	Vector tempFiles = new Vector();

	Vector menuMusicFileNames = new Vector();

	public OptimizeAndShrinkTwo(GWMainWindow parent, boolean intro, boolean bonusVideo, boolean menuMusic, boolean practiceMode, boolean tutorial, boolean credits) {
		super(parent);
		removeIntro = intro;
		removeBonusVideos = bonusVideo;
		removeMenuMusic = menuMusic;
		removePracticeMode = practiceMode;
		removeTutorial = tutorial;
		removeCredits = credits;

		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2) {
			menuMusicFileNames.add("metaloop1_warpigs.vgs");
			menuMusicFileNames.add("metaloop2_psycho.vgs");
			menuMusicFileNames.add("metaloop3_whowas.vgs");
			menuMusicFileNames.add("metaloop_artillery.vgs");
			menuMusicFileNames.add("metaloop_crowns.vgs");
			menuMusicFileNames.add("metaloop_ftk.vgs");
			menuMusicFileNames.add("metaloop_hoods.vgs");
			menuMusicFileNames.add("metaloop_rawdog.vgs");
			menuMusicFileNames.add("metaloop_voivod.vgs");
		} else if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH80) {
			menuMusicFileNames.add("metaloop_bangyourhead.vgs");
			menuMusicFileNames.add("metaloop_holydiver.vgs");
			menuMusicFileNames.add("metaloop_iran.vgs");
			menuMusicFileNames.add("metaloop_noonelikeyou.vgs");
			menuMusicFileNames.add("metaloop_radarlove.vgs");
			menuMusicFileNames.add("metaloop_shakin.vgs");
			menuMusicFileNames.add("metaloop_wegotthebeat.vgs");
		}

		this.setSize(new Dimension(341, 333));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doOptimize() {
		lblStep1.setText("<html>Expanding ARK File...</html>");
		ExpandARK a = new ExpandARK(this);
		if (a.getFinishedString().equals(ExpandARK.WAS_CANCELED)) {
			GHUtils.cleanupTempDir();
			lblExplanation.setText("Shrink and Optimize Canceled");
			this.setTitle("Optimize archive canceled");
			lblStep1.setText("<html><h4>Expanding ARK File... CANCELED</h4></html>");
			btnClose.setEnabled(true);
			return;
		} else if ( !(a.getFinishedString().equals(ExpandARK.FINISHED_SUCCESSFULLY))) {
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR expanding ARK file:\n" + a.getFinishedString(), "Error expanding ARK file", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Shrink and Optimize - ERROR");
			this.setTitle("Optimize archive - ERROR");
			lblStep1.setText("<html><h4>Expanding ARK File... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep1.setText("<html><h4>Expanding ARK File... COMPLETED</h4></html>");

		lblStep2.setText("<html>Replacing Files...</html>");
		try {
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH2 ||
					GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH80) {
				//remove non-ARK video files, if required
				File parPath = GuitarWizardMain.ARKFile.getParentFile().getParentFile();
				File[] kids = parPath.listFiles();
				File videoPath = null;
				int counter = 0;
				while (counter < kids.length) {
					File kid = kids[counter];
					if (kid.isDirectory() && kid.getName().toUpperCase().equals("VIDEOS")) {
						videoPath = kid;
					}
					counter = counter + 1;
				}
				if (videoPath != null) {
					File[] morekids = videoPath.listFiles();
					counter = 0;
					while (counter < morekids.length) {
						File lid = morekids[counter];
						String name = lid.getName().toUpperCase();
						if (removeIntro) {
							if (name.equals("INTRO.PSS")) {
								lid.delete();
							}
						}
						if (removeBonusVideos) {
							if (name.equals("HMX.PSS")) {
								lid.delete();
							}
							if (name.equals("WG.PSS")) {
								lid.delete();
							}
						}
						counter = counter + 1;
					}
				}
			}
			//remove specific files from opened ARK archive, replacing them with a blanks
			tempFiles.clear();
    		getFilesFor(GuitarWizardMain.TempDir);
    		int counter = 0;
    		while (counter < tempFiles.size()) {
    			File f = (File)tempFiles.get(counter);
    			String s = f.getName();
    			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
    				if (removeIntro) {
    					if (s.equals("ghintro.pss")) {
    						replaceWithSilence(f);
    					}
    				}
    				if (removeBonusVideos) {
    					if (s.equals("character.pss")) {
    						replaceWithSilence(f);
    					}
    					if (s.equals("venues.pss")) {
    						replaceWithSilence(f);
    					}
    					if (s.equals("wavegroup.pss")) {
    						replaceWithSilence(f);
    					}
    				}
    			}

    			if (removeCredits) {
    				if (s.equals("credits.vgs")) {
    					replaceWithSilence(f);
    				}
    			}
    			if (removeMenuMusic) {
    				if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
    					if (s.startsWith("meta_loop")) {
    						replaceWithSilence(f);
    					}
    				} else {
	    				if (menuMusicFileNames.contains(s)) {
	    					replaceWithSilence(f);
	    				}
    				}
    			}
    			if (removeTutorial) {
    				if (s.startsWith("tutorial") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("v10") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("v19") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("v20") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("v30") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("vgood") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("vgreat") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("vmiss") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.startsWith("vtimeout") && s.endsWith("vgs")) {
    					replaceWithSilence(f);
    				}
    			}
    			if (removePracticeMode) {
    				if (s.endsWith("_p50.vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.endsWith("_p65.vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.endsWith("_p85.vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.endsWith("_p60.vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.endsWith("_p75.vgs")) {
    					replaceWithSilence(f);
    				}
    				if (s.endsWith("_p90.vgs")) {
    					replaceWithSilence(f);
    				}
    			}
    			counter = counter + 1;
    		}

		} catch (Exception err) {
			err.printStackTrace();
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR removing files:\n" + a.getFinishedString(), "Error removing files", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Shrink and Optimize Aborted");
			this.setTitle("Optimize archive aborted");
			lblStep2.setText("<html><h4>Replacing Files... CANCELED</h4></html>");
			btnClose.setEnabled(true);
			return;
		}

		lblStep2.setText("<html><h4>Replacing Files... COMPLETED</h4></html>");

		lblStep3.setText("<html>Creating ARK File...</html>");
		CreateARK b = new CreateARK(this);
		if ( !(b.getFinishedString().equals(CreateARK.FINISHED_SUCCESSFULLY))) {
			GHUtils.cleanupTempDir();
			JOptionPane.showMessageDialog(this, "ERROR creating ARK file:\n" + b.getFinishedString(), "Error creating ARK file", JOptionPane.ERROR_MESSAGE);
			lblExplanation.setText("Shrink and Optimize - ERROR");
			this.setTitle("Optimize archive - ERROR");
			lblStep3.setText("<html><h4>Creating ARK File... ERROR</h4></html>");
			btnClose.setEnabled(true);
			return;
		}
		lblStep3.setText("<html><h4>Creating ARK File... COMPLETED</h4></html>");

		lblStep4.setText("<html>Removing temp files...</html>");
		GHUtils.cleanupTempDir();
		lblStep4.setText("<html><h4>Removing temp files... COMPLETED</h4></html>");

		lblExplanation.setText("Optimize archive completed");
		this.setTitle("Optimize archive completed");
		btnClose.setEnabled(true);
	}

	private void replaceWithSilence(File f) throws Exception {
		f.delete();
		f.createNewFile();
	    FileChannel srcChannel = new FileInputStream(GuitarWizardMain.BLANK_VGS_FILE).getChannel();
	    FileChannel dstChannel = new FileOutputStream(f).getChannel();
	    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	    srcChannel.close();
	    dstChannel.close();
	}

	private void getFilesFor(File expandFolder) {
		File[] children = expandFolder.listFiles();
		int counter = 0;
		while (counter < children.length) {
			File child = children[counter];
			if (child.isFile()) {
				//don't include header file!
				if (!child.getName().equals("MAIN.HDR")) {
					tempFiles.add(child);
				}
			} else {
				//recurse
				getFilesFor(child);
			}
			counter = counter + 1;
		}
		return;
	}

	private void initialize() {
		this.setTitle("Optimizing...");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);

        btnClose.setEnabled(false);
        Runnable q = new Runnable() {
            public void run() {
            	doOptimize();
            }
        };
        Thread t = new Thread(q);
        t.start();

        this.setVisible(true);
	}

	private void doClose() {
		this.setVisible(false);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblExplanation = new JLabel();
			lblExplanation.setBounds(new Rectangle(6, 5, 322, 45));
			lblExplanation.setText("<html><center>Please wait while your archive is being optimized...</center></html>");

			int pos = 63;
			lblStep1 = new JLabel();
			lblStep1.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep1.setText("Expanding ARK File...");
			pos = pos + 30;

			lblStep2 = new JLabel();
			lblStep2.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep2.setText("");
			pos = pos + 30;

			lblStep3 = new JLabel();
			lblStep3.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep3.setText("");
			pos = pos + 30;

			lblStep4 = new JLabel();
			lblStep4.setBounds(new Rectangle(8, pos, 320, 23));
			lblStep4.setText("");
			pos = pos + 30;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnClose(), null);
			jContentPane.add(lblExplanation, null);
			jContentPane.add(lblStep1, null);
			jContentPane.add(lblStep2, null);
			jContentPane.add(lblStep3, null);
			jContentPane.add(lblStep4, null);

		}
		return jContentPane;
	}

	private JButton getBtnClose() {
		if (btnClose == null) {
			btnClose = new JButton();
			btnClose.setBounds(new Rectangle(254, 273, 74, 22));
			btnClose.setText("Close");
			btnClose.setFont(GuitarWizardMain.REGFONT);
			btnClose.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doClose();
				}
			});
		}
		return btnClose;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"