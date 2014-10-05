package malictus.gh.midi;

/**
 * A class that represents a MIDI note on event
 *
 * by Jim Halliday
 * malictus@malictus.net
 */
public class NoteOnEvent {
	public int channel;
	public int noteNumber;
	public int velocity;

	public NoteOnEvent(int channel, int noteNumber, int velocity) {
		//use -1 if running status (no channel specified)
		this.channel = channel;
		this.noteNumber = noteNumber;
		this.velocity = velocity;
	}

}
