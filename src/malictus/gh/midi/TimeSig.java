package malictus.gh.midi;

/**
 * A class that represents a MIDI time signatue event
 *
 * by Jim Halliday
 * malictus@malictus.net
 */
public class TimeSig {
	public int val1;
	public int val2;
	public int val3;
	public int val4;

	public TimeSig(int val1, int val2, int val3, int val4) {
		this.val1 = val1;
		this.val2 = val2;
		this.val3 = val3;
		this.val4 = val4;
	}

}
