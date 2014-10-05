package malictus.gh.ui.editsong;

import javax.swing.*;
import malictus.gh.ui.*;
import java.awt.Rectangle;
import java.awt.Dimension;
import malictus.gh.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * EditSongPreviewTimes
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongPreviewTimes extends JDialog {

	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnSave = null;
	private JLabel lblStartTime = null;
	protected JTextField txtfStartTime = null;
	private JLabel lblEndTime = null;
	protected JTextField txtfEndTime = null;
	private JLabel lblDuration = null;

	private int startTime = -1;
	private int endTime = -1;
	private int duration = -1;

	boolean canceled = false;

	EditSongSix theParent;

	public EditSongPreviewTimes(EditSongSix parent, int startTime, int endTime, int duration) {
		super(parent);
		theParent = parent;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.setSize(new Dimension(203, 169));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		canceled = true;
		this.setVisible(false);
	}

	private void doSave() {
		int start = -1;
		int end = -1;
		try {
			start = GHUtils.convertMinutesSecondsMillisToMilliseconds(this.txtfStartTime.getText());
			end = GHUtils.convertMinutesSecondsMillisToMilliseconds(this.txtfEndTime.getText());
		} catch (Exception err) {
			JOptionPane.showMessageDialog(this, "Error: Incorrectly formatted time fields", "Incorrectly formatted time fields", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if ( (start >= end) || (start < 0) || (end <= 0) ) {
			JOptionPane.showMessageDialog(this, "Error: Incorrect time values entered", "Incorrect time values entered", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (end > duration) {
			JOptionPane.showMessageDialog(this, "Error: End value exceeds the duration for this song", "Incorrect end value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.startTime = start;
		this.endTime = end;
		canceled = false;
		this.setVisible(false);
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	private void initialize() {
		this.setTitle("Edit Preview Times");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        populate();
        this.setResizable(false);
        this.setVisible(true);
	}

	void populate() {
		this.txtfStartTime.setText(GHUtils.convertOffsetToMinutesSecondsMillis(startTime));
		this.txtfEndTime.setText(GHUtils.convertOffsetToMinutesSecondsMillis(endTime));
		this.lblDuration.setText("Total Song Duration: " + GHUtils.convertOffsetToMinutesSecondsMillis(duration));
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			int pos = 12;
			lblStartTime = new JLabel();
			lblStartTime.setBounds(new Rectangle(9, pos, 43, 19));
			lblStartTime.setText("Start:");
			lblStartTime.setFont(GuitarWizardMain.REGFONT);
			txtfStartTime = new JTextField();
			txtfStartTime.setDocument (new LimitedTextField(8));
			txtfStartTime.setFont(GuitarWizardMain.REGFONT);
			txtfStartTime.setBounds(new Rectangle(55, pos, 74, 21));
			pos = pos + 25;
			lblEndTime = new JLabel();
			lblEndTime.setBounds(new Rectangle(9, pos, 43, 19));
			lblEndTime.setText("End:");
			lblEndTime.setFont(GuitarWizardMain.REGFONT);
			txtfEndTime = new JTextField();
			txtfEndTime.setDocument (new LimitedTextField(8));
			txtfEndTime.setFont(GuitarWizardMain.REGFONT);
			txtfEndTime.setBounds(new Rectangle(55, pos, 74, 21));
			pos = pos + 25;

			lblDuration = new JLabel();
			lblDuration.setBounds(new Rectangle(9, pos, 243, 19));
			lblDuration.setText("Total Song Duration:");
			lblDuration.setFont(GuitarWizardMain.REGFONT);

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnSave(), null);
			jContentPane.add(lblStartTime, null);
			jContentPane.add(txtfStartTime, null);
			jContentPane.add(lblEndTime, null);
			jContentPane.add(txtfEndTime, null);
			jContentPane.add(lblDuration, null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(110, 99, 74, 22));
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
			btnSave.setBounds(new Rectangle(28, 99, 74, 22));
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSave();
				}
			});
		}
		return btnSave;
	}

}