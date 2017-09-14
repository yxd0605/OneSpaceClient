package com.eli.oneos.model.oneos.aria;

import java.util.List;

/**
 * AriaInfo for show Aria generate list
 * 
 * @author shz
 */
public class AriaInfo {

	private String gid = null;
	private String status = null;
	private BitTorrent bittorrent = null;
	private List<AriaFile> files = null;
	private String completedLength = null;
	private String totalLength = null;
	private String downloadSpeed = null;
	private String uploadSpeed = null;
	private String connections = null;
	private String numSeeders = null;
	private String dir = null;

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BitTorrent getBittorrent() {
		return bittorrent;
	}

	public void setBittorrent(BitTorrent bittorrent) {
		this.bittorrent = bittorrent;
	}

	public List<AriaFile> getFiles() {
		return files;
	}

	public void setFiles(List<AriaFile> files) {
		this.files = files;
	}

	public String getCompletedLength() {
		return completedLength;
	}

	public void setCompletedLength(String completedLength) {
		this.completedLength = completedLength;
	}

	public String getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(String totalLength) {
		this.totalLength = totalLength;
	}

	public String getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(String downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public String getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(String uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getNumSeeders() {
		return numSeeders;
	}

	public void setNumSeeders(String numSeeders) {
		this.numSeeders = numSeeders;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

}
