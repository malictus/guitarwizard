package malictus.gh.dtb;

/**
 * DTBNode
 *
 * An object that represents a single DTB value.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class DTBValue {

	//float, string, or integer object (single only) or sometimes null
	Object value;
	int type;

	public DTBValue(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public int getType() {
		return type;
	}

}
