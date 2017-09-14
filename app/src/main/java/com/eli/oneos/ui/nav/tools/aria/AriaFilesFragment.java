package com.eli.oneos.ui.nav.tools.aria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.adapter.AriaFileAdapter;
import com.eli.oneos.model.oneos.aria.AriaFile;
import com.eli.oneos.model.oneos.aria.AriaStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Aria2 Task File List Fragment
 * 
 * @author shz
 * @since V1.6.21
 */
public class AriaFilesFragment extends Fragment implements AriaDetailsActivity.OnAriaTaskChangedListener {

	private static final String TAG = AriaFilesFragment.class.getSimpleName();

	private ListView mListView;
	private AriaFileAdapter mAdapter;

	private List<AriaFile> mTaskFileList = new ArrayList<AriaFile>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_aria_files, container, false);

		initViews(view);

		return view;
	}

	private void initViews(View view) {
		mListView = (ListView) view.findViewById(R.id.listview_files);
		mAdapter = new AriaFileAdapter(getActivity(), mTaskFileList);
		mListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onAriaChanged(AriaStatus ariaStatus) {
		mTaskFileList.clear();
		if (null != ariaStatus) {
			List<AriaFile> list = ariaStatus.getFiles();
			if (null != list) {
				mTaskFileList.addAll(list);
			}
		}
		Log.d(TAG, "Aria File List: " + mTaskFileList.size());
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}
}
