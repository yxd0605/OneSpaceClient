package com.eli.oneos.model.oneos.aria;

public class AriaFile {
	/**
	 * { "completedLength":"0", "index":"1", "length":"284",
	 * "path":"\/sata\/storage\/public\/DOWNLOAD\/name in utf-8\/path in utf-8", "selected":"true",
	 * "uris":[] }
	 */
	private String completedLength = null;
	private String index = null;
	private String length = null;
	private String path = null;
	private String selected = null;
	// private String uris = null; //"uris":[]

	public String getCompletedLength() {
		return completedLength;
	}

	public void setCompletedLength(String completedLength) {
		this.completedLength = completedLength;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}
}
