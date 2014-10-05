package malictus.gh.audio;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import javax.swing.*;
import javax.sound.*;
import javax.sound.sampled.*;

import malictus.gh.*;

/**
 * AudioPlayer
 * A class for playing a VGS file along with an optional WAV click file
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class AudioPlayer {

	private File vgsFile = null;
	private File wavFile = null;

	//all the numbers in this section refer to the VGS file
	private int duration = -1;	//in millis
	private int numStreams = -1;	//between 4 and 6
	private int samprate = -1;	//sample rate for regular streams
	private int bassrate = -1;	//sample rate for bass stream, if different than samprate

	private int currOffset = -1;
	private int stopTime = -1;
	private boolean isPlaying = false;
	private boolean isPlayingFlag = false;

	SourceDataLine	lineBacking = null;
	SourceDataLine	lineBass = null;

	float srFactor = 1;
	int diff = 0;

	//MUST be divisible by 3584
	private static final int BUFFER_SIZE = 3584;

	public static final int SOUND_AUDIO = 1;
	public static final int SOUND_BEATS = 2;
	public static final int SOUND_NOTES = 3;
	private int leftChannel = SOUND_AUDIO;
	private int rightChannel = SOUND_AUDIO;

	double vagstate1 = 0;
	double vagstate2 = 0;
	double vagstate3 = 0;
	double vagstate4 = 0;
	double vagstate5= 0;
	double vagstate6 = 0;
	double vagstate1NEW = 0;
	double vagstate2NEW = 0;
	double vagstate3NEW = 0;
	double vagstate4NEW = 0;
	double vagstate5NEW = 0;
	double vagstate6NEW = 0;

	double[][] filter =  {
		{ 0.0, 0.0 },
		{ 60.0 / 64.0,  0.0 },
		{ 115.0 / 64.0, -52.0 / 64.0 },
		{ 98.0 / 64.0, -55.0 / 64.0 },
		{ 122.0 / 64.0, -60.0 / 64.0 }
	};

	PlayerUI parent;

	public AudioPlayer(File vgsFile, File wavFile, PlayerUI parent) throws Exception {
		this.vgsFile = vgsFile;
		this.wavFile = wavFile;
		this.parent = parent;
		//verify vgs file and read basic stream information
		RandomAccessFile rafVGS = new RandomAccessFile(vgsFile, "r");
		try {
			//skip to info about streams
			rafVGS.skipBytes(8);
			int counter = 1;
			//check to see how many valid streams there are
			Vector streaminfos = new Vector();
			while (counter < 7) {
				int sampRateStream = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks = (int)GHUtils.readNumber(rafVGS.readInt());
				//is this stream valid?
				if ( (sampRateStream != 0) && (numberOfBlocks != 0) ) {
					int[] streaminfo = new int[2];
					streaminfo[0] = sampRateStream;
					streaminfo[1] = numberOfBlocks;
					streaminfos.add(streaminfo);
				}
				counter = counter + 1;
			}
			if ( (streaminfos.size() < 4) || (streaminfos.size() > 6) ) {
				throw new Exception("Error reading VGS; unexpected stream number");
			}
			numStreams = streaminfos.size();
			//verify sample rate and get lowest valid number of blocks (sometimes some channels have 1 more)
			counter = 0;
			int lowest = 70000000;
			int samprate = ((int[])streaminfos.get(0))[0];
			while (counter < streaminfos.size()) {
				int[] streaminfo = (int[])streaminfos.get(counter);
				if (streaminfo[1] < lowest) {
					if (streaminfo[0] == samprate) {
						//sometimes number of blocks varies by one, so find lowest
						lowest = streaminfo[1];
					}
				}
				if (streaminfo[0] != samprate) {
					if (counter < 4) {
						throw new Exception("Sample rates are not the same " + streaminfo[0] + " " + samprate);
					}
				}
				counter = counter + 1;
			}
			this.samprate = samprate;
			if (streaminfos.size() > 4) {
				int bass = ((int[])streaminfos.get(4))[0];
				if (bass != this.samprate) {
					this.bassrate = bass;
				}
			}
			long numsamples = lowest * 28;
			float dura = (float)((float)numsamples / (float)samprate);
			dura = dura * 1000;	//millis
			this.duration = (int)dura;
			rafVGS.close();
		} catch (Exception err) {
			rafVGS.close();
			throw err;
		}
	}

	/**
	 * Needed to be done this way because we actually need this info BEFORE click track is created
	 */
	public static int getDurationFor(File vgsFile) throws Exception {
		//verify vgs file and read basic stream information
		RandomAccessFile rafVGS = null;
		try {
			rafVGS = new RandomAccessFile(vgsFile, "r");
			//skip to info about streams
			rafVGS.skipBytes(8);
			int counter = 1;
			//check to see how many valid streams there are
			Vector streaminfos = new Vector();
			while (counter < 7) {
				int sampRateStream = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks = (int)GHUtils.readNumber(rafVGS.readInt());
				//is this stream valid?
				if ( (sampRateStream != 0) && (numberOfBlocks != 0) ) {
					int[] streaminfo = new int[2];
					streaminfo[0] = sampRateStream;
					streaminfo[1] = numberOfBlocks;
					streaminfos.add(streaminfo);
				}
				counter = counter + 1;
			}
			if ( (streaminfos.size() < 4) || (streaminfos.size() > 6) ) {
				throw new Exception("Error reading VGS; unexpected stream number");
			}
			//verify sample rate and get lowest valid number of blocks (sometimes some channels have 1 more)
			counter = 0;
			int lowest = 70000000;
			int samprate = ((int[])streaminfos.get(0))[0];
			while (counter < streaminfos.size()) {
				int[] streaminfo = (int[])streaminfos.get(counter);
				if (streaminfo[1] < lowest) {
					if (streaminfo[0] == samprate) {
						//sometimes number of blocks varies by one, so find lowest
						lowest = streaminfo[1];
					}
				}
				if (streaminfo[0] != samprate) {
					if (counter < 4) {
						throw new Exception("Sample rates are not the same " + streaminfo[0] + " " + samprate);
					}
				}
				counter = counter + 1;
			}
			long numsamples = lowest * 28;
			float dura = (float)((float)numsamples / (float)samprate);
			dura = dura * 1000;	//millis
			rafVGS.close();
			return (int)dura;
		} catch (Exception err) {
			if (rafVGS != null) {
				rafVGS.close();
			}
			throw err;
		}
	}

	public static void addSilence(File vgsFile, int amount) throws Exception {
		if (amount == 0) {
			return;
		}
		RandomAccessFile rafVGS = null;
		rafVGS = new RandomAccessFile(vgsFile, "rw");
		try {
			//skip to info about streams
			rafVGS.skipBytes(8);
			int counter = 1;
			//check to see how many valid streams there are
			Vector streaminfos = new Vector();
			while (counter < 7) {
				int sampRateStream = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks = (int)GHUtils.readNumber(rafVGS.readInt());
				//is this stream valid?
				if ( (sampRateStream != 0) && (numberOfBlocks != 0) ) {
					int[] streaminfo = new int[2];
					streaminfo[0] = sampRateStream;
					streaminfo[1] = numberOfBlocks;
					streaminfos.add(streaminfo);
				}
				counter = counter + 1;
			}
			if ( (streaminfos.size() < 4) || (streaminfos.size() > 6) ) {
				throw new Exception("Error reading VGS; unexpected stream number");
			}
			int streams = streaminfos.size();
			//verify sample rate and get lowest valid number of blocks (sometimes some channels have 1 more)
			counter = 0;
			int lowest = 70000000;
			int samprate = ((int[])streaminfos.get(0))[0];
			while (counter < streaminfos.size()) {
				int[] streaminfo = (int[])streaminfos.get(counter);
				if (streaminfo[1] < lowest) {
					if (streaminfo[0] == samprate) {
						//sometimes number of blocks varies by one, so find lowest
						lowest = streaminfo[1];
					}
				}
				if (streaminfo[0] != samprate) {
					if (counter < 4) {
						throw new Exception("Sample rates are not the same " + streaminfo[0] + " " + samprate);
					}
				}
				counter = counter + 1;
			}
			boolean sepBass = false;
			if (streaminfos.size() > 4) {
				int bass = ((int[])streaminfos.get(4))[0];
				if (bass != samprate) {
					sepBass = true;
				}
			}

			float secsToAdd = amount / 1000f;
			float bytesToAdd = secsToAdd * samprate * 2f;
			int blocksToAdd = (int)(bytesToAdd / (28f * 2));
			if ((blocksToAdd % 2) != 0) {
				blocksToAdd = blocksToAdd + 1;
			}

			rafVGS.seek(8);
			counter = 0;
			while (counter < streams) {
				int sampRateStream = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks = (int)GHUtils.readNumber(rafVGS.readInt());
				if (sepBass && (counter > 3)) {
					numberOfBlocks = numberOfBlocks + (int)(blocksToAdd / 2f);
				} else {
					numberOfBlocks = numberOfBlocks + blocksToAdd;
				}
				rafVGS.seek(rafVGS.getFilePointer() - 4);
				rafVGS.write(GHUtils.writeNumber(numberOfBlocks));
				counter = counter + 1;
			}
			int amt = 0;
			if (blocksToAdd < 0) {
				//subtract content
				boolean switcher = false;
				while (blocksToAdd < 0) {
					if (!sepBass) {
						amt = amt + (16 * streams);
					} else {
						if (switcher) {
							amt = amt + (16 * 4);
							switcher = false;
						} else {
							amt = amt + (16 * streams);
							switcher = true;
						}
					}
					blocksToAdd = blocksToAdd + 1;
				}
				//move the rest of the file up
				rafVGS.seek(128 + amt);
				long curpos = rafVGS.getFilePointer();
				amount = (int) ((float)((rafVGS.length() - curpos) / 65546f)) + 4;
				byte[] buf = new byte[65536];
				while ((curpos + 65536) < rafVGS.length()) {
					int x = rafVGS.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error while inserting DTB file");
					}
					rafVGS.seek(curpos - amt);
					rafVGS.write(buf);
					rafVGS.seek(curpos + 65536);
					curpos = rafVGS.getFilePointer();
				}
				//finish up any remaining bytes
				if (rafVGS.length() != curpos) {
					buf = new byte[(int)(rafVGS.length() - curpos)];
					int x = rafVGS.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error 2 while inserting DTB file");
					}
					rafVGS.seek(curpos - amt);
					rafVGS.write(buf);
				}
				//remove anything after this
				long startofnewfile = rafVGS.getFilePointer();
				rafVGS.setLength(startofnewfile);
				rafVGS.close();
			} else {
				//add content
				//here we will create a new file, then copy it over the old one and delete the new file
				//first, create new temp file
				File newTemp = File.createTempFile("newTemp", ".vgs", GuitarWizardMain.TempDir);
				newTemp.deleteOnExit();
				RandomAccessFile newRAF = new RandomAccessFile(newTemp, "rw");
				//now copy header
				byte[] header = new byte[128];
				rafVGS.seek(0);
				rafVGS.read(header);
				newRAF.write(header);
				//now add silence
				boolean switcher = false;
				byte[] thing = new byte[16];
				while (blocksToAdd > 0) {
					int innercounter = 0;
					while (innercounter < streams) {
						thing[1] = (byte)innercounter;
						if (!sepBass) {
							newRAF.write(thing);
						} else {
							if (switcher && (innercounter > 3)) {
								//do nothing
							} else {
								newRAF.write(thing);
							}
						}
						innercounter = innercounter + 1;
					}
					if (switcher) {
						switcher = false;
					} else {
						switcher = true;
					}
					blocksToAdd = blocksToAdd - 1;
				}
				//copy rest of original file
				byte[] buf = new byte[65536];
				rafVGS.seek(128);
				long curpos = 128;
				while ((curpos + 65536) < rafVGS.length()) {
					int x = rafVGS.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error while inserting DTB file");
					}
					newRAF.write(buf);
					curpos = rafVGS.getFilePointer();
				}
				//finish up any remaining bytes
				if (rafVGS.length() != curpos) {
					buf = new byte[(int)(rafVGS.length() - curpos)];
					int x = rafVGS.read(buf);
					if (x != buf.length) {
						throw new Exception("Read error 2 while inserting DTB file");
					}
					newRAF.write(buf);
				}
				//now make original file zero length
	    	    rafVGS.setLength(0);
	    	    //close rafs
	    	    rafVGS.close();
	    	    newRAF.close();
				//now copy contents of new file to old one
	    	    FileChannel srcChannel = new FileInputStream(newTemp).getChannel();
	    	    FileChannel dstChannel = new FileOutputStream(vgsFile).getChannel();
	    	    long x = dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				if (x != srcChannel.size()) {
	    	    	throw new Exception("File not transferred correctly " + x);
	    	    }
				srcChannel.close();
	    	    dstChannel.close();
				//now delete new file and cleanup
				newTemp.delete();
				//DONE
			}
		} catch (Exception err) {
			err.printStackTrace();
			rafVGS.close();
			throw err;
		}
	}

	/**
	 * Needed to be done this way because we actually need this info BEFORE click track is created
	 */
	public static float getSampRateFor(File vgsFile) throws Exception {
		//verify vgs file and read basic stream information
		RandomAccessFile rafVGS = null;
		try {
			rafVGS = new RandomAccessFile(vgsFile, "r");
			//skip to info about streams
			rafVGS.skipBytes(8);
			int counter = 1;
			//check to see how many valid streams there are
			Vector streaminfos = new Vector();
			while (counter < 7) {
				int sampRateStream = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks = (int)GHUtils.readNumber(rafVGS.readInt());
				//is this stream valid?
				if ( (sampRateStream != 0) && (numberOfBlocks != 0) ) {
					int[] streaminfo = new int[2];
					streaminfo[0] = sampRateStream;
					streaminfo[1] = numberOfBlocks;
					streaminfos.add(streaminfo);
				}
				counter = counter + 1;
			}
			if ( (streaminfos.size() < 4) || (streaminfos.size() > 6) ) {
				throw new Exception("Error reading VGS; unexpected stream number");
			}
			//verify sample rate and get lowest valid number of blocks (sometimes some channels have 1 more)
			counter = 0;
			int lowest = 70000000;
			int samprate = ((int[])streaminfos.get(0))[0];
			while (counter < streaminfos.size()) {
				int[] streaminfo = (int[])streaminfos.get(counter);
				if (streaminfo[1] < lowest) {
					if (streaminfo[0] == samprate) {
						//sometimes number of blocks varies by one, so find lowest
						lowest = streaminfo[1];
					}
				}
				if (streaminfo[0] != samprate) {
					if (counter < 4) {
						throw new Exception("Sample rates are not the same " + streaminfo[0] + " " + samprate);
					}
				}
				counter = counter + 1;
			}
			rafVGS.close();
			return (float)samprate;
		} catch (Exception err) {
			if (rafVGS != null) {
				rafVGS.close();
			}
			throw err;
		}
	}

	public int getDuration() {
		return this.duration;
	}

	public int getOffset() {
		return this.currOffset;
	}

	public void setDiff(int iAmt) {
		this.diff = iAmt;
	}

	public void stop() {
		if (isPlaying) {
			isPlaying = false;
			while (isPlayingFlag == false) {
				//wait for thread to finish
				try {
					Thread.sleep(20);
				} catch (Exception err) {}
			}
			isPlayingFlag = false;
		}
	}

	public void play(int starttime, int stoptime) {
		if (this.isPlaying()) {
			stop();
		}
		this.stopTime = stoptime;
		this.currOffset = starttime;
		isPlaying = true;
		Runnable q = new Runnable() {
            public void run() {
            	playSamples();
            }
        };
        Thread t = new Thread(q);
        t.start();
	}

	public void discard() {
		if (lineBacking != null) {
			lineBacking.stop();
			lineBacking.drain();
			lineBacking.close();
		}
		if (numStreams > 4) {
			if (lineBass != null) {
				lineBass.stop();
				lineBass.drain();
				lineBass.close();
			}
		}
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setChannels(int leftChan, int rightChan) throws Exception {
		if ( (leftChan == AudioPlayer.SOUND_BEATS) || (leftChan == AudioPlayer.SOUND_NOTES)  ||
				(rightChan == AudioPlayer.SOUND_BEATS) || (rightChan == AudioPlayer.SOUND_NOTES) ) {
			if (wavFile == null) {
				throw new Exception("No WAV file is specified");
			}
		}
		if ( (leftChan < 1) || (leftChan > 3) ||
			(rightChan < 1) || (rightChan > 3) ) {
			throw new Exception("Incorrect channel specifications");
		}
		this.leftChannel = leftChan;
		this.rightChannel = rightChan;
	}

	public void setSRFactor(float newSRFactor) {
		this.srFactor = newSRFactor;
	}

	private void playSamples() {
		try {
			AudioFormat	audioFormatBacking = new AudioFormat(samprate * srFactor, 16, 2, true, false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormatBacking);
			lineBacking = (SourceDataLine)AudioSystem.getLine(info);
			lineBacking.open(audioFormatBacking);
			if (numStreams > 4) {
				if (this.bassrate != -1) {
					AudioFormat	audioFormatBass = new AudioFormat(bassrate * srFactor, 16, 2, true, false);
					DataLine.Info infoBass = new DataLine.Info(SourceDataLine.class, audioFormatBass);
					lineBass = (SourceDataLine)AudioSystem.getLine(infoBass);
					lineBass.open(audioFormatBass);
				} else {
					lineBass = (SourceDataLine)AudioSystem.getLine(info);
					lineBass.open(audioFormatBacking);
				}
			}
		} catch (Exception err) {
			isPlaying = false;
			isPlayingFlag = false;
			parent.endOfSegment();
			err.printStackTrace();
			return;
		}

		lineBacking.start();
		if (numStreams > 4) {
			lineBass.start();
		}

		RandomAccessFile rafVGS = null;
		RandomAccessFile rafWAV = null;

		try {
			rafVGS = new RandomAccessFile(vgsFile, "r");
			if (wavFile != null) {
				rafWAV = new RandomAccessFile(wavFile, "r");
				rafWAV.seek( 0x24 + 8 + (int)((((float)currOffset) / 1000f) * samprate * 2f * 2f));
				//adjust to start of an actual sample
				if ((rafWAV.getFilePointer() % 2) != 0) {
					rafWAV.skipBytes(1);
				}
				if ((rafWAV.getFilePointer() % 4) != 0) {
					rafWAV.skipBytes(2);
				}
			}

			int xx = currOffset + this.diff;
			float off = xx / 1000f;
			off = off * this.samprate;
			off = off / 28f;
			int offs = (int)off;
			boolean switcher = false;
			boolean hasBass = false;
			int switchamount = 2;
			if (this.bassrate != -1) {
				switcher = true;
			}
			if (this.numStreams > 4) {
				hasBass = true;
			}
			int amt = offs * 16 * this.numStreams;
			if (switcher) {
				if (numStreams == 5) {
					amt = (int)((float)amt * (9f/10f));	//9 out of 10 streams actually there
					if ((amt % 16) != 0) {
						amt = amt - (amt % 16);	//move to real sample place if hit on wrong one
					}

				} else {
					amt = ((int)((float)amt * (5f/6f))) + (16*5);	//10 out of 12 streams actually there
					if ((amt % 16) != 0) {
						amt = amt - (amt % 16);	//move to real sample place if hit on wrong one
					}
				}
			}
			if (amt >= 0) {
				rafVGS.seek(128 + (int)amt);
			} else {
				rafVGS.seek(128);
			}

			//if using multi-sample rate, we still need to make sure we're in right place by checking flags
			if (switcher) {
				byte flagcheck = 0;
				//after this concludes we should be ready to start 'real' reading
				while (flagcheck != (numStreams - 1)) {
					rafVGS.skipBytes(1);
					flagcheck = rafVGS.readByte();
					rafVGS.skipBytes(14);
				}
				rafVGS.skipBytes(16 * 4);
			}

			/************* NOW READY TO START READING SAMPLES *****************/
			//final output byte arrays
			byte[]	abData1 = new byte[BUFFER_SIZE * 2];
			byte[]	abData2 = new byte[BUFFER_SIZE * 2];
			//VGS blocks in/out blocks for decoding
			//first left channel
			byte[] blockin1 = new byte[16];
			short[] blockout1 = new short[28];
			byte[] blockin2 = new byte[16];
			short[] blockout2 = new short[28];
			byte[] bassin = new byte[16];
			short[] bassout = new short[28];
			//and right channel
			byte[] blockin1NEW = new byte[16];
			short[] blockout1NEW = new short[28];
			byte[] blockin2NEW = new byte[16];
			short[] blockout2NEW = new short[28];
			byte[] bassinNEW = new byte[16];
			short[] bassoutNEW = new short[28];
			//final byte arrays for lines
			//these three must stay the same size
			byte[] finalout = new byte[28 * 2 * 2];
			byte[] finalbassout = new byte[28 * 2 * 2];
			byte[] finaloutWAV = new byte[28 * 2 * 2];

			int vgsPos = 0;
			int bassPos = 0;

			while ((isPlaying == true)) {
				//first calculate new offset and check for end-of-segment
				float num = -1;
				if (this.wavFile != null) {
					num = ((rafWAV.getFilePointer() - 0x24 - 8) * 1000f) / (samprate * 2f * 2f);
				} else {
					if (switcher) {
						if (numStreams == 5) {
							num = (rafVGS.getFilePointer() - 128) / ((16 * numStreams) * (9f/10f));
						} else {
							num = (rafVGS.getFilePointer() - 128) / ((16 * numStreams) * (5f/6f));
						}
					} else {
						num = (rafVGS.getFilePointer() - 128) / (16 * numStreams);
					}
					num = num * 28f;
					num = num / samprate;
					num = num * 1000f;
					currOffset = (int)num;
				}
				currOffset = (int)num;
				boolean readBlock = true;
				int vgsOff = currOffset + this.diff;
				if (vgsOff < 0) {
					readBlock = false;
				}
				if (currOffset >= stopTime) {
					isPlaying = false;
					parent.endOfSegment();
				} else {
					if (rafVGS.getFilePointer() >= (rafVGS.length() - (28 * 6))) {
						readBlock = false;
					}
					if (readBlock) {
						//read VGS block
						int read1 = rafVGS.read(blockin1);
						int skipper = rafVGS.read(blockin1NEW);
						int read2 = rafVGS.read(blockin2);
						skipper = rafVGS.read(blockin2NEW);
						if (hasBass) {
							if (switcher) {
								if (switchamount == 2) {
									read1 = rafVGS.read(bassin);
									if (numStreams == 6) {
										rafVGS.read(bassinNEW);
									}
									switchamount = 1;
								} else {
									switchamount = 2;
								}
							} else {
								read1 = rafVGS.read(bassin);
								if (numStreams == 6) {
									rafVGS.read(bassinNEW);
								}
							}
						}
						if ( (read1 != 16) || (skipper != 16) || (read2 != 16) ) {	//eof
							isPlaying = false;
							parent.endOfSegment();
						} else {

							blockout1 = decodeVAGBlock(blockin1, 0);
							blockout2 = decodeVAGBlock(blockin2, 2);
							blockout1NEW = decodeVAGBlock(blockin1NEW, 1);
							blockout2NEW = decodeVAGBlock(blockin2NEW, 3);
							if (hasBass) {
								if (switcher) {
									if (switchamount == 1) {
										bassout = decodeVAGBlock(bassin, 4);
										if (numStreams == 6) {
											bassoutNEW = decodeVAGBlock(bassinNEW, 5);
										}
									}
								} else {
									bassout = decodeVAGBlock(bassin, 4);
									if (numStreams == 6) {
										bassoutNEW = decodeVAGBlock(bassinNEW, 5);
									}
								}
							}
							int innercounter = 0;
							while (innercounter < blockout1.length) {
								//read both streams to one array, but avoid distortion by dividing by 2
								blockout1[innercounter] = (short)(blockout1[innercounter] / 2f);
								blockout2[innercounter] = (short)(blockout2[innercounter] / 2f);

								blockout1NEW[innercounter] = (short)(blockout1NEW[innercounter] / 2f);
								blockout2NEW[innercounter] = (short)(blockout2NEW[innercounter] / 2f);
								if (hasBass) {
									//also reduce vol of bass to match
									if (switcher) {
										if (switchamount == 1) {
											bassout[innercounter] = (short)(bassout[innercounter] / 2f);
											if (numStreams == 6) {
												bassoutNEW[innercounter] = (short)(bassoutNEW[innercounter] / 2f);
											}
										}
									} else {
										bassout[innercounter] = (short)(bassout[innercounter] / 2f);
										if (numStreams == 6) {
											bassoutNEW[innercounter] = (short)(bassoutNEW[innercounter] / 2f);
										}
									}
								}
								blockout1[innercounter] = (short)(blockout1[innercounter] + blockout2[innercounter]);
								blockout1NEW[innercounter] = (short)(blockout1NEW[innercounter] + blockout2NEW[innercounter]);
								innercounter = innercounter + 1;
							}
						}
					}

					for (int jj = 0; jj < 28; jj++) {
						short candidate;
						short candidateNEW;
						if (readBlock) {
							candidate = blockout1[jj];
							candidateNEW = blockout1NEW[jj];
						} else {
							candidate = 0;
							candidateNEW = 0;
						}
						if (this.leftChannel == AudioPlayer.SOUND_AUDIO) {
							finalout[(jj * 4)] = (byte)(candidate & 0xff);
							finalout[(jj * 4) + 1] = (byte)((candidate >>> 8) & 0xff);
						} else {
							finalout[(jj * 4)] = 0;
							finalout[(jj * 4) + 1] = 0;
						}

						if (this.rightChannel == AudioPlayer.SOUND_AUDIO) {
							finalout[(jj * 4) + 2] = (byte)(candidateNEW & 0xff);
							finalout[(jj * 4) + 3] = (byte)((candidateNEW >>> 8) & 0xff);
						} else {
							finalout[(jj * 4) + 2] = 0;
							finalout[(jj * 4) + 3] = 0;
						}

						if (hasBass) {
							//also reduce vol of bass to match
							if (switcher) {
								if (switchamount == 1) {
									short candidatebass;
									short candidatebassNEW;
									if (readBlock) {
										candidatebass = bassout[jj];
										candidatebassNEW = bassout[jj];
										if (numStreams == 6) {
											candidatebassNEW = bassoutNEW[jj];
										}
									} else {
										candidatebass = 0;
										candidatebassNEW = 0;
									}
									if (this.leftChannel == AudioPlayer.SOUND_AUDIO) {
										finalbassout[(jj * 4)] = (byte)(candidatebass & 0xff);
										finalbassout[(jj * 4) + 1] = (byte)((candidatebass >>> 8) & 0xff);
									} else {
										finalbassout[(jj * 4)] = 0;
										finalbassout[(jj * 4) + 1] = 0;
									}
									if (this.rightChannel == AudioPlayer.SOUND_AUDIO) {
										if (numStreams == 6) {
											finalbassout[(jj * 4) + 2] = (byte)(candidatebass & 0xff);
											finalbassout[(jj * 4) + 3] = (byte)((candidatebass >>> 8) & 0xff);
										} else {
											finalbassout[(jj * 4) + 2] = (byte)(candidatebassNEW & 0xff);
											finalbassout[(jj * 4) + 3] = (byte)((candidatebassNEW >>> 8) & 0xff);
										}
									} else {
										finalbassout[(jj * 4) + 2] = 0;
										finalbassout[(jj * 4) + 3] = 0;
									}
								}
							} else {
								short candidatebass;
								short candidatebassNEW;
								if (readBlock) {
									candidatebass = bassout[jj];
									candidatebassNEW = bassout[jj];
									if (numStreams == 6) {
										candidatebassNEW = bassoutNEW[jj];
									}
								} else {
									candidatebass = 0;
									candidatebassNEW = 0;
								}
								if (this.leftChannel == AudioPlayer.SOUND_AUDIO) {
									finalbassout[(jj * 4)] = (byte)(candidatebass & 0xff);
									finalbassout[(jj * 4) + 1] = (byte)((candidatebass >>> 8) & 0xff);
								} else {
									finalbassout[(jj * 4)] = 0;
									finalbassout[(jj * 4) + 1] = 0;
								}
								if (this.rightChannel == AudioPlayer.SOUND_AUDIO) {
									if (numStreams == 6) {
										finalbassout[(jj * 4) + 2] = (byte)(candidatebass & 0xff);
										finalbassout[(jj * 4) + 3] = (byte)((candidatebass >>> 8) & 0xff);
									} else {
										finalbassout[(jj * 4) + 2] = (byte)(candidatebassNEW & 0xff);
										finalbassout[(jj * 4) + 3] = (byte)((candidatebassNEW >>> 8) & 0xff);
									}
								} else {
									finalbassout[(jj * 4) + 2] = 0;
									finalbassout[(jj * 4) + 3] = 0;
								}
							}
						}
					}

					if (this.wavFile != null) {
						//read WAV file data
						rafWAV.read(finaloutWAV);
						int counter = 0;
						while (counter < (28 * 2 * 2)) {
							byte cand1 = finaloutWAV[counter];
							byte cand2 = finaloutWAV[counter + 1];
							byte cand3 = finaloutWAV[counter + 2];
							byte cand4 = finaloutWAV[counter + 3];

							if (this.leftChannel == AudioPlayer.SOUND_AUDIO) {
								finaloutWAV[counter] = 0;
								finaloutWAV[counter + 1] = 0;
							}
							if (this.rightChannel == AudioPlayer.SOUND_AUDIO) {
								finaloutWAV[counter + 2] = 0;
								finaloutWAV[counter + 3] = 0;
							}

							if (this.leftChannel == AudioPlayer.SOUND_BEATS) {
								finaloutWAV[counter] = cand3;
								finaloutWAV[counter + 1] = cand4;
							}
							if (this.rightChannel == AudioPlayer.SOUND_NOTES) {
								finaloutWAV[counter + 2] = cand1;
								finaloutWAV[counter + 3] = cand2;
							}
							finalout[counter] = (byte)(finalout[counter] + finaloutWAV[counter]);
							finalout[counter + 1] = (byte)(finalout[counter + 1] + finaloutWAV[counter + 1]);
							finalout[counter + 2] = (byte)(finalout[counter + 2] + finaloutWAV[counter + 2]);
							finalout[counter + 3] = (byte)(finalout[counter + 3] + finaloutWAV[counter + 3]);
							counter = counter + 4;
						}
					}

					System.arraycopy(finalout, 0, abData1, vgsPos, finalout.length);
					if (hasBass) {
						if (switcher) {
							if (switchamount == 1) {
								System.arraycopy(finalbassout, 0, abData2, bassPos, finalbassout.length);
								bassPos = bassPos + finalbassout.length;
							}
						} else {
							System.arraycopy(finalbassout, 0, abData2, bassPos, finalbassout.length);
							bassPos = bassPos + finalbassout.length;
						}
					}
					vgsPos = vgsPos + finalout.length;
					if (vgsPos == abData1.length) {
						vgsPos = 0;
						lineBacking.write(abData1, 0, abData1.length);
					}

					if (hasBass) {
						if (switcher) {
							if (switchamount == 1) {
								if (bassPos == abData2.length) {
									bassPos = 0;
									lineBass.write(abData2, 0, abData2.length);
								}
							}
						} else {
							if (bassPos == abData2.length) {
								bassPos = 0;
								lineBass.write(abData2, 0, abData2.length);
							}
						}
					}
				}
			}
			if (isPlaying == false) {
				lineBacking.flush();
				lineBacking.stop();
				if (hasBass) {
					lineBass.flush();
					lineBass.stop();
				}
				//set flag to trigger next stream
				isPlayingFlag = true;
			}
			isPlaying = false;	//just in case
			rafVGS.close();
			if (rafWAV != null) {
				rafWAV.close();
			}
		} catch (Exception err) {
			isPlaying = false;
			isPlayingFlag = false;
			parent.endOfSegment();
			try {
				rafVGS.close();
				if (rafWAV != null) {
					rafWAV.close();
				}
			} catch (Exception er) {
				er.printStackTrace();
			}
			err.printStackTrace();
		}
	}

	short[] decodeVAGBlock(byte[] in, int flagcheck) throws Exception {
		double[] s = new double[2];

		if (flagcheck == 0) {
			s[0] = vagstate1;
			s[1] = vagstate2;
		} else if (flagcheck == 2) {
			s[0] = vagstate3;
			s[1] = vagstate4;
		} else if (flagcheck == 4){
			s[0] = vagstate5;
			s[1] = vagstate6;
		} else if (flagcheck == 1) {
			s[0] = vagstate1NEW;
			s[1] = vagstate2NEW;
		} else if (flagcheck == 3){
			s[0] = vagstate3NEW;
			s[1] = vagstate4NEW;
		} else if (flagcheck == 5){
			s[0] = vagstate5NEW;
			s[1] = vagstate6NEW;
		}

		int	predictor = highnibble(in[0]);
		int	shift = lownibble(in[0]);
		int flag = in[1];
		if (flag != flagcheck) {
			System.out.println(flag + " " + flagcheck);
			throw new Exception();
		}
		if (predictor > 4) {
			throw new Exception();
		}

		double[] samples = new double[28];
		short[] outshorts = new short[28];

		//we now have in and out ready to go
        for ( int i = 0; i < 28; i += 2 ) {
        	int numb = (i/2) + 2;
            int ss = ( in[numb] & 0xf ) << 12;
            if ( (ss & 0x8000) != 0 )
                ss |= 0xffff0000;
            samples[i] = (double) ( ss >> shift  );
            ss = ( in[numb] & 0xf0 ) << 8;
            if ( (ss & 0x8000) != 0 )
                ss |= 0xffff0000;
            samples[i+1] = (double) ( ss >> shift  );
        }

        for ( int i = 0; i < 28; i++ ) {
            samples[i] = samples[i] + s[0] * filter[predictor][0] + s[1] * filter[predictor][1];
            s[1] = s[0];
			s[0] = samples[i];
			outshorts[i] = (short) ( samples[i] + 0.5 );
        }

        if (flagcheck == 0) {
        	vagstate1 = s[0];
        	vagstate2 = s[1];
        } else if (flagcheck == 2) {
        	vagstate3 = s[0];
        	vagstate4 = s[1];
        } else if (flagcheck == 4) {
        	vagstate5 = s[0];
        	vagstate6 = s[1];
        } else if (flagcheck == 1) {
        	vagstate1NEW = s[0];
        	vagstate2NEW = s[1];
        } else if (flagcheck == 3) {
        	vagstate3NEW = s[0];
        	vagstate4NEW = s[1];
        } else if (flagcheck == 5) {
        	vagstate5NEW = s[0];
        	vagstate6NEW = s[1];
        }

		return outshorts;
	}

	private int highnibble(int a) {
		return (a >> 4) & 15;
	}

	private int lownibble(int a) {
		return a & 15;
	}

}
