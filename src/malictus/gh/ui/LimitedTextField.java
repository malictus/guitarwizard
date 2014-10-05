package malictus.gh.ui;

import javax.swing.text.*;

/**
 * LimitedTextField
 *
 * Used to limit the number of characters that can be typed into a text field
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class LimitedTextField extends PlainDocument {
	private int limit;

	public LimitedTextField(int limit) {
		super();
		this.limit = limit;
	}

	public void insertString (int offset, String  str, AttributeSet attr) throws BadLocationException {
		if (str == null) return;
		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}
}