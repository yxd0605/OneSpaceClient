package com.eli.oneos.model.oneos.aria;

import java.util.ArrayList;

public class AriaStatus {

	/*
	 * {"bitfield":"00", "bittorrent": { Class:BitTorrent }, "completedLength":"0",
	 * "connections":"0", "dir":"\/sata\/storage\/public\/DOWNLOAD", "downloadSpeed":"0", "files":[
	 * AriaFile ], "gid":"56952b227aa11a25", "infoHash":"e8c46e50d37328728a6d9f55362e9ef19da8ed4e",
	 * "numPieces":"3", "numSeeders":"0", "pieceLength":"128", "status":"active",
	 * "totalLength":"384", "uploadLength":"0", "uploadSpeed":"0" }
	 */

	private String bitfield = null;
	private BitTorrent bittorrent = null;
	private String completedLength = null;
	private String connections = null;
	private String dir = null;
	private String downloadSpeed = null;
	private ArrayList<AriaFile> files = null;
	private String gid = null;
	private String infoHash = null;
	private String numPieces = null;
	private String numSeeders = null;
	private String pieceLength = null;
	private String status = null;
	private String totalLength = null;
	private String uploadLength = null;
	private String uploadSpeed = null;

	// private String errorCode = null;
	// private String followedBy = null;
	// private String belongsTo = null;

	public String getBitfield() {
		return bitfield;
	}

	public void setBitfield(String bitfield) {
		this.bitfield = bitfield;
	}

	public BitTorrent getBittorrent() {
		return bittorrent;
	}

	public void setBittorrent(BitTorrent bittorrent) {
		this.bittorrent = bittorrent;
	}

	public String getCompletedLength() {
		return completedLength;
	}

	public void setCompletedLength(String completedLength) {
		this.completedLength = completedLength;
	}

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(String downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public ArrayList<AriaFile> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<AriaFile> files) {
		this.files = files;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getInfoHash() {
		return infoHash;
	}

	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}

	public String getNumPieces() {
		return numPieces;
	}

	public void setNumPieces(String numPieces) {
		this.numPieces = numPieces;
	}

	public String getNumSeeders() {
		return numSeeders;
	}

	public void setNumSeeders(String numSeeders) {
		this.numSeeders = numSeeders;
	}

	public String getPieceLength() {
		return pieceLength;
	}

	public void setPieceLength(String pieceLength) {
		this.pieceLength = pieceLength;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(String totalLength) {
		this.totalLength = totalLength;
	}

	public String getUploadLength() {
		return uploadLength;
	}

	public void setUploadLength(String uploadLength) {
		this.uploadLength = uploadLength;
	}

	public String getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(String uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

}
