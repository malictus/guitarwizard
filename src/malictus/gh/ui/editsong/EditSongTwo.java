package malictus.gh.ui.editsong;

import javax.swing.*;
import java.util.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JButton;
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
 * EditSongTwo
 * Step 2 in edit song
 * Choose how to proceed with handling audio and MIDI
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class EditSongTwo extends JDialog {

	/******************** UI COMPONENTS *************************/
	protected JPanel jContentPane = null;
	protected JButton btnCancel = null;
	private JButton btnNext = null;
	private JLabel lblWhatToDo = null;
	private JRadioButton radNothing = null;
	private JRadioButton radAdjustSync = null;
	private JRadioButton radNew = null;

	GWMainWindow theParent;
	EditSongOne esong1;

	public EditSongTwo(EditSongOne editsongone, GWMainWindow parent) {
		super(parent);
		theParent = parent;
		esong1 = editsongone;
		this.setSize(new Dimension(441, 208));
		GHUtils.centerWindow(this);
		initialize();
	}

	private void doCancel() {
		GHUtils.cleanupTempDir();
		this.setVisible(false);
	}

	private void doNext() {
		this.setVisible(false);
		try {
			if (this.radNew.isSelected()) {
				//go on to choose audio step
				new EditSongThree(esong1, theParent);
			} else if (this.radNothing.isSelected()) {
				//skip all audio/MIDI pages
				new EditSongSix(esong1, theParent);
			} else if (this.radAdjustSync.isSelected()) {
				//skip to edit sync
				new EditSongFive(esong1, theParent);
			}
		} catch (Exception err) {
			err.printStackTrace();
        	GHUtils.cleanupTempDir();
        	JOptionPane.showMessageDialog(this, "ERROR going to next step", "Error going to next step", JOptionPane.ERROR_MESSAGE);
        	return;
		}
	}

	private void initialize() {
		this.setTitle("Audio/MIDI options");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        radNothing.setSelected(true);
        this.setVisible(true);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			int pos = 12;
			lblWhatToDo = new JLabel();
			lblWhatToDo.setBounds(new Rectangle(9, pos, 503, 19));
			lblWhatToDo.setFont(GuitarWizardMain.REGFONT);
			lblWhatToDo.setText("Choose how to handle audio/MIDI for this song by making a selection below");
			pos = pos + 25;

			radNothing = new JRadioButton();
			radNothing.setBounds(new Rectangle(9, pos, 403, 19));
			radNothing.setFont(GuitarWizardMain.REGFONT);
			radNothing.setText("Keep existing audio and MIDI");
			pos = pos + 25;

			radAdjustSync = new JRadioButton();
			radAdjustSync.setBounds(new Rectangle(9, pos, 403, 19));
			radAdjustSync.setFont(GuitarWizardMain.REGFONT);
			radAdjustSync.setText("Keep existing audio and MIDI, but adjust sync");
			pos = pos + 25;

			radNew = new JRadioButton();
			radNew.setBounds(new Rectangle(9, pos, 403, 19));
			radNew.setFont(GuitarWizardMain.REGFONT);
			radNew.setText("Replace audio and/or MIDI with new files");
			pos = pos + 25;

			ButtonGroup bgrp = new ButtonGroup();
			bgrp.add(radNothing);
			bgrp.add(radAdjustSync);
			bgrp.add(radNew);

			jContentPane.add(lblWhatToDo, null);
			jContentPane.add(radNothing, null);
			jContentPane.add(radAdjustSync, null);
			jContentPane.add(radNew, null);
			jContentPane.add(getBtnCancel(), null);
			jContentPane.add(getBtnNext(), null);
		}
		return jContentPane;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new Rectangle(389-40, 233-80, 74, 22));
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
			btnNext.setBounds(new Rectangle(162-20, 209-80, 114, 47));
			btnNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doNext();
				}
			});
		}
		return btnNext;
	}

}