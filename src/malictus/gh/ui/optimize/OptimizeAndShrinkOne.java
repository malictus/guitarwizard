package malictus.gh.ui.optimize;

import malictus.gh.ui.*;
import malictus.gh.*;
import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

/**
 * OptimizeAndShrinkOne
 * Step 1 in optimize and shrink
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class OptimizeAndShrinkOne extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnOptimize = null;
	private JLabel lblExplanation = null;
	private JCheckBox chkIntro = null;
	private JCheckBox chkBonusVideo = null;
	private JCheckBox chkMenuMusic = null;
	private JCheckBox chkPracticeMode = null;
	private JCheckBox chkTutorial = null;
	private JCheckBox chkCredits = null;

	GWMainWindow theParent;

	public OptimizeAndShrinkOne(GWMainWindow parent) {
		super(parent);
		theParent = parent;
		this.setLocation(parent.getX() + 5, parent.getY() + 5);
		initialize();
	}

	private void doCancel() {
		this.setVisible(false);
	}

	private void doOptimize() {
		this.setVisible(false);
		new OptimizeAndShrinkTwo(theParent, this.chkIntro.isSelected(), this.chkBonusVideo.isSelected(),
				this.chkMenuMusic.isSelected(), this.chkPracticeMode.isSelected(),
				this.chkTutorial.isSelected(), this.chkCredits.isSelected());
	}

	private void initialize() {
		this.setTitle("Optimize and Shrink");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setSize(new Dimension(341, 333));
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        doVersionCheck();
        this.setVisible(true);
	}

	private void doVersionCheck() {
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH80) {
			this.chkBonusVideo.setEnabled(false);
		}
		if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
			this.chkPracticeMode.setEnabled(false);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblExplanation = new JLabel();
			lblExplanation.setBounds(new Rectangle(6, 5, 322, 45));
			lblExplanation.setText("<html><center>All checked items will be REMOVED and replaced with silence. If nothing is selected to be removed, your archive will still be optimized so that changing it will be faster in the future.</center></html>");

			int pos = 79;
			chkIntro = new JCheckBox();
			chkIntro.setText("Intro Video");
			chkIntro.setFont(GuitarWizardMain.REGFONT);
			chkIntro.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			chkBonusVideo = new JCheckBox();
			chkBonusVideo.setText("Bonus Videos");
			chkBonusVideo.setFont(GuitarWizardMain.REGFONT);
			chkBonusVideo.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			chkMenuMusic = new JCheckBox();
			chkMenuMusic.setText("Menu and Background Music");
			chkMenuMusic.setFont(GuitarWizardMain.REGFONT);
			chkMenuMusic.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			chkPracticeMode = new JCheckBox();
			chkPracticeMode.setText("Practice Mode for all Songs");
			chkPracticeMode.setFont(GuitarWizardMain.REGFONT);
			chkPracticeMode.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			chkTutorial = new JCheckBox();
			chkTutorial.setText("Tutorial");
			chkTutorial.setFont(GuitarWizardMain.REGFONT);
			chkTutorial.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			chkCredits = new JCheckBox();
			chkCredits.setText("Credits Music");
			chkCredits.setFont(GuitarWizardMain.REGFONT);
			chkCredits.setBounds(new Rectangle(10, pos, 313, 19));
			pos = pos + 23;

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnOptimize(), null);
			jContentPane.add(lblExplanation, null);
			jContentPane.add(chkIntro);
			jContentPane.add(chkBonusVideo);
			jContentPane.add(chkMenuMusic);
			jContentPane.add(chkPracticeMode);
			jContentPane.add(chkTutorial);
			jContentPane.add(chkCredits);

		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(254, 273, 74, 22));
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

	/**
	 * This method initializes btnOptimize
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOptimize() {
		if (btnOptimize == null) {
			btnOptimize = new JButton();
			btnOptimize.setText("<html><h4>Optimize and Shrink</h4></html>");
			btnOptimize.setBounds(new Rectangle(102, 249, 114, 47));
			btnOptimize.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doOptimize();
				}
			});
		}
		return btnOptimize;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"