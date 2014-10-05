package malictus.gh.dtb;

import malictus.gh.*;

import java.io.*;
import java.util.*;

/**
 * DTBFile
 *
 * An object that represents a single DTB file.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class DTBFile {

	private File DTBFile;
	private File DTAFile;
	private int key;
	//Vector of DTB entries (which store DTBValue objects) for this file
	private Vector DTBNodes;
	//used to keep track of new node id numbers to use
	private int nodeIDCounter = -1;

	public DTBFile(String theFileName, String theDirName) throws Exception {
		DTBNodes = new Vector();
		//create DTB file from ARK
		DTBFile = GHUtils.putInTemp(theFileName, theDirName);
		//create a fresh file for the DTA file
		String dtaName = DTBFile.getName().substring(0, DTBFile.getName().length() - 4);
		dtaName = dtaName + ".dta";
		DTAFile = new File(DTBFile.getParent() + File.separator + dtaName);
		if (DTAFile.exists()) {
			DTAFile.delete();
		}
		DTAFile.createNewFile();
		RandomAccessFile rafin = new RandomAccessFile(DTBFile, "r");
		key = (int)GHUtils.readNumber(rafin.readInt());
		rafin.close();
		FileInputStream fis = new FileInputStream(DTBFile);
		FileOutputStream fos = new FileOutputStream(DTAFile);
		DecryptionTable dt = new DecryptionTable(key);
		try {
			byte[] buf = new byte[(int)DTBFile.length() - 4];
			fis.skip(4);
			int x = fis.read(buf);
			if (x != buf.length) {
				throw new Exception("Error decrypting DTB file");
			}
			dt.decrypt(buf);
			fos.write(buf);
		} catch (Exception err) {
			fis.close();
			fos.close();
			throw err;
		}
		fis.close();
		fos.close();
		createLookupTable();
	}

	/*************************** GENERAL PUBLIC METHODS **********************/

	public void encrypt() throws Exception {
		if (!DTAFile.exists()) {
			throw new Exception("DTA file does not exist");
		}
		if (DTBFile.exists()) {
			DTBFile.delete();
		}
		DTBFile.createNewFile();

		FileInputStream fis = new FileInputStream(DTAFile);
		FileOutputStream fos = new FileOutputStream(DTBFile);
		DecryptionTable dt = new DecryptionTable(key);
		try {
			byte[] buf = new byte[(int)DTAFile.length()];
			//write key
			fos.write(GHUtils.writeNumber(key));
			//read DTA file into memory
			int x = fis.read(buf);
			if (x != buf.length) {
				throw new Exception("Error encrytping DTB file");
			}
			//encrypt
			dt.encrypt(buf);
			//write encrypted data
			fos.write(buf);
		} catch (Exception err) {
			fis.close();
			fos.close();
			throw err;
		}
		fis.close();
		fos.close();
	}

	public File getDTBFile() {
		return DTBFile;
	}

	public boolean nodeExists(String node) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			if (x.path.equals(node)) {
				return true;
			}
			counter = counter + 1;
		}
		return false;
	}

	/**
	 * Return a string vector with the node names of all direct child nodes of this node, stripping
	 * out the parent node portion
	 */
	public Vector getChildNodesFor(String nodeLabel) throws Exception {
		int counter = 0;
		Vector vec = new Vector();
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.startsWith(nodeLabel) && (!node.equals(nodeLabel))) {
				if (node.indexOf("/") != -1) {
					node = node.substring(node.indexOf("/") + 1);
					if (node.indexOf("/") == -1) {
						vec.add(node);
					}
				}
			}
			counter = counter + 1;
		}
		return vec;
	}

	public int getNodePosition(String nodeLabel) throws Exception {
		int counter = 0;
		int realfilepos = -1;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(nodeLabel)) {
				realfilepos = x.filepos;
			}
			counter = counter + 1;
		}
		if (realfilepos == -1) {
			throw new Exception("Node not found");
		}

		//find parent node
		counter = 0;
		String keyFolder = "";
		String keyFile = "";
		if (nodeLabel.lastIndexOf("/") == -1) {
			keyFolder = "";
			keyFile = nodeLabel;
		} else {
			keyFolder = nodeLabel.substring(0, nodeLabel.lastIndexOf("/"));
			keyFile = nodeLabel.substring(nodeLabel.lastIndexOf("/") + 1);
		}

		int filepos = 0;
		if (keyFolder.equals("")) {
			filepos = -3;
		} else {
			boolean foundit = false;
			while (counter < DTBNodes.size() && (foundit == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					foundit = true;
					filepos = x.filepos;
				}
				counter = counter + 1;
			}
			if (!foundit) {
				throw new Exception("DTB node not found");
			}
		}

		RandomAccessFile raf = new RandomAccessFile(DTAFile, "r");
		//skip node number
		raf.seek(filepos + 4);
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		//skip this node's id
		raf.skipBytes(4);
		if (filepos != -3) {
			//skip first child (should be node name)
			int numb = (int)GHUtils.readNumber(raf.readInt());
			if (numb != 5) {
				//wasn't keyword
				raf.close();
				throw new Exception("Not keyword");
			}
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			while (innercounter < skipper) {
				raf.read();
				innercounter = innercounter + 1;
			}
		}
		int returnval = 1;
		//now should be at start of first non-name node, position 1
		while (raf.getFilePointer() != realfilepos) {
			skipNode(raf);
			returnval = returnval + 1;
			if (raf.getFilePointer() >= (raf.length() + 1)) {
				raf.close();
				throw new Exception("node note found");
			}
		}
		raf.close();
		return returnval;
	}

	/*************************** PUBLIC VALUE READING METHODS **********************/

	public Float readFloat(String floatLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(floatLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Float)) {
					throw new Exception("DTBValue is not float.");
				}
				return (Float)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("Float not found");
	}

	public Integer readInt(String intLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(intLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Integer)) {
					throw new Exception("DTBValue is not integer.");
				}
				return (Integer)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("Integer not found");
	}

	public String readString(String stringLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(stringLabel)) {
				Object obj = x.theValue;
				DTBValue dtbval;
				if (! (obj instanceof DTBValue)) {
					//exception ONLY for Trogdor loading tip!
					if (stringLabel.startsWith("loading_tip")) {
						Vector nodes = (Vector)obj;
						dtbval =(DTBValue)(nodes.get(0));
					} else {
						throw new Exception("DTBValue object not found.");
					}
				} else {
					dtbval = (DTBValue)obj;
				}
				if (! (dtbval.value instanceof String)) {
					throw new Exception("DTBValue is not string.");
				}
				return (String)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("String not found");
	}

	public Vector readVector(String vectorLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(vectorLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof Vector)) {
					throw new Exception("Vector object not found.");
				}
				return (Vector)obj;
			}
			counter = counter + 1;
		}
		throw new Exception("Vector not found");
	}

	/*************************** PUBLIC NODE CREATION/DELETION METHODS **********************/

	/*
	 * Position refers to the position to insert the node at, starting at 1 and not counting
	 * the parent node name itself
	 */
	public void createStringNode(String key, String value, int position, int nodeType) throws Exception {
		int counter = 0;
		if (position < 1) {
			throw new Exception("Incorrect position specification");
		}
		if ( (nodeType != 2) && (nodeType != 5) && (nodeType != 7) && (nodeType != 18)
				&& (nodeType != 32) && (nodeType != 33) && (nodeType != 34) ) {
			throw new Exception("Incorrect noteType");
		}
		//make sure node doesn't exist
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				throw new Exception("Node exists");
			}
			counter = counter + 1;
		}
		//now, find parent node
		counter = 0;
		String keyFolder = "";
		String keyFile = "";
		if (key.lastIndexOf("/") == -1) {
			keyFolder = "";
			keyFile = key;
		} else {
			keyFolder = key.substring(0, key.lastIndexOf("/"));
			keyFile = key.substring(key.lastIndexOf("/") + 1);
		}
		int filepos = 0;
		if (keyFolder.equals("")) {
			filepos = -3;
		} else {
			boolean foundit = false;
			while (counter < DTBNodes.size() && (foundit == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					foundit = true;
					filepos = x.filepos;
				}
				counter = counter + 1;
			}
			if (!foundit) {
				throw new Exception("DTB node not found");
			}
		}
		RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
		//skip node number
		raf.seek(filepos + 4);
		//number of children nodes
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		if (originalchildren < position) {
			raf.close();
			throw new Exception("Not enough child nodes");
		}
		//add one to node count
		originalchildren = originalchildren + 1;
		raf.seek(filepos + 4);
		raf.write(GHUtils.write16BitNumber(originalchildren));
		//skip this node's id
		raf.skipBytes(4);
		if (filepos != -3) {
			//skip first child (should be node name)
			int numb = (int)GHUtils.readNumber(raf.readInt());
			if (numb != 5) {
				//wasn't keyword
				raf.close();
				throw new Exception("Not keyword");
			}
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			while (innercounter < skipper) {
				raf.read();
				innercounter = innercounter + 1;
			}
		}
		//now should be at start of first non-name node, position 1
		while (position > 1) {
			skipNode(raf);
			position = position - 1;
		}
		//read in byte array for the rest of the file
		long prevpos = raf.getFilePointer();
		byte[] rest = new byte[(int)(raf.length() - raf.getFilePointer())];
		raf.readFully(rest);
		//delete rest of file
		raf.seek(prevpos);
		raf.setLength(prevpos);
		//now, write in new node value
		raf.write(GHUtils.writeNumber(16));
		//now, number of children
		raf.write(GHUtils.write16BitNumber(2));
		//now node ID
		raf.write(GHUtils.writeNumber(getNewNodeID()));
		//now keyword child
		raf.write(GHUtils.writeNumber(5));
		raf.write(GHUtils.writeNumber(keyFile.length()));
		byte[] newvalue = keyFile.getBytes("ISO-8859-1");
		if (newvalue.length != keyFile.length()) {
			throw new Exception("Error converting " + keyFile + " to bytes.");
		}
		raf.write(newvalue);
		//now string child
		raf.write(GHUtils.writeNumber(nodeType));
		raf.write(GHUtils.writeNumber(value.length()));
		newvalue = value.getBytes("ISO-8859-1");
		if (newvalue.length != value.length()) {
			throw new Exception("Error converting " + value + " to bytes.");
		}
		raf.write(newvalue);
		//now, the rest of the file
		raf.write(rest);
		raf.close();
		//now, clear out all current values, and recreate entire DTBNodes structure
		createLookupTable();
	}

	/*
	 * Position refers to the position to insert the node at, starting at 1 and not counting
	 * the parent node name itself
	 */
	public void createStringVectorNode(String key, Vector stringvalues, int position) throws Exception {
		int counter = 0;
		if (position < 1) {
			throw new Exception("Incorrect position specification");
		}
		//make sure node doesn't exist
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				throw new Exception("Node exists");
			}
			counter = counter + 1;
		}
		//now, find parent node
		counter = 0;
		String keyFolder = "";
		String keyFile = "";
		if (key.lastIndexOf("/") == -1) {
			keyFolder = "";
			keyFile = key;
		} else {
			keyFolder = key.substring(0, key.lastIndexOf("/"));
			keyFile = key.substring(key.lastIndexOf("/") + 1);
		}

		int filepos = 0;
		if (keyFolder.equals("")) {
			filepos = -3;
		} else {
			boolean foundit = false;
			while (counter < DTBNodes.size() && (foundit == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					foundit = true;
					filepos = x.filepos;
				}
				counter = counter + 1;
			}
			if (!foundit) {
				throw new Exception("DTB node not found");
			}
		}
		RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
		//skip node number
		raf.seek(filepos + 4);
		//number of children nodes
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		if (originalchildren < position) {
			raf.close();
			throw new Exception("Not enough child nodes");
		}
		//add one to node count
		originalchildren = originalchildren + 1;
		raf.seek(filepos + 4);
		raf.write(GHUtils.write16BitNumber(originalchildren));
		//skip this node's id
		raf.skipBytes(4);
		if (filepos != -3) {
			//skip first child (should be node name)
			int numb = (int)GHUtils.readNumber(raf.readInt());
			if (numb != 5) {
				//wasn't keyword
				raf.close();
				throw new Exception("Not keyword");
			}
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			while (innercounter < skipper) {
				raf.read();
				innercounter = innercounter + 1;
			}
		}
		//now should be at start of first non-name node, position 1
		while (position > 1) {
			skipNode(raf);
			position = position - 1;
		}
		//read in byte array for the rest of the file
		long prevpos = raf.getFilePointer();
		byte[] rest = new byte[(int)(raf.length() - raf.getFilePointer())];
		raf.readFully(rest);
		//delete rest of file
		raf.seek(prevpos);
		raf.setLength(prevpos);
		//now, write in new node value
		raf.write(GHUtils.writeNumber(16));
		//now, number of children
		raf.write(GHUtils.write16BitNumber(stringvalues.size() + 1));
		//now node ID
		raf.write(GHUtils.writeNumber(getNewNodeID()));
		//now keyword child
		raf.write(GHUtils.writeNumber(5));
		raf.write(GHUtils.writeNumber(keyFile.length()));
		byte[] newvalue = keyFile.getBytes("ISO-8859-1");
		if (newvalue.length != keyFile.length()) {
			throw new Exception("Error converting " + keyFile + " to bytes.");
		}
		raf.write(newvalue);
		//now string children
		int innercounter = 0;
		while (innercounter < stringvalues.size()) {
			String a = (String)stringvalues.get(innercounter);
			raf.write(GHUtils.writeNumber(18));
			raf.write(GHUtils.writeNumber(a.length()));
			newvalue = a.getBytes("ISO-8859-1");
			if (newvalue.length != a.length()) {
				throw new Exception("Error converting " + a + " to bytes.");
			}
			raf.write(newvalue);
			innercounter = innercounter + 1;
		}
		//now, the rest of the file
		raf.write(rest);
		raf.close();

		//now, clear out all current values, and recreate entire DTBNodes structure
		createLookupTable();
	}

	/*
	 * Position refers to the position to insert the node at, starting at 1 and not counting
	 * the parent node name itself
	 */
	public void createIntegerVectorNode(String key, Vector intvalues, int position) throws Exception {
		int counter = 0;
		if (position < 1) {
			throw new Exception("Incorrect position specification");
		}
		//make sure node doesn't exist
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				throw new Exception("Node exists");
			}
			counter = counter + 1;
		}
		//now, find parent node
		counter = 0;
		String keyFolder = "";
		String keyFile = "";
		if (key.lastIndexOf("/") == -1) {
			keyFolder = "";
			keyFile = key;
		} else {
			keyFolder = key.substring(0, key.lastIndexOf("/"));
			keyFile = key.substring(key.lastIndexOf("/") + 1);
		}

		int filepos = 0;
		if (keyFolder.equals("")) {
			filepos = -3;
		} else {
			boolean foundit = false;
			while (counter < DTBNodes.size() && (foundit == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					foundit = true;
					filepos = x.filepos;
				}
				counter = counter + 1;
			}
			if (!foundit) {
				throw new Exception("DTB node not found");
			}
		}
		RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
		//skip node number
		raf.seek(filepos + 4);
		//number of children nodes
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		if (originalchildren < position) {
			raf.close();
			throw new Exception("Not enough child nodes");
		}
		//add one to node count
		originalchildren = originalchildren + 1;
		raf.seek(filepos + 4);
		raf.write(GHUtils.write16BitNumber(originalchildren));
		//skip this node's id
		raf.skipBytes(4);
		if (filepos != -3) {
			//skip first child (should be node name)
			int numb = (int)GHUtils.readNumber(raf.readInt());
			if (numb != 5) {
				//wasn't keyword
				raf.close();
				throw new Exception("Not keyword");
			}
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			while (innercounter < skipper) {
				raf.read();
				innercounter = innercounter + 1;
			}
		}
		//now should be at start of first non-name node, position 1
		while (position > 1) {
			skipNode(raf);
			position = position - 1;
		}
		//read in byte array for the rest of the file
		long prevpos = raf.getFilePointer();
		byte[] rest = new byte[(int)(raf.length() - raf.getFilePointer())];
		raf.readFully(rest);
		//delete rest of file
		raf.seek(prevpos);
		raf.setLength(prevpos);
		//now, write in new node value
		raf.write(GHUtils.writeNumber(16));
		//now, number of children
		raf.write(GHUtils.write16BitNumber(intvalues.size() + 1));
		//now node ID
		raf.write(GHUtils.writeNumber(getNewNodeID()));
		//now keyword child
		raf.write(GHUtils.writeNumber(5));
		raf.write(GHUtils.writeNumber(keyFile.length()));
		byte[] newvalue = keyFile.getBytes("ISO-8859-1");
		if (newvalue.length != keyFile.length()) {
			throw new Exception("Error converting " + keyFile + " to bytes.");
		}
		raf.write(newvalue);
		//now int children
		int innercounter = 0;
		while (innercounter < intvalues.size()) {
			Integer a = (Integer)intvalues.get(innercounter);
			raf.write(GHUtils.writeNumber(0));
			raf.write(GHUtils.writeNumber(a.intValue()));
			innercounter = innercounter + 1;
		}
		//now, the rest of the file
		raf.write(rest);
		raf.close();

		//now, clear out all current values, and recreate entire DTBNodes structure
		createLookupTable();
	}


	public void deleteNode(String key) throws Exception {
		int counter = 0;
		int filepos = -1;
		//make sure node exists and find its filepos
		boolean foundit = false;
		while (counter < DTBNodes.size() && (foundit == false) ) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				filepos = x.filepos;
				foundit = true;
			}
			counter = counter + 1;
		}
		if (!foundit) {
			throw new Exception("node not found");
		}
		//now, find parent node
		counter = 0;
		String keyFolder = "";
		if (key.lastIndexOf("/") == -1) {
			keyFolder = "";
		} else {
			keyFolder = key.substring(0, key.lastIndexOf("/"));
		}
		int fileposs = 0;
		if (keyFolder.equals("")) {
			fileposs = -3;
		} else {
			boolean founditt = false;
			while (counter < DTBNodes.size() && (founditt == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					founditt = true;
					fileposs = x.filepos;
				}
				counter = counter + 1;
			}
			if (!founditt) {
				throw new Exception("DTB node not found");
			}
		}
		RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
		//skip node number
		raf.seek(fileposs + 4);
		//number of children nodes
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		//subtract one from parent node count
		originalchildren = originalchildren - 1;
		raf.seek(fileposs + 4);
		raf.write(GHUtils.write16BitNumber(originalchildren));

		//now skip to beginning of node to delete
		raf.seek(filepos);
		//find end of node position
		skipNode(raf);
		//read in byte array for the rest of the file
		byte[] rest = new byte[(int)(raf.length() - raf.getFilePointer())];
		raf.readFully(rest);
		//delete rest of file
		raf.seek(filepos);
		raf.setLength(filepos);
		//now, the rest of the file without deleted node
		raf.write(rest);
		raf.close();
		//now, clear out all current values, and recreate entire DTBNodes structure
		createLookupTable();
	}

	/*************************** PUBLIC VALUE WRITING METHODS **********************/

	public void write1Float(String key, Float value) throws Exception {
		int counter = 0;
		boolean foundit = false;
		while (counter < DTBNodes.size() && (foundit == false) ) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Float)) {
					throw new Exception("DTBValue is not float.");
				}
				//just replace with new val, since we're not rebuilding (same size)
				dtbval.value = value;
				RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
				int filepos = x.filepos;
				raf.seek(filepos + 14);
				int skipper = (int)GHUtils.readNumber(raf.readInt());
				raf.skipBytes(skipper);
				int numb = (int)GHUtils.readNumber(raf.readInt());
				if (numb != 1) {
					raf.close();
					throw new Exception("Incorrect DTB Value");
				}
				int intversion = Float.floatToIntBits(value.floatValue());
				raf.write(GHUtils.writeNumber(intversion));

				raf.close();
				foundit = true;
			}

			counter = counter + 1;
		}
		if (!foundit) {
			throw new Exception("Float not found");
		}
	}

	public void write0Int(String key, Integer value) throws Exception {
		int counter = 0;
		boolean foundit = false;
		while (counter < DTBNodes.size() && (foundit == false) ) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Integer)) {
					throw new Exception("DTBValue is not integer.");
				}
				//just replace with new val, since we're not rebuilding (same size)
				dtbval.value = value;
				RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
				int filepos = x.filepos;
				raf.seek(filepos);
				raf.seek(filepos + 14);
				int skipper = (int)GHUtils.readNumber(raf.readInt());
				raf.skipBytes(skipper);
				int numb = (int)GHUtils.readNumber(raf.readInt());
				if (numb != 0) {
					raf.close();
					throw new Exception("Incorrect DTB Value");
				}
				int intversion = value.intValue();
				raf.write(GHUtils.writeNumber(intversion));
				raf.close();
				foundit = true;
			}

			counter = counter + 1;
		}
		if (!foundit) {
			throw new Exception("Integer not found");
		}
	}

	public void writeString(String key, String value, int nodeType) throws Exception {
		int counter = 0;
		if ( (nodeType != 2) && (nodeType != 5) && (nodeType != 7) && (nodeType != 18)
				&& (nodeType != 32) && (nodeType != 33) && (nodeType != 34) ) {
			throw new Exception("Incorrect noteType");
		}
		boolean foundit = false;
		while (counter < DTBNodes.size() && (foundit == false) ) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(key)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof String)) {
					throw new Exception("DTBValue is not string.");
				}
				//rebuild DBA file first, then recalculate everything, since filepos's change
				RandomAccessFile raf = new RandomAccessFile(DTAFile, "rw");
				int filepos = x.filepos;
				raf.seek(filepos);
				raf.seek(filepos + 14);
				int skipper = (int)GHUtils.readNumber(raf.readInt());
				raf.skipBytes(skipper);
				int numb = (int)GHUtils.readNumber(raf.readInt());
				if (numb != nodeType) {
					raf.close();
					throw new Exception("Incorrect DTB Value" + numb);
				}
				skipper = (int)GHUtils.readNumber(raf.readInt());
				//overwrite with new value
				raf.seek(raf.getFilePointer() - 4);
				raf.write(GHUtils.writeNumber(value.length()));
				long prevpos = raf.getFilePointer();
				//read in byte array for the rest of the file, starting at the the right point in the file
				raf.skipBytes(skipper);
				byte[] rest = new byte[(int)(raf.length() - raf.getFilePointer())];
				raf.readFully(rest);
				//delete rest of file
				raf.seek(prevpos);
				raf.setLength(prevpos);
				//write new value
				byte[] newvalue = value.getBytes("ISO-8859-1");
				//confirm that nothing strange happened in the text transfer
				if (newvalue.length != value.length()) {
					throw new Exception("Error converting " + value + " to bytes.");
				}
				raf.write(newvalue);
				//now rewrite rest of file back from array
				raf.write(rest);
				raf.close();
				//rewrite nodes from file
				createLookupTable();
				foundit = true;
			}
			counter = counter + 1;
		}
		if (!foundit) {
			throw new Exception("String not found");
		}
	}

	/*************************** PRIVATE METHODS **********************/
	/*
	 * Re-read the current DTA file, and repopulate all local vars
	 */
	private void createLookupTable() throws Exception {
		RandomAccessFile raf = new RandomAccessFile(DTAFile, "r");
		DTBNodes.clear();
		nodeIDCounter = -1;
		//read number of top-level nodes
		raf.skipBytes(1);
		int topNodes = GHUtils.read16BitNumber(raf.readShort());
		raf.skipBytes(4);
		try {
			int counter = 0;
			while (counter < topNodes) {
				String nodeString = "";
				processNode(raf, nodeString);
				counter = counter + 1;
			}
		} catch (Exception err) {
			err.printStackTrace();
			raf.close();
			throw err;
		}
		raf.close();
	}

	/**
	 * Read through the nodes, creating the DTBNodes vector as we go
	 */
	private void processNode(RandomAccessFile raf, String nodeString) throws Exception {
		//always assume we started at a node, so read in the node
		int filepos = (int)raf.getFilePointer();		//for storing later
		int numb = (int)GHUtils.readNumber(raf.readInt());
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//happens sometimes
			raf.seek(raf.getFilePointer() - 4);
			Object x = readValue(raf);		//oddball node
			nodeString = nodeString + " ";
			DTBValue val = new DTBValue(x, numb);
			DTBNode oddNode = new DTBNode(val, 0, filepos, nodeString, -1);
			DTBNodes.add(oddNode);
			return;
		}
		//number of children
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		//node ID
		int nodeID = (int)GHUtils.readNumber(raf.readInt());     //can cast to int here, these won't get big
		if (originalchildren < 1) {
			throw new Exception("No children for node");
		}
		String keyword = "";
		//read first child (which is node name, usually)
		numb = (int)GHUtils.readNumber(raf.readInt());
		int children = originalchildren;
		if (numb != 5) {
			//wasn't keyword
			keyword = " ";
			raf.seek(raf.getFilePointer() - 4);
			children = children + 1;
		} else {
			//was keyword
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int counter = 0;
			while (counter < skipper) {
				keyword = keyword + (char)raf.read();
				counter = counter + 1;
			}
		}
		if (!nodeString.equals("")) {
			nodeString = nodeString + "/";
		}
		nodeString = nodeString + keyword;
		//now, figure out if other children are nodes or data
		//assumes all nodes are one or the other only
		if (children == 1) {
			DTBNode oddNode = new DTBNode(null, 0, filepos, nodeString, nodeID);
			DTBNodes.add(oddNode);
			return;
		}
		int counter = 0;
		numb = (int)GHUtils.readNumber(raf.readInt());
		raf.seek(raf.getFilePointer() - 4);	//back up since we'll re-read this value below
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//DATA FOUND
			if (children == 2) {
				//single value
				Object singleValue = readValue(raf);
				DTBValue val = new DTBValue(singleValue, numb);
				DTBNode node = new DTBNode(val, 0, filepos, nodeString, nodeID);
				DTBNodes.add(node);
			} else {
				//more than one object here
				Vector v = new Vector();
				counter = 1;
				while (counter < children) {
					Object singleValue = readValue(raf);
					DTBValue val = new DTBValue(singleValue, numb);
					v.add(val);
					counter = counter + 1;
				}
				DTBNode node = new DTBNode(v, 0, filepos, nodeString, nodeID);
				DTBNodes.add(node);
			}
		} else {
			//MORE NODES
			DTBNode node = new DTBNode(null, originalchildren, filepos, nodeString, nodeID);
			DTBNodes.add(node);
			counter = 0;
			while (counter < (children - 1) ) {
				//recurse
				processNode(raf, nodeString);
				counter = counter + 1;
			}
		}
	}

	/**
	 * Like process node, but just skips a node in the file rather than writing anything
	 */
	private void skipNode(RandomAccessFile raf) throws Exception {
		//always assume we started at a node, so read in the node
		int numb = (int)GHUtils.readNumber(raf.readInt());
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//happens sometimes
			raf.seek(raf.getFilePointer() - 4);
			readValue(raf);		//oddball node
			return;
		}
		//number of children
		int originalchildren = GHUtils.read16BitNumber(raf.readShort());
		if (originalchildren < 1) {
			throw new Exception("No children for node");
		}
		//node id
		raf.skipBytes(4);
		//read first child (which is node name, usually)
		numb = (int)GHUtils.readNumber(raf.readInt());
		int children = originalchildren;
		if (numb != 5) {
			raf.seek(raf.getFilePointer() - 4);
			children = children + 1;
		} else {
			//was keyword
			int skipper = (int)GHUtils.readNumber(raf.readInt());
			int counter = 0;
			while (counter < skipper) {
				raf.read();
				counter = counter + 1;
			}
		}
		//now, figure out if other children are nodes or data
		//assumes all nodes are one or the other only
		if (children == 1) {
			return;
		}
		int counter = 0;
		numb = (int)GHUtils.readNumber(raf.readInt());
		raf.seek(raf.getFilePointer() - 4);	//back up since we'll re-read this value below
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//DATA FOUND
			if (children == 2) {
				//single value
				readValue(raf);
			} else {
				//more than one object here
				counter = 1;
				while (counter < children) {
					readValue(raf);
					counter = counter + 1;
				}
			}
		} else {
			//MORE NODES
			counter = 0;
			while (counter < (children - 1) ) {
				//recurse
				skipNode(raf);
				counter = counter + 1;
			}
		}
	}

	/**
	 * Read and return a single (non-tree node) value
	 */
	private Object readValue(RandomAccessFile raf) throws Exception {
		int numb = (int)GHUtils.readNumber(raf.readInt());
		int skipper;
		if (numb == 0) {
			//32 bit int
			int val = (int)GHUtils.readNumber(raf.readInt());		//i think casting is safe here
			Integer intVal = new Integer(val);
			return intVal;
		} else if (numb == 1) {
			//32 bit float
			int val = (int)GHUtils.readNumber(raf.readInt());
			Float floatVal = new Float(Float.intBitsToFloat(val));
			return floatVal;
		} else if (numb == 2) {
			//function name
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 5) {
			//keyword (sometimes shows up here); treat as a string
			skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			String stringVal = "";
			while (innercounter < skipper) {
				stringVal = stringVal + (char)raf.read();
				innercounter = innercounter + 1;
			}
			return stringVal;
		} else if (numb == 6) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 7) {
			//unknown
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 8) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 9) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 18) {
			//STRING VALUE
			skipper = (int)GHUtils.readNumber(raf.readInt());
			int innercounter = 0;
			String stringVal = "";
			while (innercounter < skipper) {
				stringVal = stringVal + (char)raf.read();
				innercounter = innercounter + 1;
			}
			return stringVal;
		} else if (numb == 32) {
			//string
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 33) {
			//file ref
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 34) {
			//file ref
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 35) {
			//string
			skipper = (int)GHUtils.readNumber(raf.readInt());
			raf.skipBytes(skipper);
			return null;
		} else {
			throw new Exception("Incorrect DTB reference: " + numb);
		}
	}

	private int getNewNodeID() {
		if (nodeIDCounter != -1) {
			return nodeIDCounter + 1;
		}
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			if (x.nodeID > nodeIDCounter) {
				nodeIDCounter = x.nodeID;
			}
			counter = counter + 1;
		}
		return nodeIDCounter + 1;
	}
}
