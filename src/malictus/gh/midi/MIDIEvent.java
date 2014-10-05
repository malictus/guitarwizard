package malictus.gh.midi;

/**
 * A class that represents a single MIDI event
 *
 * by Jim Halliday
 * malictus@malictus.net
 */
public class MIDIEvent {
	//NOT raw delta value; this is delta calculated from beginning of file, not last note
	public int totaldelta = -1;
	public int eventType = -1;
	public String eventText = "";
	public TimeSig timeSig = null;
	public int tempo = -1;
	public NoteOnEvent noe = null;

	public static final int EVENT_TYPE_TRACKNAME = 1;
	public static final int EVENT_TYPE_TIMESIG = 2;
	public static final int EVENT_TYPE_TEMPO = 3;
	public static final int EVENT_TYPE_END_OF_TRACK = 4;
	public static final int EVENT_TYPE_TEXT_EVENT = 5;
	public static final int EVENT_NOTE_ON = 6;
}
