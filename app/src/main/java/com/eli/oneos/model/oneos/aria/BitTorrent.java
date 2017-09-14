package com.eli.oneos.model.oneos.aria;

public class BitTorrent {

	/**
	 * "bittorrent":
	 * {"announceList":[["http:\/\/tracker1"],["http:\/\/tracker2"],["http:\/\/tracker3"]],
	 * "comment":"This is utf8 comment.", "creationDate":1123456789,
	 * "info":{"info":"info in utf-8"}, "mode":"multi" },
	 */
	// private ArrayList<String> announceList = new ArrayList<String>();
	private String comment = null;
	private long creationDate = 0;
	private BTInfo info = null;
	private String mode = null;

	// public ArrayList<String> getAnnounceList() {
	// return announceList;
	// }
	//
	// public void setAnnounceList(ArrayList<String> announceList) {
	// this.announceList = announceList;
	// }

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public BTInfo getInfo() {
		return info;
	}

	public void setInfo(BTInfo info) {
		this.info = info;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public class BTInfo {
		private String name = null;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
