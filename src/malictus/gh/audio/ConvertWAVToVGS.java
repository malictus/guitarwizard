package malictus.gh.audio;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import javax.swing.*;
import javax.sound.*;
import javax.sound.sampled.*;

import malictus.gh.*;

/**
 * ConvertWAVToVGS
 * A class for converting only 44100 STEREO 16-bit PCM WAV files to VGS
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class ConvertWAVToVGS extends JDialog {

	private JPanel jContentPane = null;
	private JLabel lblProg = null;
	private JProgressBar prgProg = null;

	java.util.Timer theTimer = new java.util.Timer();
	private String finishedString = "";
	private File vgsFile;
	private File backingFile;
	private File guitFile;
	private File bassFile;

	static final public String FINISHED_SUCCESSFULLY = "DONE";

	double vagstate1 = 0;
	double vagstate2 = 0;
	double vagstate3 = 0;
	double vagstate4 = 0;
	double vagstate5 = 0;
	double vagstate6 = 0;
	double vagstate1NEW = 0;
	double vagstate2NEW = 0;
	double vagstate3NEW = 0;
	double vagstate4NEW = 0;
	double vagstate5NEW = 0;
	double vagstate6NEW = 0;

	double vagstate1a = 0;
	double vagstate2a = 0;
	double vagstate3a = 0;
	double vagstate4a = 0;
	double vagstate5a = 0;
	double vagstate6a = 0;
	double vagstate1NEWa = 0;
	double vagstate2NEWa = 0;
	double vagstate3NEWa = 0;
	double vagstate4NEWa = 0;
	double vagstate5NEWa = 0;
	double vagstate6NEWa = 0;

	double[][] filter =  {
			{ 0.0, 0.0 },
			{  -60.0 / 64.0, 0.0 },
	        { -115.0 / 64.0, 52.0 / 64.0 },
	        {  -98.0 / 64.0, 55.0 / 64.0 },
	        { -122.0 / 64.0, 60.0 / 64.0 }
	};

	public ConvertWAVToVGS(JDialog parent, File backing, File guit, File bass, File vgs) {
		super(parent);
		backingFile = backing;
		vgsFile = vgs;
		guitFile = guit;
		bassFile = bass;
		this.setTitle("Re-encoding " + vgs.getName());
        this.setSize(new java.awt.Dimension(370,88));
        GHUtils.centerWindow(this);
		initialize();
	}

	public String getFinishedString() {
		return finishedString;
	}

	private void initialize() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.setResizable(false);

		Runnable q = new Runnable() {
            public void run() {
            	doConvertToVGS();
            }
        };

        ConvertToVGSTask lTask = new ConvertToVGSTask(theTimer);
        theTimer.schedule(lTask, 0, 200);
        Thread t = new Thread(q);
        t.start();
        this.setVisible(true);
	}

	private void doConvertToVGS() {
		try {
			//examine wav files and find the longest one
			long len = backingFile.length();
			if (guitFile != null) {
				if (guitFile.length() > len) {
					len = guitFile.length();
				}
			}
			if (bassFile != null) {
				if (bassFile.length() > len) {
					len = bassFile.length();
				}
			}
			//now calculate the number of VGS sample blocks needed
			len = len - 0x24 - 8;
			//len is now number of sample bytes
			float floatlen = (float)len;
			floatlen = floatlen / 2f;
			//floatlen is now number of 16-bit samples
			floatlen = floatlen / 2f;
			//floatlen is now number of 16-bit samples PER CHANNEL (all files are stereo)
			floatlen = floatlen / 28f;
			len = (int)floatlen;
			//each sample block is 28 samples, so len is now number of blocks needed for VGS file
			int counter = 0;
			this.prgProg.setMaximum((int)len);
			this.prgProg.setValue(0);

			//open all the necessary files
			RandomAccessFile rafBack = null;
			RandomAccessFile rafGuit = null;
			RandomAccessFile rafBass = null;
			RandomAccessFile rafVGS = null;
			try {
				rafBack = new RandomAccessFile(backingFile, "r");
				rafVGS = new RandomAccessFile(vgsFile, "rw");
				if (guitFile != null) {
					rafGuit = new RandomAccessFile(guitFile, "r");
				}
				if (bassFile != null) {
					rafBass = new RandomAccessFile(bassFile, "r");
				}

				//rewrite VGS headers to reflect new info
				//skip to info about streams
				rafVGS.skipBytes(8);
				//write info about backing stream (both channels)
				rafVGS.write(GHUtils.writeNumber(44100));
				rafVGS.write(GHUtils.writeNumber(len + 1));	//+1 since we add end block
				rafVGS.write(GHUtils.writeNumber(44100));
				rafVGS.write(GHUtils.writeNumber(len + 1));
				//write info about guit stream (both channel)
				rafVGS.write(GHUtils.writeNumber(44100));
				rafVGS.write(GHUtils.writeNumber(len + 1));
				rafVGS.write(GHUtils.writeNumber(44100));
				rafVGS.write(GHUtils.writeNumber(len + 1));
				//do we have bass channel(s)?
				int numOfBassChannels = 0;
				long pos = rafVGS.getFilePointer();
				int sampRateStream1 = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks1 = (int)GHUtils.readNumber(rafVGS.readInt());
				int sampRateStream2 = (int)GHUtils.readNumber(rafVGS.readInt());
				int numberOfBlocks2 = (int)GHUtils.readNumber(rafVGS.readInt());
				if ( (sampRateStream1 != 0) && (numberOfBlocks1 != 0) ) {
					numOfBassChannels = 1;
					if ( (sampRateStream2 != 0) && (numberOfBlocks2 != 0) ) {
						numOfBassChannels = 2;
					}
				}
				rafVGS.seek(pos);
				if (numOfBassChannels > 0) {
					rafVGS.write(GHUtils.writeNumber(44100));
					rafVGS.write(GHUtils.writeNumber(len));
				}
				if (numOfBassChannels > 1) {
					rafVGS.write(GHUtils.writeNumber(44100));
					rafVGS.write(GHUtils.writeNumber(len));
				}
				//queue up rafVGS
				rafVGS.seek(128);
				//remove everything after
				rafVGS.setLength(128);
				//queue up other files
				rafBack.seek(0x24 + 8);
				if (rafGuit != null) {
					rafGuit.seek(0x24 + 8);
				}
				if (rafBass != null) {
					rafBass.seek(0x24 + 8);
				}

				//start sample reading/writing loop
				byte[] outArray = new byte[16];
				short[] inArrayL = new short[28];
				short[] inArrayR = new short[28];
				while (counter < len) {
					//backing tracks
					populateShortArrays(rafBack, inArrayL, inArrayR);
					outArray = encodeVAGBlock(inArrayL, 0);
					rafVGS.write(outArray);
					outArray = encodeVAGBlock(inArrayR, 1);
					rafVGS.write(outArray);

					//guit tracks
					if (rafGuit == null) {
						outArray = new byte[16];
						outArray[1] = 2;
						rafVGS.write(outArray);
						outArray[1] = 3;
						rafVGS.write(outArray);
					} else {
						populateShortArrays(rafGuit, inArrayL, inArrayR);
						outArray = encodeVAGBlock(inArrayL, 2);
						rafVGS.write(outArray);
						outArray = encodeVAGBlock(inArrayR, 3);
						rafVGS.write(outArray);
					}

					//bass
					if (numOfBassChannels > 0) {
						if (rafBass == null) {
							outArray = new byte[16];
							outArray[1] = 4;
							rafVGS.write(outArray);
							if (numOfBassChannels == 2) {
								outArray = new byte[16];
								outArray[1] = 5;
								rafVGS.write(outArray);
							}
						} else {
							populateShortArrays(rafBass, inArrayL, inArrayR);
							outArray = encodeVAGBlock(inArrayL, 4);
							rafVGS.write(outArray);
							if (numOfBassChannels == 2) {
								outArray = encodeVAGBlock(inArrayR, 5);
								rafVGS.write(outArray);
							}
						}
					}

					this.prgProg.setValue(counter);
					counter = counter + 1;
				}
				//add 'finished' sample blocks
				outArray = new byte[16];
				outArray[1] = (byte)0x80;
				rafVGS.write(outArray);
				outArray[1] = (byte)0x81;
				rafVGS.write(outArray);
				outArray[1] = (byte)0x82;
				rafVGS.write(outArray);
				outArray[1] = (byte)0x83;
				rafVGS.write(outArray);
				if (numOfBassChannels > 0) {
					outArray[1] = (byte)0x84;
					rafVGS.write(outArray);
				}
				if (numOfBassChannels > 1) {
					outArray[1] = (byte)0x85;
					rafVGS.write(outArray);
				}

				//done!
				rafBack.close();
				if (rafGuit != null) {
					rafGuit.close();
				}
				if (rafBass != null) {
					rafBass.close();
				}
				rafVGS.close();
				finishedString = ConvertWAVToVGS.FINISHED_SUCCESSFULLY;
			} catch (Exception err) {
				if (rafBack != null) {
					rafBack.close();
				}
				if (rafGuit != null) {
					rafGuit.close();
				}
				if (rafVGS != null) {
					rafVGS.close();
				}
				if (rafBass != null) {
					rafBass.close();
				}
				throw err;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() == null) {
    			finishedString = "Error converting audio.";
    		} else if (e.getMessage().equals("null")) {
    			finishedString = "Error converting audio.";
    		} else {
    			finishedString = e.getMessage();
    		}
			return;
		}
	}

	byte[] encodeVAGBlock(short[] in, int flagcheck) throws Exception {
		byte[] out = new byte[16];
		out[1] = (byte)flagcheck;

		int i, j;
	    double[][] buffer = new double[28][5];
	    double min = 1e10;
	    double[] max = new double[5];
	    double ds;
	    int min2;
	    int shift_mask;
	    double s_0, s_1, s_2;
	    double[] stat = new double[2];
	    int predict_nr = 0;
	    double[] d_samples = new double[28];

	    if (flagcheck == 0) {
	    	stat[0] = vagstate1;
	    	stat[1] = vagstate2;
		} else if (flagcheck == 2) {
			stat[0] = vagstate3;
			stat[1] = vagstate4;
		} else if (flagcheck == 4){
			stat[0] = vagstate5;
			stat[1] = vagstate6;
		} else if (flagcheck == 1) {
			stat[0] = vagstate1NEW;
			stat[1] = vagstate2NEW;
		} else if (flagcheck == 3){
			stat[0] = vagstate3NEW;
			stat[1] = vagstate4NEW;
		} else if (flagcheck == 5){
			stat[0] = vagstate5NEW;
			stat[1] = vagstate6NEW;
		}

	    s_1 = 0;
	    s_2 = 0;
	    for ( i = 0; i < 5; i++ ) {
	        max[i] = 0.0;
	        s_1 = stat[0];
	        s_2 = stat[1];
	        for ( j = 0; j < 28; j ++ ) {
	            s_0 = (double) in[j];
	            if ( s_0 > 30719.0 )
	                s_0 = 30719.0;
	            if ( s_0 < - 30720.0 )
	                s_0 = -30720.0;
	            ds = s_0 + s_1 * filter[i][0] + s_2 * filter[i][1];
	            buffer[j][i] = ds;
	            if ( Math.abs( ds ) > max[i] ) {
	                max[i] = Math.abs( ds );
	            }
	            s_2 = s_1;
                s_1 = s_0;
	        }

	        if ( max[i] < min ) {
	            min = max[i];
	            predict_nr = i;
	        }
	        if ( min <= 7 ) {
	            predict_nr = 0;
	            break;
	        }
	    }
	    stat[0] = s_1;
	    stat[1] = s_2;

	    if (flagcheck == 0) {
        	vagstate1 = stat[0];
        	vagstate2 = stat[1];
        } else if (flagcheck == 2) {
        	vagstate3 = stat[0];
        	vagstate4 = stat[1];
        } else if (flagcheck == 4) {
        	vagstate5 = stat[0];
        	vagstate6 = stat[1];
        } else if (flagcheck == 1) {
        	vagstate1NEW = stat[0];
        	vagstate2NEW = stat[1];
        } else if (flagcheck == 3) {
        	vagstate3NEW = stat[0];
        	vagstate4NEW = stat[1];
        } else if (flagcheck == 5) {
        	vagstate5NEW = stat[0];
        	vagstate6NEW = stat[1];
        }

	    for ( i = 0; i < 28; i++ ) {
	        d_samples[i] = buffer[i][predict_nr];
	    }

	    min2 = ( int ) min;
	    shift_mask = 0x4000;
	    int shift_factor = 0;

	    while( shift_factor < 12 ) {
	        if ( ( shift_mask  & ( min2 + ( shift_mask >> 3 ) ) ) != 0 ) {
	            break;
	        }
	        (shift_factor)++;
	        shift_mask = shift_mask >> 1;
	    }

	    //pack
	    int di;
	    double s_0a;
	    double[] statA = new double[2];

	    if (flagcheck == 0) {
	    	statA[0] = vagstate1a;
	    	statA[1] = vagstate2a;
		} else if (flagcheck == 2) {
			statA[0] = vagstate3a;
			statA[1] = vagstate4a;
		} else if (flagcheck == 4){
			statA[0] = vagstate5a;
			statA[1] = vagstate6a;
		} else if (flagcheck == 1) {
			statA[0] = vagstate1NEWa;
			statA[1] = vagstate2NEWa;
		} else if (flagcheck == 3){
			statA[0] = vagstate3NEWa;
			statA[1] = vagstate4NEWa;
		} else if (flagcheck == 5){
			statA[0] = vagstate5NEWa;
			statA[1] = vagstate6NEWa;
		}

	    short[] four_bit = new short[28];

	    for ( i = 0; i < 28; i++ ) {
	        s_0a = d_samples[i] + statA[0] * filter[predict_nr][0] + statA[1] * filter[predict_nr][1];
	        ds = s_0a * (double) ( 1 << shift_factor );

	        di = ( (int) ds + 0x800 ) & 0xfffff000;

	        if ( di > 32767 )
	            di = 32767;
	        if ( di < -32768 )
	            di = -32768;

	        four_bit[i] = (short) di;

	        di = di >> shift_factor;
	        statA[1] = statA[0];
	        statA[0] = (double) di - s_0a;

	    }

	    if (flagcheck == 0) {
        	vagstate1a = statA[0];
        	vagstate2a = statA[1];
        } else if (flagcheck == 2) {
        	vagstate3a = statA[0];
        	vagstate4a = statA[1];
        } else if (flagcheck == 4) {
        	vagstate5a = statA[0];
        	vagstate6a = statA[1];
        } else if (flagcheck == 1) {
        	vagstate1NEWa = statA[0];
        	vagstate2NEWa = statA[1];
        } else if (flagcheck == 3) {
        	vagstate3NEWa = statA[0];
        	vagstate4NEWa = statA[1];
        } else if (flagcheck == 5) {
        	vagstate5NEWa = statA[0];
        	vagstate6NEWa = statA[1];
        }

	    out[0] = (byte)(( predict_nr << 4 ) | shift_factor);
	    for ( int k = 0; k < 28; k += 2 ) {
	    	int amt = ( ( four_bit[k+1] >> 8 ) & 0xf0 ) | ( ( four_bit[k] >> 12 ) & 0xf );
            out[(k/2) + 2] = (byte)((amt & 0x000000FFL));
        }

		return out;
	}

	private void populateShortArrays(RandomAccessFile raf, short[] arrL, short[] arrR) throws Exception {
		if ((raf.getFilePointer() + (28 * 4)) >= raf.length()) {
			//EOF; send zeroes instead
			arrL = new short[28];
			arrR = new short[28];
			return;
		}
		int counter = 0;
		while (counter < 28) {
			short cand = raf.readShort();
			cand = Short.reverseBytes(cand);
			arrL[counter] = cand;
			cand = raf.readShort();
			cand = Short.reverseBytes(cand);
			arrR[counter] = cand;
			counter = counter + 1;
		}
	}

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

	private class ConvertToVGSTask extends TimerTask {
        java.util.Timer myTimer = null;

        public ConvertToVGSTask(java.util.Timer aTimer) {
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
}
