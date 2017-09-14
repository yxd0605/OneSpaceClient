package com.eli.oneos.model.oneos.aria;

public class AriaUtils {

	public static final String ARIA_END_URL = "/jsonrpc";
	public static final String ARIA_PARAMS_ENCODE = "application/x-www-form-urlencoded";

	public static final String[] ARIA_PARAMS_GET_LIST = { "gid", "status", "bittorrent", "files", "completedLength", "uploadLength", "totalLength",
			"downloadSpeed", "uploadSpeed", "numSeeders", "connections", "dir" };
	// public static final String[] ARIA_PARAMS_GET_STATUS = { "" };

	public static final String ARIA_CMD_NAME_RPC = "jsonrpc";
	public static final String ARIA_CMD_NAME_ID = "id";
	public static final String ARIA_CMD_NAME_METHOD = "method";
	public static final String ARIA_CMD_NAME_PARAMS = "params";

	// 1. 并发数: max-concurrent-downloads
	public static final String ARIA_KEY_GLOBAL_MAX_CUR_CONNECT = "max-concurrent-downloads";
	// 2. 上传速度限制: max-upload-limit
	public static final String ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT = "max-overall-upload-limit";
	// 3. 上传速度限制: max-download-limit
	public static final String ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT = "max-overall-download-limit";
	// 4. 最小分片大小: piece-length
	public static final String ARIA_KEY_GLOBAL_SPLIT_SIZE = "min-split-size";

}
