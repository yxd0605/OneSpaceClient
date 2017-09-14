package com.eli.oneos.model.oneos.aria;

import android.util.Log;

import com.eli.oneos.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AriaCmd {
	private static final String TAG = AriaCmd.class.getSimpleName();

	public enum AriaAction {
		ADD_URI, ADD_TORRENT, GET_ACTIVE_LIST, GET_WAITING_LIST, GET_STOP_LIST, PAUSE, RESUME, REMOVE, DELETE, PAUSE_ALL, RESUME_ALL, DELETE_ALL, GET_TASK_STATUS, GET_TASK_OPTION, SET_TASK_OPTION, GET_GLOBAL_OPTION, SET_GLOBAL_OPTION
	}

	private String endUrl = "";
	private String rpc = "2.0";
	private String id = "aria2";
	private String method = null;
	private int offset = 0;
	private int count = 0;
	private JSONObject attrJson = null;
	private JSONArray paramsArray = null;
	private ArrayList<String> contentList = new ArrayList<String>();
	private AriaAction action = null;

	/**
	 * Init JsonArray Params
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSONException
	 */
	private void initJsonArray() throws FileNotFoundException, IOException, JSONException {
		if (null == contentList) {
			return;
		}
		this.paramsArray = new JSONArray();

		if (this.action == AriaAction.ADD_TORRENT) {
			for (int i = 0; i < contentList.size(); i++) {
				this.paramsArray.put(FileUtils.encodeFileToBase64(contentList.get(i)));
			}
		} else if (this.action == AriaAction.PAUSE || this.action == AriaAction.RESUME
				|| this.action == AriaAction.REMOVE || this.action == AriaAction.DELETE
				|| this.action == AriaAction.GET_TASK_STATUS
				|| this.action == AriaAction.GET_TASK_OPTION) {
			for (int i = 0; i < contentList.size(); i++) {
				this.paramsArray.put(contentList.get(i));
			}
		} else if (this.action == AriaAction.SET_TASK_OPTION
				|| this.action == AriaAction.SET_GLOBAL_OPTION) {
			for (int i = 0; i < contentList.size(); i++) {
				this.paramsArray.put(contentList.get(i));
			}
			if (null != attrJson) {
				this.paramsArray.put(attrJson);
			}
		} else {
			JSONArray content = new JSONArray(contentList);
			if (this.count != 0 || this.offset != 0) {
				this.paramsArray.put(0, this.offset);
				this.paramsArray.put(1, this.count);
				this.paramsArray.put(2, content);
			} else {
				this.paramsArray.put(0, content);
			}
			// Logged.d(TAG, "JSON Param: " + this.paramsArray.toString());
		}
	}

	/**
	 * Init Aria Method by Action
	 * 
	 * @throws JSONException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void initAriaMethod() throws JSONException {
		// Logged.d(TAG, "---Init Aria Method by Action---");

		if (this.action == null) {
			Log.w(TAG, "AriaAction is NULL");
			this.method = "";
		} else if (this.action == AriaAction.ADD_URI) {
			this.method = "aria2.addUri";
		} else if (this.action == AriaAction.ADD_TORRENT) {
			this.method = "aria2.addTorrent";
		} else if (this.action == AriaAction.GET_ACTIVE_LIST) {
			this.method = "aria2.tellActive";
		} else if (this.action == AriaAction.GET_TASK_STATUS) {
			this.method = "aria2.tellStatus";
		} else if (this.action == AriaAction.GET_WAITING_LIST) {
			this.method = "aria2.tellWaiting";
		} else if (this.action == AriaAction.GET_STOP_LIST) {
			this.method = "aria2.tellStopped";
		} else if (this.action == AriaAction.PAUSE) {
			this.method = "aria2.pause";
		} else if (this.action == AriaAction.RESUME) {
			this.method = "aria2.unpause";
		} else if (this.action == AriaAction.REMOVE) {
			this.method = "aria2.remove";
		} else if (this.action == AriaAction.DELETE) {
			this.method = "aria2.removeDownloadResult";
		} else if (this.action == AriaAction.PAUSE_ALL) {
			this.method = "aria2.pauseAll";
		} else if (this.action == AriaAction.RESUME_ALL) {
			this.method = "aria2.unpauseAll";
		} else if (this.action == AriaAction.DELETE_ALL) {
			this.method = "aria2.purgeDownloadResult";
		} else if (this.action == AriaAction.GET_TASK_OPTION) {
			this.method = "aria2.getOption";
		} else if (this.action == AriaAction.SET_TASK_OPTION) {
			this.method = "aria2.changeOption";
		} else if (this.action == AriaAction.GET_GLOBAL_OPTION) {
			this.method = "aria2.getGlobalOption";
		} else if (this.action == AriaAction.SET_GLOBAL_OPTION) {
			this.method = "aria2.changeGlobalOption";
		}

	}

	/**
	 * Off-line download BTInfo to JSON String
	 * 
	 * @return JSON String
	 * @throws JSONException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String toJsonParam() throws JSONException, FileNotFoundException, IOException {
		initAriaMethod();
		initJsonArray();

		JSONObject params = new JSONObject();

		params.put(AriaUtils.ARIA_CMD_NAME_RPC, this.rpc);
		params.put(AriaUtils.ARIA_CMD_NAME_ID, this.id);
		params.put(AriaUtils.ARIA_CMD_NAME_METHOD, this.method);
		params.put(AriaUtils.ARIA_CMD_NAME_PARAMS, this.paramsArray);

		Log.d(TAG, "Aria Json: " + params.toString());

		return params.toString();
	}

	public String getEndUrl() {
		return endUrl;
	}

	public void setEndUrl(String endUrl) {
		this.endUrl = endUrl;
	}

	public String getRpc() {
		return rpc;
	}

	public void setRpc(String rpc) {
		this.rpc = rpc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	// public String getParams() {
	// return params;
	// }
	//
	// public void setParams(String params) {
	// this.params = params;
	// }

	public void setAttrJson(JSONObject json) {
		this.attrJson = json;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setContent(String content) {
		if (null != content) {
			this.contentList.add(content);
		}
	}

	public void setContents(String[] content) {
		if (null != content) {
			for (int i = 0; i < content.length; i++) {
				this.contentList.add(content[i]);
			}
		}
	}

	public ArrayList<String> getContentList() {
		return contentList;
	}

	public void setContentList(ArrayList<String> contentList) {
		this.contentList = contentList;
	}

	public AriaAction getAction() {
		return action;
	}

	public void setAction(AriaAction action) {
		this.action = action;
	}

}
