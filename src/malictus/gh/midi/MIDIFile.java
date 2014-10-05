package malictus.gh.midi;

import malictus.gh.*;
import java.io.*;
import java.util.*;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * MIDIFile
 *
 * An object that represents a single MIDI/chart file.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class MIDIFile extends JDialog {

	//UI components
	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String finishedString = "";

	static final public String FINISHED_SUCCESSFULLY = "DONE";

	File theFile;
	File clickTrack = null;

	public MIDIFile(JDialog parent, File inputFile) throws Exception {
		super(parent);
		theFile = inputFile;
	}

	public String getFinishedString() {
		return finishedString;
	}

	public File getCreatedClickTrack() {
		return clickTrack;
	}

/********************************************* VERIFY SECTION ***********************/

	/**
	 * This step verifies that the MIDI is valid and gets rid of any junk events
	 * it also removes bad channels and rearranges all the channels into the appropriate order
	 * and also prompts the user about any cleanup that is necessary or suggested
	 */
	public void verify() throws Exception {
		this.setTitle("Verifying MIDI file...");
        this.setSize(new java.awt.Dimension(370,88));
        GHUtils.centerWindow(this);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
		Runnable q = new Runnable() {
            public void run() {
            	doVerify();
            }
        };
        Tasker lTask = new Tasker(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doVerify() {
		prgProg.setIndeterminate(true);
		int runningStatus = -1;
		int runningChannel = -1;
		RandomAccessFile raf = null;
		Vector OriginalMIDIChannels = new Vector();
		Vector NewMIDIChannels = new Vector();
		try {
			raf = new RandomAccessFile(theFile, "r");
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading file";
			return;
		}

		try {
			//read input MIDI file, and fill vector with events
			String id = readID(raf);
			if (!id.equals("MThd")) {
				throw new Exception("Invalid MIDI header");
			}
			int size = (int)GHUtils.readBigEndNumber(raf.readInt());
			if (size != 6) {
				throw new Exception("Invalid MIDI header");
			}
			int format = raf.readUnsignedShort();
			if (format != 1) {
				throw new Exception("Invalid MIDI header");
			}
			int numTracks = raf.readUnsignedShort();
			if ((numTracks < 2) || (numTracks) > 50) {
				throw new Exception("Invalid MIDI header - " + numTracks + " tracks found");
			}
			int division = raf.readUnsignedShort();
			int counter = 0;
			while (counter < numTracks) {
				//System.out.println("_---_");
				MIDIChannel chan = new MIDIChannel();
				OriginalMIDIChannels.add(chan);
				id = readID(raf);
				size = (int)GHUtils.readBigEndNumber(raf.readInt());
				if (!(id.equals("MTrk"))) {
					throw new Exception("Invalid MIDI header");
				}
				long curpos = raf.getFilePointer();
				int curDelta = 0;

				while (raf.getFilePointer() < (curpos + size)) {
					//read a single event
					int delta = readDelta(raf);
					curDelta = curDelta + delta;
					int eventType = raf.readUnsignedByte();
					if (eventType == 255) {
						//meta event
						int subType = raf.readUnsignedByte();
						if (subType == 3) {
							int textlength = readDelta(raf);
							String stringVal = "";
							int innercounter = 0;
							while (innercounter < textlength) {
								stringVal = stringVal + (char)raf.read();
								innercounter = innercounter + 1;
							}
							//add trackname event
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_TYPE_TRACKNAME;
							even.eventText = stringVal;
							//System.out.println("EVENT - delta:" + delta + " - type: TRACKNAME - value: " + stringVal);
						} else if (subType == 4) {
							//only 'shakin' has one of these; we can safely ignore it
							int textlength = readDelta(raf);
							String stringVal = "";
							int innercounter = 0;
							while (innercounter < textlength) {
								stringVal = stringVal + (char)raf.read();
								innercounter = innercounter + 1;
							}
							//System.out.println("EVENT - delta:" + delta + " - type: ODD TEXT - value:" + stringVal);
						} else if (subType == 88) {
							int test = raf.readUnsignedByte();
							if (test != 4) {
								throw new Exception("Unrecognized MIDI check");
							}
							int byte1 = raf.readUnsignedByte();
							int byte2 = raf.readUnsignedByte();
							int byte3 = raf.readUnsignedByte();
							int byte4 = raf.readUnsignedByte();
							//add time sig event
							TimeSig ts = new TimeSig(byte1, byte2, byte3, byte4);
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_TYPE_TIMESIG;
							even.timeSig = ts;
							//System.out.println("EVENT - delta:" + delta + " - type: TIMESIG - value: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4);
						} else if (subType == 81) {
							int test = raf.readUnsignedByte();
							if (test != 3) {
								throw new Exception("Unrecognized MIDI check");
							}
							//add tempo event
							int byte1 = raf.readUnsignedByte();
							int byte2 = raf.readUnsignedByte();
							int byte3 = raf.readUnsignedByte();
							int numb = (byte1 << 16) + (byte2 << 8) + byte3;
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_TYPE_TEMPO;
							even.tempo = numb;
							//System.out.println("EVENT - delta:" + delta + " - type: TEMPO - value: " + numb);
						} else if (subType == 47) {
							int test = raf.readUnsignedByte();
							if (test != 0) {
								throw new Exception("Unrecognized MIDI check");
							}
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_TYPE_END_OF_TRACK;
							//System.out.println("EVENT - delta:" + delta + " - type: END OF TRACK");
						} else if (subType == 1) {
							//text event
							int textlength = readDelta(raf);
							String stringVal = "";
							int innercounter = 0;
							while (innercounter < textlength) {
								stringVal = stringVal + (char)raf.read();
								innercounter = innercounter + 1;
							}
							//add text event
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventText = stringVal;
							even.eventType = MIDIEvent.EVENT_TYPE_TEXT_EVENT;
							//System.out.println("EVENT - delta:" + delta + " - type: TEXT - value:" + stringVal);
						} else {
							//misc junk; these show up in some custom midis and can be ignored
							int thisdelta = readDelta(raf);
							raf.skipBytes(thisdelta);
							//System.out.println("EVENT - delta:" + delta + " - type: UNKNOWN (" + subType + ")");
						}
					} else {
						int x = highnibble(eventType);
						int y = lownibble(eventType);
						if (x < 8) {
							//use running status
							if (runningStatus == 8) {
								int byte1 = eventType;
								int byte2 = raf.readUnsignedByte();
								//don't normally show up, but we'll turn it into a note on event with velocity of zero, just in case
								NoteOnEvent noe = new NoteOnEvent(-1, byte1, 0);
								MIDIEvent even = new MIDIEvent();
								chan.midiEvents.add(even);
								even.totaldelta = curDelta;
								even.eventType = MIDIEvent.EVENT_NOTE_ON;
								even.noe = noe;
								//System.out.println("EVENT - delta:" + delta + " - type: NOTE OFF RUNNING - CHANNEL:" + runningChannel + " - NOTE NUMBER:" +  byte1 + " VELOCITY:" + byte2);
							} else if (runningStatus == 9) {
								int byte1 = eventType;
								int byte2 = raf.readUnsignedByte();
								//note on event
								NoteOnEvent noe = new NoteOnEvent(-1, byte1, byte2);
								MIDIEvent even = new MIDIEvent();
								chan.midiEvents.add(even);
								even.totaldelta = curDelta;
								even.eventType = MIDIEvent.EVENT_NOTE_ON;
								even.noe = noe;
								//System.out.println("EVENT - delta:" + delta + " - type: NOTE ON RUNNING - CHANNEL:" + runningChannel + " - NOTE NUMBER:" + byte1 + " VELOCITY:" + byte2);
							} else if (runningStatus == 10) {
								//ignore
								int byte1 = eventType;
								raf.skipBytes(1);
								//System.out.println("EVENT - delta:" + delta + " - type: UNSUPPORTED - CHANNEL:" + runningChannel + " - VAL:" + byte1);
							} else if (runningStatus == 12) {
								//ignore
								int byte1 = eventType;
								//System.out.println("EVENT - delta:" + delta + " - type: UNSUPPORTED - CHANNEL:" + runningChannel + " - VAL:" + byte1);
							} else {
								throw new Exception("Unrecognized MIDI with running status");
							}
						} else if (x == 8) {
							//note off
							int byte1 = raf.readUnsignedByte();
							int byte2 = raf.readUnsignedByte();
							//don't normally show up, but we'll turn it into a note on event with velocity of zero, just in case
							NoteOnEvent noe = new NoteOnEvent(y, byte1, 0);
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_NOTE_ON;
							even.noe = noe;
							//System.out.println("EVENT - delta:" + delta + " - type: NOTE OFF NONRUNNING - CHANNEL:" + y + " - NOTE NUMBER:" +  byte1 + " VELOCITY:" + byte2);
							runningStatus = 8;
							runningChannel = y;
						} else if (x == 9) {
							//note on
							int byte1 = raf.readUnsignedByte();
							int byte2 = raf.readUnsignedByte();
							NoteOnEvent noe = new NoteOnEvent(y, byte1, byte2);
							MIDIEvent even = new MIDIEvent();
							chan.midiEvents.add(even);
							even.totaldelta = curDelta;
							even.eventType = MIDIEvent.EVENT_NOTE_ON;
							even.noe = noe;
							//System.out.println("EVENT - delta:" + delta + " - type: NOTE ON NONRUNNING - CHANNEL:" + y + " - NOTE NUMBER:" + byte1 + " VELOCITY:" + byte2);
							runningStatus = 9;
							runningChannel = y;
						} else if ((x == 10) || (x == 11) || (x == 14)) {
							//unsupported in GH
							raf.skipBytes(2);
							//System.out.println("EVENT - delta:" + delta + " - type: UNSUPPORTED - CHANNEL:" + y);
							runningStatus = 10;
							runningChannel = y;
						} else if ((x == 12) || (x == 13)) {
							//unsupported in GH
							raf.skipBytes(1);
							//System.out.println("EVENT - delta:" + delta + " - type: UNSUPPORTED - CHANNEL:" + y);
							runningStatus = 12;
							runningChannel = y;
						} else {
							throw new Exception("Unrecognized MIDI event " + eventType + " " + delta + " " + x);
						}
					}
				}
				raf.seek(curpos);
				raf.skipBytes(size);
				counter = counter + 1;
			}
			raf.close();
		} catch (Exception err) {
			err.printStackTrace();
			try {
				raf.close();
			} catch (Exception errow) {}
			finishedString = "Error verifying MIDI file";
			return;
		}

		//now, our original MIDI channel vectors have been populated;
		//it's time to verify certain things about the archive and make any needed changes before moving on
		try {
			//create a brand new Vector of MIDIChannels, and copy over the channels we need in the right order

			/**************** TEMPO TRACK *******************/
			//MUST be first (we won't know what it's called otherwise)
			MIDIChannel tempoTrack = (MIDIChannel)OriginalMIDIChannels.get(0);
			//verify that last event is end event
			addEndEventIfNeeded(tempoTrack);
			addTrackNameIfNeeded(tempoTrack, "name");
			//now add to new vector
			NewMIDIChannels.add(tempoTrack);

			/***************  T1 GEMS/PART GUITAR ***********/
			MIDIChannel t1 = findChannelNamed("T1 GEMS", OriginalMIDIChannels);
			MIDIChannel pg = findChannelNamed("PART GUITAR", OriginalMIDIChannels);
			if ((t1 == null) && (pg == null)) {
				throw new Exception("Guitar MIDI channel not found");
			}
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				if (t1 == null) {
					MIDIEvent me = (MIDIEvent)pg.midiEvents.get(0);
					me.eventText = "T1 GEMS";
					addEndEventIfNeeded(pg);
					NewMIDIChannels.add(pg);
				} else {
					addEndEventIfNeeded(t1);
					NewMIDIChannels.add(t1);
				}
			} else {
				if (pg == null) {
					MIDIEvent me = (MIDIEvent)t1.midiEvents.get(0);
					me.eventText = "PART GUITAR";
					addEndEventIfNeeded(t1);
					NewMIDIChannels.add(t1);
				} else {
					addEndEventIfNeeded(pg);
					NewMIDIChannels.add(pg);
				}
			}

			/*****************  ANIM   ********************/
			if (GuitarWizardMain.Version == GuitarWizardMain.VERSION_GH1) {
				MIDIChannel anim = findChannelNamed("ANIM", OriginalMIDIChannels);
				if (anim != null) {
					addEndEventIfNeeded(anim);
					NewMIDIChannels.add(anim);
				}
			}

			/***************** PART GUITAR COOP **************/
			if (GuitarWizardMain.Version != GuitarWizardMain.VERSION_GH1) {
				MIDIChannel pgc = findChannelNamed("PART GUITAR COOP", OriginalMIDIChannels);
				if (pgc != null) {
					addEndEventIfNeeded(pgc);
					NewMIDIChannels.add(pgc);
				}
			}

			/**************** PART RHYTHM OR PART BASS *******/
			if (GuitarWizardMain.Version != GuitarWizardMain.VERSION_GH1) {
				MIDIChannel rhythm = findChannelNamed("PART RHYTHM", OriginalMIDIChannels);
				MIDIChannel bass = findChannelNamed("PART BASS", OriginalMIDIChannels);
				if ((rhythm == null) && (bass == null)) {
					//have to add one, we'll just pick bass for now
					MIDIChannel bassChan = new MIDIChannel();
					MIDIEvent start = new MIDIEvent();
					start.eventType = MIDIEvent.EVENT_TYPE_TRACKNAME;
					start.eventText = "PART BASS";
					start.totaldelta = 0;
					bassChan.midiEvents.add(start);
					MIDIEvent note1 = new MIDIEvent();
					note1.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe = new NoteOnEvent(0, 98, 100);
					note1.noe = noe;
					note1.totaldelta = 0;
					bassChan.midiEvents.add(note1);
					MIDIEvent note2 = new MIDIEvent();
					note2.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe2 = new NoteOnEvent(-1, 86, 100);
					note2.noe = noe2;
					note2.totaldelta = 0;
					bassChan.midiEvents.add(note2);
					MIDIEvent note3 = new MIDIEvent();
					note3.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe3 = new NoteOnEvent(-1, 74, 100);
					note3.noe = noe3;
					note3.totaldelta = 0;
					bassChan.midiEvents.add(note3);
					MIDIEvent note4 = new MIDIEvent();
					note4.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe4 = new NoteOnEvent(-1, 62, 100);
					note4.noe = noe4;
					note4.totaldelta = 0;
					bassChan.midiEvents.add(note4);

					MIDIEvent note5 = new MIDIEvent();
					note5.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe5 = new NoteOnEvent(0, 98, 0);
					note5.noe = noe5;
					note5.totaldelta = 48;
					bassChan.midiEvents.add(note5);
					MIDIEvent note6 = new MIDIEvent();
					note6.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe6 = new NoteOnEvent(-1, 86, 0);
					note6.noe = noe6;
					note6.totaldelta = 48;
					bassChan.midiEvents.add(note6);
					MIDIEvent note7 = new MIDIEvent();
					note7.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe7 = new NoteOnEvent(-1, 74, 0);
					note7.noe = noe7;
					note7.totaldelta = 48;
					bassChan.midiEvents.add(note7);
					MIDIEvent note8 = new MIDIEvent();
					note8.eventType = MIDIEvent.EVENT_NOTE_ON;
					NoteOnEvent noe8 = new NoteOnEvent(-1, 62, 0);
					note8.noe = noe8;
					note8.totaldelta = 48;
					bassChan.midiEvents.add(note8);
					MIDIEvent end = new MIDIEvent();
					end.eventType = MIDIEvent.EVENT_TYPE_END_OF_TRACK;
					end.totaldelta = 48;
					bassChan.midiEvents.add(end);

					NewMIDIChannels.add(bass);
				} else {
					//try bass channel first
					if (bass != null) {
						addEndEventIfNeeded(bass);
						NewMIDIChannels.add(bass);
					} else {
						addEndEventIfNeeded(rhythm);
						NewMIDIChannels.add(rhythm);
					}
				}
			}

			/*************** BAND BASS, DRUMS, SINGER *****************/
			if (GuitarWizardMain.Version != GuitarWizardMain.VERSION_GH1) {
				MIDIChannel bb = findChannelNamed("BAND BASS", OriginalMIDIChannels);
				if (bb != null) {
					addEndEventIfNeeded(bb);
					NewMIDIChannels.add(bb);
				}
				bb = findChannelNamed("BAND DRUMS", OriginalMIDIChannels);
				if (bb != null) {
					addEndEventIfNeeded(bb);
					NewMIDIChannels.add(bb);
				}
				bb = findChannelNamed("BAND SINGER", OriginalMIDIChannels);
				if (bb != null) {
					addEndEventIfNeeded(bb);
					NewMIDIChannels.add(bb);
				}
			}

			/*************** EVENTS **********************/
			MIDIChannel events = findChannelNamed("EVENTS", OriginalMIDIChannels);
			if (events == null) {
				events = new MIDIChannel();
				MIDIEvent trackname = new MIDIEvent();
				trackname.eventType = MIDIEvent.EVENT_TYPE_TRACKNAME;
				trackname.totaldelta = 0;
				trackname.eventText = "EVENTS";
				events.midiEvents.add(trackname);
			}
			addEndEventIfNeeded(events);
			//verify contains music start and end events
			if (GuitarWizardMain.Version != GuitarWizardMain.VERSION_GH1) {
				int innercounter = 0;
				boolean hasIt = false;
				while (innercounter < events.midiEvents.size()) {
					MIDIEvent m = (MIDIEvent)events.midiEvents.get(0);
					if (m.eventType == MIDIEvent.EVENT_TYPE_TEXT_EVENT) {
						if (m.eventText.equals("[music_start]")) {
							hasIt = true;
						}
					}
					innercounter = innercounter + 1;
				}
				if (!hasIt) {
					MIDIEvent musicstart = new MIDIEvent();
					musicstart.eventType = MIDIEvent.EVENT_TYPE_TEXT_EVENT;
					musicstart.totaldelta = 0;
					musicstart.eventText = "[music_start]";
					events.midiEvents.add(1, musicstart);
				}
				innercounter = 0;
				hasIt = false;
				while (innercounter < events.midiEvents.size()) {
					MIDIEvent m = (MIDIEvent)events.midiEvents.get(innercounter);
					if (m.eventType == MIDIEvent.EVENT_TYPE_TEXT_EVENT) {
						if (m.eventText.equals("[end]")) {
							hasIt = true;
						}
					}
					innercounter = innercounter + 1;
				}
				if (!hasIt) {
					MIDIEvent musicend = new MIDIEvent();
					musicend.eventType = MIDIEvent.EVENT_TYPE_TEXT_EVENT;
					MIDIEvent lastEvent = (MIDIEvent)events.midiEvents.get(events.midiEvents.size() - 1);
					musicend.totaldelta = lastEvent.totaldelta;
					musicend.eventText = "[end]";
					events.midiEvents.add(events.midiEvents.size() - 2, musicend);
				}
			} else {
				int innercounter = 0;
				boolean hasIt = false;
				while (innercounter < events.midiEvents.size()) {
					MIDIEvent m = (MIDIEvent)events.midiEvents.get(0);
					if (m.eventType == MIDIEvent.EVENT_TYPE_TEXT_EVENT) {
						if (m.eventText.equals("end")) {
							hasIt = true;
						}
					}
					innercounter = innercounter + 1;
				}
				if (!hasIt) {
					MIDIEvent musicend = new MIDIEvent();
					musicend.eventType = MIDIEvent.EVENT_TYPE_TEXT_EVENT;
					MIDIEvent lastEvent = (MIDIEvent)events.midiEvents.get(events.midiEvents.size() - 1);
					musicend.totaldelta = lastEvent.totaldelta;
					musicend.eventText = "end";
					events.midiEvents.add(events.midiEvents.size() - 2, musicend);
				}
			}
			NewMIDIChannels.add(events);

			/******************** TRIGGERS **************************/
			MIDIChannel triggers = findChannelNamed("TRIGGERS", OriginalMIDIChannels);
			if (triggers == null) {
				//have to add one
				triggers = new MIDIChannel();
				MIDIEvent start = new MIDIEvent();
				start.eventType = MIDIEvent.EVENT_TYPE_TRACKNAME;
				start.eventText = "TRIGGERS";
				start.totaldelta = 0;
				triggers.midiEvents.add(start);
				MIDIEvent end = new MIDIEvent();
				end.eventType = MIDIEvent.EVENT_TYPE_END_OF_TRACK;
				end.totaldelta = 0;
				triggers.midiEvents.add(end);
			} else {
				addEndEventIfNeeded(triggers);
			}
			NewMIDIChannels.add(triggers);

		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR verifying file";
			return;
		}

		//vectors are now ready to be read back out into fully cleaned up file, so let's do that now
		try {
			raf = new RandomAccessFile(theFile, "rw");
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading file";
			return;
		}
		try {
			//maintain only old header
			raf.seek(14);
			raf.setLength(14);
			//write out channels one by one
			int counter = 0;
			while (counter < NewMIDIChannels.size()) {
				String id = "MTrk";
				byte[] idBytes = id.getBytes("ISO-8859-1");
				raf.write(idBytes);
				//we'll come back here at the end
				long sizepos = raf.getFilePointer();
				raf.writeInt(0);
				MIDIChannel mc = (MIDIChannel)NewMIDIChannels.get(counter);
				int innercounter = 0;
				int prevdelta = 0;
				while (innercounter < mc.midiEvents.size()) {
					MIDIEvent mee = (MIDIEvent)mc.midiEvents.get(innercounter);
					//write delta first
					int totaldelta = mee.totaldelta;
					int delta = totaldelta - prevdelta;
					prevdelta = totaldelta;
					writeDelta(raf, delta);
					//now write event-specific info
					if (mee.eventType == MIDIEvent.EVENT_NOTE_ON) {
						NoteOnEvent n = mee.noe;
						if (n.channel == -1) {
							//running status
							raf.writeByte(GHUtils.convertToUnsignedByte(n.noteNumber));
							raf.writeByte(GHUtils.convertToUnsignedByte(n.velocity));
						} else {
							//not running status
							int number = (9 << 4) + n.channel;
							raf.writeByte(GHUtils.convertToUnsignedByte(number));
							raf.writeByte(GHUtils.convertToUnsignedByte(n.noteNumber));
							raf.writeByte(GHUtils.convertToUnsignedByte(n.velocity));
						}
					} else if (mee.eventType == MIDIEvent.EVENT_TYPE_END_OF_TRACK) {
						raf.writeByte(GHUtils.convertToUnsignedByte(255));
						raf.writeByte(GHUtils.convertToUnsignedByte(47));
						raf.writeByte(GHUtils.convertToUnsignedByte(0));
					} else if (mee.eventType == MIDIEvent.EVENT_TYPE_TEMPO) {
						raf.writeByte(GHUtils.convertToUnsignedByte(255));
						raf.writeByte(GHUtils.convertToUnsignedByte(81));
						raf.writeByte(GHUtils.convertToUnsignedByte(3));
						int amt = mee.tempo;
						int byte1 = amt >> 16;
						amt = amt - (amt >> 16);
						int byte2 = amt >> 8;
						amt = amt - (amt >> 8);
						int byte3 = amt;
						raf.writeByte(GHUtils.convertToUnsignedByte(byte1));
						raf.writeByte(GHUtils.convertToUnsignedByte(byte2));
						raf.writeByte(GHUtils.convertToUnsignedByte(byte3));
					} else if (mee.eventType == MIDIEvent.EVENT_TYPE_TEXT_EVENT) {
						raf.writeByte(GHUtils.convertToUnsignedByte(255));
						raf.writeByte(GHUtils.convertToUnsignedByte(1));
						writeDelta(raf, mee.eventText.length());
						raf.writeBytes(mee.eventText);
					} else if (mee.eventType == MIDIEvent.EVENT_TYPE_TIMESIG) {
						raf.writeByte(GHUtils.convertToUnsignedByte(255));
						raf.writeByte(GHUtils.convertToUnsignedByte(88));
						raf.writeByte(GHUtils.convertToUnsignedByte(4));
						raf.writeByte(GHUtils.convertToUnsignedByte(mee.timeSig.val1));
						raf.writeByte(GHUtils.convertToUnsignedByte(mee.timeSig.val2));
						raf.writeByte(GHUtils.convertToUnsignedByte(mee.timeSig.val3));
						raf.writeByte(GHUtils.convertToUnsignedByte(mee.timeSig.val4));
					} else if (mee.eventType == MIDIEvent.EVENT_TYPE_TRACKNAME) {
						raf.writeByte(GHUtils.convertToUnsignedByte(255));
						raf.writeByte(GHUtils.convertToUnsignedByte(3));
						writeDelta(raf, mee.eventText.length());
						raf.writeBytes(mee.eventText);
					} else {
						throw new Exception("Unrecognized MIDI event");
					}
					innercounter = innercounter + 1;
				}
				//now, go back and write size var
				long curpos = raf.getFilePointer();
				long amt = raf.getFilePointer() - sizepos - 4;
				raf.seek(sizepos);
				raf.writeInt((int)amt);
				raf.seek(curpos);
				counter = counter + 1;
			}
			//now rewrite total number of channels to original header
			raf.seek(10);
			raf.writeShort(NewMIDIChannels.size());
			raf.close();
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "Error verifying MIDI file";
			try {
				raf.close();
			} catch (Exception errow) {}
			return;
		}
		finishedString = MIDIFile.FINISHED_SUCCESSFULLY;
	}

	private void addEndEventIfNeeded(MIDIChannel chan) {
		//first, remove any end of track event not occuring at actual end of track
		int counter = chan.midiEvents.size() - 2;
		while (counter >= 0) {
			MIDIEvent me = (MIDIEvent)(chan.midiEvents.get(counter));
			if (me.eventType == MIDIEvent.EVENT_TYPE_END_OF_TRACK) {
				chan.midiEvents.remove(counter);
			}
			counter = counter - 1;
		}

		MIDIEvent x = (MIDIEvent)(chan.midiEvents.get(chan.midiEvents.size() - 1));
		if (x.eventType != MIDIEvent.EVENT_TYPE_END_OF_TRACK) {
			MIDIEvent end = new MIDIEvent();
			end.eventType = MIDIEvent.EVENT_TYPE_END_OF_TRACK;
			end.totaldelta = x.totaldelta;
			chan.midiEvents.add(end);
		}
		return;
	}

	private void addTrackNameIfNeeded(MIDIChannel chan, String name) {
		//first, remove any track name events not at beginning
		int counter = chan.midiEvents.size() - 1;
		while (counter > 0) {
			MIDIEvent me = (MIDIEvent)(chan.midiEvents.get(counter));
			if (me.eventType == MIDIEvent.EVENT_TYPE_TRACKNAME) {
				chan.midiEvents.remove(counter);
			}
			counter = counter - 1;
		}

		MIDIEvent x = (MIDIEvent)(chan.midiEvents.get(0));
		if (x.eventType != MIDIEvent.EVENT_TYPE_TRACKNAME) {
			MIDIEvent start = new MIDIEvent();
			start.eventType = MIDIEvent.EVENT_TYPE_TRACKNAME;
			start.totaldelta = 0;
			chan.midiEvents.add(0, start);
		}
		return;
	}

	private MIDIChannel findChannelNamed(String channelName, Vector channels) {
		int counter = 0;
		while (counter < channels.size()) {
			MIDIChannel mc = (MIDIChannel)(channels.get(counter));
			if (mc.midiEvents.size() > 0) {
				MIDIEvent nameEvent = (MIDIEvent)(mc.midiEvents.get(0));
				if (nameEvent.eventType == MIDIEvent.EVENT_TYPE_TRACKNAME) {
					String name = nameEvent.eventText;
					if (name.equals(channelName)) {
						return mc;
					}
				}
			}
			counter = counter + 1;
		}
		return null;
	}

/*********************** CLICK TRACK SECTION ***********************************/

	public void createClickTrack(final File inputFile, final int duration, final float sampRate) {
		this.setTitle("Creating click track...");
        this.setSize(new java.awt.Dimension(370,88));
        GHUtils.centerWindow(this);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);
        this.clickTrack = inputFile;
		Runnable q = new Runnable() {
            public void run() {
            	doCreateClickTrack(duration, sampRate);
            }
        };
        Tasker lTask = new Tasker(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	/**
	 * Create a click track based on the MIDI file, given the total file duration and sample rate
	 */
	private void doCreateClickTrack(int duration, float sampRate) {
		//assume input file has been created and is empty and ready to write
		//also assume midi file has been verified, and errant events have been deleted
		prgProg.setIndeterminate(true);

		Vector tempos = new Vector();
		Vector tempobeats = new Vector();
		Vector beats = new Vector();
		Vector notes = new Vector();
		Vector clicks = new Vector();

		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(theFile, "r");
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading file";
			return;
		}
		try {
			raf.skipBytes(10);
			int numTracks = raf.readUnsignedShort();
			if ((numTracks < 2) || (numTracks) > 50) {
				throw new Exception("Invalid MIDI header - " + numTracks);
			}
			int division = raf.readUnsignedShort();
			lblProg.setText("Reading tempos");
			String id = readID(raf);
			int size = (int)GHUtils.readBigEndNumber(raf.readInt());
			if (!(id.equals("MTrk"))) {
				throw new Exception("Invalid tempo track header " + id);
			}
			long curpos = raf.getFilePointer();
			int curDelta = 0;
			int prevNote = -1;
			while (raf.getFilePointer() < (curpos + size)) {
				//read a single event
				int delta = readDelta(raf);
				curDelta = curDelta + delta;
				int eventType = raf.readUnsignedByte();
				if (eventType == 255) {
					//meta event
					int subType = raf.readUnsignedByte();
					if (subType == 3) {
						int textlength = readDelta(raf);
						String stringVal = "";
						int innercounter = 0;
						while (innercounter < textlength) {
							stringVal = stringVal + (char)raf.read();
							innercounter = innercounter + 1;
						}
					} else if (subType == 88) {
						int test = raf.readUnsignedByte();
						if (test != 4) {
							throw new Exception("Unrecognized MIDI check");
						}
						raf.skipBytes(4);
					} else if (subType == 81) {
						int test = raf.readUnsignedByte();
						if (test != 3) {
							throw new Exception("Unrecognized MIDI check");
						}
						int byte1 = raf.readUnsignedByte();
						int byte2 = raf.readUnsignedByte();
						int byte3 = raf.readUnsignedByte();
						int numb = (byte1 << 16) + (byte2 << 8) + byte3;
						float thing = ((float)curDelta) / ((float)division);
						tempos.add(new Integer(numb));
						tempobeats.add(new Float(thing));
					} else if (subType == 47) {
						int test = raf.readUnsignedByte();
						if (test != 0) {
							throw new Exception("Unrecognized MIDI check");
						}
					} else if (subType == 1) {
						//text event
						int textlength = readDelta(raf);
						String stringVal = "";
						int innercounter = 0;
						while (innercounter < textlength) {
							stringVal = stringVal + (char)raf.read();
							innercounter = innercounter + 1;
						}
					} else {
						throw new Exception("Unrecognized MIDI subtype " + subType);
					}
				} else {
					throw new Exception("Unrecognized MIDI event in tempo track " + eventType + " " + delta);
				}
			}
			lblProg.setText("Reading guitar notes");
			//now read lead guitar notes only
			int runningStatus = -1;
			int runningChannel = -1;
			id = readID(raf);
			size = (int)GHUtils.readBigEndNumber(raf.readInt());
			if (!(id.equals("MTrk"))) {
				throw new Exception("Invalid lead guitar track header");
			}
			curpos = raf.getFilePointer();
			curDelta = 0;
			while (raf.getFilePointer() < (curpos + size)) {
				//read a single event
				int delta = readDelta(raf);
				curDelta = curDelta + delta;
				int eventType = raf.readUnsignedByte();
				if (eventType == 255) {
					//meta event
					int subType = raf.readUnsignedByte();
					if (subType == 3) {
						int textlength = readDelta(raf);
						String stringVal = "";
						int innercounter = 0;
						while (innercounter < textlength) {
							stringVal = stringVal + (char)raf.read();
							innercounter = innercounter + 1;
						}
						if ( (!(stringVal.equals("T1 GEMS"))) && (!(stringVal.equals("PART GUITAR"))) ) {
							throw new Exception("MIDI track is not lead guitar");
						}
					} else if (subType == 47) {
						int test = raf.readUnsignedByte();
						if (test != 0) {
							throw new Exception("Unrecognized MIDI check");
						}
					} else if (subType == 1) {
						//text event
						int textlength = readDelta(raf);
						String stringVal = "";
						int innercounter = 0;
						while (innercounter < textlength) {
							stringVal = stringVal + (char)raf.read();
							innercounter = innercounter + 1;
						}
					} else {
						throw new Exception("Unrecognized MIDI subtype " + subType);
					}
				} else {
					int x = highnibble(eventType);
					int y = lownibble(eventType);
					if (x < 8) {
						//use running status
						if (runningStatus == 8) {
							raf.skipBytes(1);
						} else if (runningStatus == 9) {
							//raf.skipBytes(1);
							int byte1 = eventType;
							int byte2 = raf.readUnsignedByte();
							if (byte2 != 0) {
								if ((byte1 > 95) && (byte1 < 101)) {
									if (prevNote != curDelta) {
										notes.add(new Float( (float)curDelta / (float)division ));
										prevNote = curDelta;
									}
								}
							}
						} else {
							throw new Exception("Unrecognized MIDI with running status");
						}
					} else if (x == 8) {
						//note off
						raf.skipBytes(2);
						runningStatus = 8;
						runningChannel = y;
					} else if (x == 9) {
						//note on
						int byte1 = raf.readUnsignedByte();
						int byte2 = raf.readUnsignedByte();
						if (byte2 != 0) {
							if ((byte1 > 95) && (byte1 < 101)) {
								if (prevNote != curDelta) {
									notes.add(new Float( (float)curDelta / (float)division ));
									prevNote = curDelta;
								}
							}
						}
						runningStatus = 9;
						runningChannel = y;
					} else {
						throw new Exception("Unrecognized MIDI event " + eventType + " " + delta);
					}
				}
			}
			raf.close();
		} catch (Exception err) {
			err.printStackTrace();
			try {
				raf.close();
			} catch (Exception errw) {
				errw.printStackTrace();
				finishedString = "ERROR reading file";
				return;
			}
			finishedString = "ERROR reading MIDI file";
			return;
		}
		lblProg.setText("Creating tempo vector");
		//now populate beats vector
		float pos = 0;
		beats.add(new Integer(0));
		int oldTempo = ((Integer)tempos.get(0)).intValue();
		float oldBeat = ((Float)tempobeats.get(0)).floatValue();
		int counter = 1;
		while (counter < tempos.size()) {
			int newTempo = ((Integer)tempos.get(counter)).intValue();
			float newBeat = ((Float)tempobeats.get(counter)).floatValue();
			int diff = (int)newBeat - (int)oldBeat;
			//a fractional move without a beat at all
			if (diff == 0) {
				//move up a fractional amount only
				pos = pos + (oldTempo * ( (newBeat - ((int)newBeat)) - (oldBeat - ((int)oldBeat))));
			} else {
				//at least one beat needs to be added
				//first, check to see if we need to move up a fraction or full beat to add first click
				if ( (((int)oldBeat) - oldBeat) != 0) {
					pos = pos + (oldTempo * (1-((oldBeat - ((int)oldBeat)))));
				} else {
					pos = pos + oldTempo;
				}
				//now add single click
				beats.add(new Integer((int)pos));
				//now add as many more clicks at this tempo as we need, minus the last one
				int innercounter = 0;
				while (innercounter < (diff - 2)) {
					pos = pos + oldTempo;
					beats.add(new Integer((int)pos));
					innercounter = innercounter + 1;
				}
				if (diff > 1) {
					pos = pos + oldTempo;
					beats.add(new Integer((int)pos));
				}
				if ( (((int)newBeat) - newBeat) != 0) {
					//fraction
					pos = pos + (oldTempo * (newBeat - ((int)newBeat)));
				}
			}
			oldBeat = newBeat;
			oldTempo = newTempo;
			counter = counter + 1;
		}
		try {
			//add more clicks up until full duration of song
			duration = duration * 1000;
			while (pos < (duration - 2)) {
				pos = pos + oldTempo;
				beats.add(new Integer((int)pos));
			}
			//in some cases, WAV file may be shorter than some of the MIDI notes, so here's we'll add enough
			//beats to finish out the song in those cases
			counter = 0;
			int high = 0;
			while (counter < notes.size()) {
				float note = ((Float)notes.get(counter)).floatValue();
				int not1 = (int)note;
				int not2 = not1 + 1;
				high = not2;
				counter = counter + 1;
			}
			counter = beats.size();
			while ( counter <= high ) {
				pos = pos + oldTempo;
				beats.add(new Integer((int)pos));
				counter = counter + 1;
			}
			if (duration <= pos) {
				duration = ((int)pos) + 2;
			}
			lblProg.setText("Creating note vector");
			//now we now know where the beats are, but we need to figure out where to add the note clicks
			counter = 0;
			while (counter < notes.size()) {
				float note = ((Float)notes.get(counter)).floatValue();
				int not1 = (int)note;
				int not2 = not1 + 1;
				float diff = note - not1;
				int val1 = ((Integer)beats.get(not1)).intValue();
				int val2 = ((Integer)beats.get(not2)).intValue();
				float amt = val1 + ((val2-val1) * diff);
				clicks.add(new Integer((int)amt));
				counter = counter + 1;
			}
			lblProg.setText("Creating sound file");
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading file";
			return;
		}
		//create blank WAV file of 'duration' (+ a sec or two) length
		File clicker = this.clickTrack;
		clicker.deleteOnExit();
		RandomAccessFile rafWav;
		try {
			rafWav = new RandomAccessFile(clicker, "rw");
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading file";
			return;
		}
		try {
			//number of sample bytes
			int numBytes = (int)(((float)duration / 1000000f) * sampRate * 2f * 2f);
			int riffLength = numBytes + 0x24;
			byte[] riffLengthBytes = GHUtils.writeNumber(riffLength);
			// RIFF HEADER
			byte[] riffHeader = { 'R', 'I', 'F', 'F', riffLengthBytes[0], riffLengthBytes[1], riffLengthBytes[2], riffLengthBytes[3] };
			rafWav.write(riffHeader);
			// NEXT PART
			byte[] nextPart = { 'W', 'A', 'V', 'E', 'f', 'm', 't', ' ', 0x10,0,0,0,1,0,2,0 };
			rafWav.write(nextPart);
			//sample rate
			rafWav.write(GHUtils.writeNumber((int)sampRate));
			//bytes per sec
			rafWav.write(GHUtils.writeNumber((int)sampRate * 2 * 2));
			//block align
			rafWav.write(GHUtils.write16BitNumber(4));
			//bits per sample
			rafWav.write(GHUtils.write16BitNumber(16));
			byte[] thing6 = { 'd', 'a', 't', 'a' };
			rafWav.write(thing6);
			//length of data chunk
			rafWav.write(GHUtils.writeNumber(numBytes));
			//data itself (all zeroes first)
			byte[] buf = new byte[1024];
			counter = 0;
			int stop = (int)((float)numBytes / 1024f);
			while (counter < stop) {
				rafWav.write(buf);
				counter = counter + 1;
			}
			int leftover = numBytes % 1024;
			byte[] left = new byte[leftover];
			rafWav.write(left);
			lblProg.setText("Inserting clicks");
			//put in all the clicks
			//first, read in the click sound file sample data to a byte array
			RandomAccessFile r = new RandomAccessFile(GuitarWizardMain.CLICKTRACK_FILE, "r");
			r.skipBytes(44);
			byte[] clickByte = new byte[(int)(GuitarWizardMain.CLICKTRACK_FILE.length() - 44)];
			r.read(clickByte);
			r.close();
			counter = 0;
			int amount = clicks.size() + beats.size();
			prgProg.setIndeterminate(false);
			this.prgProg.setMaximum(amount + 1);
			this.prgProg.setValue(0);
			while (counter < clicks.size()) {
				this.prgProg.setValue(this.prgProg.getValue() + 1);
				int time = ((Integer)clicks.get(counter)).intValue();
				rafWav.seek( 0x24 + 8 + (int)((((float)time) / 1000000f) * sampRate * 2f * 2f));
				//adjust to start of an actual sample
				if ((rafWav.getFilePointer() % 2) != 0) {
					rafWav.skipBytes(1);
				}
				if ((rafWav.getFilePointer() % 4) != 0) {
					rafWav.skipBytes(2);
				}
				int innercounter = 0;
				while (innercounter < clickByte.length) {
					rafWav.writeByte(clickByte[innercounter]);
					rafWav.writeByte(clickByte[innercounter + 1]);
					rafWav.skipBytes(2);
					innercounter = innercounter + 2;
				}
				counter = counter + 1;
			}
			lblProg.setText("Inserting beats");
			counter = 0;
			while (counter < beats.size()) {
				this.prgProg.setValue(this.prgProg.getValue() + 1);
				int time = ((Integer)beats.get(counter)).intValue();
				rafWav.seek( 0x24 + 8 + (int)((((float)time) / 1000000f) * sampRate * 2f * 2f) );
				//adjust to start of an actual sample
				if ((rafWav.getFilePointer() % 2) != 0) {
					rafWav.skipBytes(1);
				}
				if ((rafWav.getFilePointer() % 4) != 2) {
					rafWav.skipBytes(2);
				}
				int innercounter = 0;
				while (innercounter < clickByte.length) {
					rafWav.writeByte(clickByte[innercounter]);
					rafWav.writeByte(clickByte[innercounter + 1]);
					rafWav.skipBytes(2);
					innercounter = innercounter + 2;
				}
				counter = counter + 1;
			}
			rafWav.close();
		} catch (Exception err) {
			err.printStackTrace();
			finishedString = "ERROR reading MIDI file";
			try {
				rafWav.close();
			} catch (Exception errw) {
				errw.printStackTrace();
				finishedString = "ERROR reading MIDI file";
			}
			return;
		}
		this.clickTrack = clicker;
		this.finishedString = MIDIFile.FINISHED_SUCCESSFULLY;
	}

/****************************************** UI SECTION ****************************/

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblProg = new JLabel();
			lblProg.setBounds(new java.awt.Rectangle(7,5,300,16));
			lblProg.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			lblProg.setText("");

			prgProg = new JProgressBar();
			prgProg.setMinimum(0);
			prgProg.setMaximum(100);
			prgProg.setValue(0);
			prgProg.setIndeterminate(false);
			prgProg.setBounds(new java.awt.Rectangle(7,28,303,23));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblProg, null);
			jContentPane.add(prgProg, null);
		}
		return jContentPane;
	}

/************************************* UTILS SECTION ***************************************/

	private class Tasker extends TimerTask {
        java.util.Timer myTimer = null;
        public Tasker(java.util.Timer aTimer) {
            super();
            myTimer = aTimer;
        }
        public void run() {
            if (!finishedString.equals("")) {
            	theTimer.cancel();
            	setVisible(false);
            }
        }
	}

	private void writeDelta(RandomAccessFile raf, int delta) throws Exception {
		long buffer = delta & 0x7F;
        while((delta >>= 7) > 0){
        	buffer <<= 8;
            buffer |= ((delta & 0x7F) | 0x80);
        }
        while(true){
        	raf.writeByte((byte)buffer);
            if((buffer & 0x80) != 0){
            	buffer >>= 8;
            } else {
                break;
            }
        }
	}

	private int readDelta(RandomAccessFile raf) throws Exception {
		int finalval = raf.readUnsignedByte();
		int checker = finalval & 0x80;
		finalval = finalval & 0x7F;
		int val = -1;
		while (checker != 0) {
			val = raf.readUnsignedByte();
			checker = val & 0x80;
			val = val & 0x7F;
			finalval = (finalval << 7) + val;
		}
		return finalval;
	}

	private String readID(RandomAccessFile raf) throws Exception {
		byte[] id = new byte[4];
		raf.read(id);
		String idString = new String(id, "ISO-8859-1");
		return idString;
	}

	private int highnibble(int a) {
		return a >> 4;
	}

	private int lownibble(int a) {
		return a % 16;
	}

}
