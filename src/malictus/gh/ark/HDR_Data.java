package malictus.gh.ark;

import java.io.*;
import java.util.*;
import malictus.gh.*;

/**
 * HDR_Data
 * This class represents an entire HDR file for an ARK. Lookup is faster and easier by looking items up here
 * rather than reading the entire file each time.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class HDR_Data {

	private File theHeaderFile;
	private long arkSize;

	private Vector stringTableElements = new Vector();
	private Vector section2Offsets = new Vector();
	private Vector section3Entries = new Vector();

	/**
	 * Given a HDR file, read in all the data and initialize all the vectors
	 */
	public HDR_Data(File headerFile) throws Exception {
		theHeaderFile = headerFile;
		RandomAccessFile raf = new RandomAccessFile(theHeaderFile, "r");
		//skip to the part that tells us the size of the ARK file
		raf.seek(12);
		arkSize = GHUtils.readNumber(raf.readInt());
		//size of the string table (section 1) in bytes
		long section1Size = GHUtils.readNumber(raf.readInt());
		//skip to the part of the file that tells us the number of 32-bit string offsets in section 2
		raf.seek(20 + section1Size);
		//number of 32-bit string offsets in section 2
		long section2Size = GHUtils.readNumber(raf.readInt());
		//skip to the part of the file that tells us the number of file entries in section 3
		raf.seek(24 + section1Size + (section2Size * 4));
		long section3Size = GHUtils.readNumber(raf.readInt());
		long section1Start = 20;
		long section2Start = 24 + section1Size;
		long section3Start = 28 + section1Size + (section2Size * 4);
		//now begin populating vectors, starting with String table vector
		raf.seek(section1Start + 1);	//skip over first character, which is null
		while (raf.getFilePointer() < (section2Start - 4)) {
			String string = GHUtils.readNullTerminatedString(raf);
			stringTableElements.add(string);
		}
		//now populate vector of section 2 offsets
		raf.seek(section2Start);
		while (raf.getFilePointer() < (section3Start - 4)) {
			section2Offsets.add(new Long(GHUtils.readNumber(raf.readInt())));
		}
		//lastly, populate the file vector
		raf.seek(section3Start);
		while (raf.getFilePointer() < raf.length() - 1) {
			long offsetIntoHDR = raf.getFilePointer();
			long fileOffset = GHUtils.readNumber(raf.readInt());
			long filenameStringID = GHUtils.readNumber(raf.readInt());
			long directoryStringID = GHUtils.readNumber(raf.readInt());
			long fileSize = GHUtils.readNumber(raf.readInt());
			long shouldBeZero = GHUtils.readNumber(raf.readInt());
			if (shouldBeZero != 0) {
				throw new Exception("Error parsing HDR file.");
			}
			Section3Entry s3e = new Section3Entry(fileOffset, filenameStringID, directoryStringID, fileSize, offsetIntoHDR);
			section3Entries.add(s3e);
		}
		raf.close();
	}

	public Vector getSection3Entries() {
		return section3Entries;
	}

	/**
	 * Overwrite a section 3 entry
	 */
	public void overwriteSection3Entry(String name, String pathName, long offset, long length) throws Exception {
		long fileid = getIDFor(name);
		if (fileid == -1) {
			throw new Exception("Error looking up string table values.");
		}

		long directoryid = getIDFor(pathName);
		if (directoryid == -1) {
			throw new Exception("Error looking up string table values.");
		}
		Section3Entry s = getSection3EntryFor(fileid, directoryid);
		//now, overwrite existing values in array
		s.setFileOffset(offset);
		s.setFileSize(length);
		//also, overwrite actual values in actual HDR file
		RandomAccessFile raf = new RandomAccessFile(theHeaderFile, "rw");
		raf.seek(s.getOffsetIntoHDR());
		raf.write(GHUtils.writeNumber(offset));
		raf.seek(s.getOffsetIntoHDR() + 12);
		raf.write(GHUtils.writeNumber(length));
		raf.close();
	}

	/**
	 * Overwrite a section 3 entry
	 */
	public void overwriteSection3Entry(Section3Entry s3e) throws Exception {
		//overwrite actual values in actual HDR file
		RandomAccessFile raf = new RandomAccessFile(theHeaderFile, "rw");
		raf.seek(s3e.getOffsetIntoHDR());
		raf.write(GHUtils.writeNumber(s3e.getFileOffset()));
		raf.seek(s3e.getOffsetIntoHDR() + 12);
		raf.write(GHUtils.writeNumber(s3e.getFileSize()));
		raf.close();
	}

	private Section3Entry getSection3EntryFor(long nameid, long pathid) throws Exception{
		int counter = 0;
		while (counter < section3Entries.size()) {
			Section3Entry s = (Section3Entry)section3Entries.get(counter);
			if ( (s.getFileID() == nameid) && (s.getDirectoryID() == pathid)) {
				return s;
			}
			counter = counter + 1;
		}
		throw new Exception("File name not found in HDR file.");
	}

	public long getArkSize() {
		return arkSize;
	}

	public void setArkSize(long newArkSize) throws Exception {
		arkSize = newArkSize;
		RandomAccessFile raf = new RandomAccessFile(theHeaderFile, "rw");
		raf.seek(12);
		raf.write(GHUtils.writeNumber(newArkSize));
		raf.close();
	}

	public File getHeaderFile() {
		return theHeaderFile;
	}

	/**
	 * Find a string from the string table, given its ID from section 3
	 */
	public String getNameFor(long ID) throws Exception {
		long offset = ((Long)section2Offsets.get((int)ID)).longValue();
		long x = 1;	//start at one since string table has null char at beginning
		int counter = 0;
		while (counter < stringTableElements.size()) {
			String check = (String)stringTableElements.get(counter);
			if (x == offset) {
				return check;
			}
			x = x + check.length() + 1;
			counter = counter + 1;
		}
		throw new Exception("Unable to look up string in string table");
	}

	/**
	 * Find a section 3 ID number, given a string from the string table
	 * Returns -1 if not found
	 */
	private long getIDFor(String fileName) throws Exception {
		//first, find the offset for this string in the string table
		long x = 1;
		int counter = 0;
		long offset = -1;
		while (counter < stringTableElements.size() && (offset == -1) ) {
			String check = (String)stringTableElements.get(counter);
			if (check.equals(fileName) ) {
				offset = x;
			}
			x = x + check.length() + 1;
			counter = counter + 1;
		}
		if (offset == -1) {
			return offset;
		}
		counter = 0;
		while (counter < section2Offsets.size()) {
			Long check = (Long)section2Offsets.get(counter);
			if (check.longValue() == offset) {
				return counter;
			}
			counter = counter + 1;
		}
		throw new Exception("Matching string not found in string table");
	}
}

