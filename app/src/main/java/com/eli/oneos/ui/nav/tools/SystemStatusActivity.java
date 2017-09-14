package com.eli.oneos.ui.nav.tools;

import android.nfc.Tag;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.api.        OneOSSystemInfoAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.widget.TitleBackLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class SystemStatusActivity extends BaseActivity {
    private static final String TAG = SystemStatusActivity.class.getSimpleName();
    private static final int MAX_SHOW_COUNT = 10;
    private static final long MB_IN_BYTES = 1024 * 1024;

    private TitleBackLayout mTitleLayout;
    private LineChartView mCPUChartView, mMemoryChartView, mEth1ChartView, mEth2ChartView;
    private TextView mCPULabelTxt, mMemoryLabelTxt, mEth1LabelTxt, mEth2LabelTxt;
    private List<Integer> mCPUQueue = new ArrayList<>();
    private List<Integer> mMemoryQueue = new ArrayList<>();
    private List<Integer> mEth1ReceiveQueue = new ArrayList<>();
    private List<Integer> mEth1SendQueue = new ArrayList<>();
    private List<Integer> mEth2ReceiveQueue = new ArrayList<>();
    private List<Integer> mEth2SendQueue = new ArrayList<>();

    private LoginSession mLoginSession;
    private Timer mUpdateTimer = new Timer();
    private String product =LoginManage.getInstance().getLoginSession().getOneOSInfo().getProduct();

    private TimerTask task = new TimerTask() {

        @Override
        public void run() {
            getSystemInfo("cpu", null);
            getSystemInfo("mem", null);
            if (product.equals("h1n1")){
                getSystemInfo("net","eth0");
            }else {
                getSystemInfo("net", "eth1");
                getSystemInfo("net", "eth2");
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_system_status);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();

        initViews();
        mUpdateTimer.schedule(task, 0, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUpdateTimer.cancel();
    }

    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_system_status);
        mRootView = mTitleLayout;

        mCPULabelTxt = (TextView) findViewById(R.id.txt_cpu_label);
        mCPUChartView = (LineChartView) findViewById(R.id.chart_cpu);
        mMemoryLabelTxt = (TextView) findViewById(R.id.txt_memory_label);
        mMemoryChartView = (LineChartView) findViewById(R.id.chart_memory);
        mEth1LabelTxt = (TextView) findViewById(R.id.txt_eth1_label);
        mEth1ChartView = (LineChartView) findViewById(R.id.chart_eth1);
        mEth2LabelTxt = (TextView) findViewById(R.id.txt_eth2_label);
        mEth2ChartView = (LineChartView) findViewById(R.id.chart_eth2);

        if (product.equals("h1n1")){
            mEth2ChartView.setVisibility(View.GONE);
            mEth2LabelTxt.setVisibility(View.GONE);
        }
        initDefaultData();

        initChartView(mCPUChartView, 1, "%");
        initChartView(mMemoryChartView, 1, "%");
        initChartView(mEth1ChartView, 2, "M");
        initChartView(mEth2ChartView, 2, "M");
    }

    private void updateCPUChartData() {
        List<PointValue> mPointValues = new ArrayList<>();

        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mPointValues.add(new PointValue(i, mCPUQueue.get(MAX_SHOW_COUNT - i - 1)));
        }

        Line line = mCPUChartView.getLineChartData().getLines().get(0);
        line.setValues(mPointValues);
        mCPUChartView.animationDataUpdate(0);
    }

    private void updateMemoryChartData() {
        List<PointValue> mPointValues = new ArrayList<>();

        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mPointValues.add(new PointValue(i, mMemoryQueue.get(MAX_SHOW_COUNT - i - 1)));
        }

        Line line = mMemoryChartView.getLineChartData().getLines().get(0);
        line.setValues(mPointValues);
        mMemoryChartView.animationDataUpdate(0);
    }

    private void updateEth1ChartData() {
        List<PointValue> mRxPoints = new ArrayList<>();
        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mRxPoints.add(new PointValue(i, mEth1ReceiveQueue.get(MAX_SHOW_COUNT - i - 1)));
        }
        mEth1ChartView.getLineChartData().getLines().get(0).setValues(mRxPoints);

        List<PointValue> mTxPoints = new ArrayList<>();
        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mTxPoints.add(new PointValue(i, mEth1SendQueue.get(MAX_SHOW_COUNT - i - 1)));
        }
        mEth1ChartView.getLineChartData().getLines().get(1).setValues(mTxPoints);

        mEth1ChartView.animationDataUpdate(0);
    }

    private void updateEth2ChartData() {
        List<PointValue> mRxPoints = new ArrayList<>();
        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mRxPoints.add(new PointValue(i, mEth2ReceiveQueue.get(MAX_SHOW_COUNT - i - 1)));
        }
        mEth2ChartView.getLineChartData().getLines().get(0).setValues(mRxPoints);

        List<PointValue> mTxPoints = new ArrayList<>();
        for (int i = 0; i < MAX_SHOW_COUNT; i++) {
            mTxPoints.add(new PointValue(i, mEth2SendQueue.get(MAX_SHOW_COUNT - i - 1)));
        }
        mEth2ChartView.getLineChartData().getLines().get(1).setValues(mTxPoints);

        mEth2ChartView.animationDataUpdate(0);
    }

    private void initDefaultData() {
        for (int i = MAX_SHOW_COUNT - 1; i >= 0; i--) {
            mCPUQueue.add(0);
            mMemoryQueue.add(0);
            mEth1ReceiveQueue.add(0);
            mEth1SendQueue.add(0);
            mEth2ReceiveQueue.add(0);
            mEth2SendQueue.add(0);
        }
    }

    // max 2 lines shown
    private void initChartView(LineChartView mChartView, int lines, String append) {
        List<Line> lineList = new ArrayList<>();
        LineChartData lineData = new LineChartData();
        List<AxisValue> mXAxisValues = new ArrayList<>();
        List<PointValue> mPointValues = new ArrayList<>();
        for (int i = MAX_SHOW_COUNT - 1; i >= 0; i--) {
            mPointValues.add(new PointValue(i, 0));
            mXAxisValues.add(new AxisValue(i).setLabel((MAX_SHOW_COUNT - 1 - i) + "s"));
        }

        if (lines > 1) {
            Line line1 = new Line();
            line1.setColor(getResources().getColor(R.color.gray));
            line1.setCubic(true);
            line1.setFilled(true);
            line1.setPointRadius(4);
            line1.setStrokeWidth(2);
            line1.setValues(mPointValues);
            lineList.add(line1);
        }

        Line line = new Line();
        line.setColor(getResources().getColor(R.color.primary));
        line.setCubic(true);
        line.setFilled(true);
        line.setPointRadius(4);
        line.setStrokeWidth(2);
        line.setValues(mPointValues);
        lineList.add(line);

        lineData.setLines(lineList);

        Axis axisX = new Axis(); // X-Line Label
        axisX.setHasTiltedLabels(false);
        axisX.setHasLines(true);
        axisX.setTextColor(getResources().getColor(R.color.gray));
        axisX.setTextSize(11);
        axisX.setMaxLabelChars(2);
        axisX.setValues(mXAxisValues);
        lineData.setAxisXBottom(axisX);

        Axis axisY = new Axis();  // Y-Line Label
        axisY.setHasTiltedLabels(false);
        axisY.setTextColor(getResources().getColor(R.color.gray));
        axisY.setTextSize(11);
        axisY.setMaxLabelChars(4);
        axisY.setHasLines(true);
        axisY.setFormatter(new SimpleAxisValueFormatter().setAppendedText(append.toCharArray()));
        lineData.setAxisYLeft(axisY);

        Viewport v = new Viewport(mChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = MAX_SHOW_COUNT - 1;
        mChartView.setMaximumViewport(v);
        mChartView.setCurrentViewport(v);
        mChartView.setViewportCalculationEnabled(false);

        mChartView.setZoomEnabled(false);
        mChartView.setInteractive(false);
        mChartView.setLineChartData(lineData);

    }

    private long lastCpuTotal = -1;
    private long lastCpuUsed = -1;
    private long lastEth1Rx = -1;
    private long lastEth1Tx = -1;
    private long lastEth2Rx = -1;
    private long lastEth2Tx = -1;

    private void getSystemInfo(String dev, String name) {
        OneOSSystemInfoAPI infoAPI = new OneOSSystemInfoAPI(mLoginSession);
        infoAPI.setOnSystemInfoListener(new OneOSSystemInfoAPI.OnSystemInfoListener() {
            @Override
            public void onStart(String url, String dev, String name) {
            }

            @Override
            public void onSuccess(String url, String dev, String name, String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject datajson = json.getJSONObject("data");
                    if (datajson.has("cpu")) {
                        // {"result":true,"cpu":{"used":570775,"total":72466388}}
                        json = datajson.getJSONObject("cpu");
                        long used = json.getLong("used");
                        long total = json.getLong("total");
                        if (lastCpuUsed != -1 || lastCpuTotal != -1) {
                            int load = (int) ((used - lastCpuUsed) * 100 / (total - lastCpuTotal));
                            Log.d(TAG, ">>> CPULoad: " + load);
                            load = load > 0 ? load : 0;
                            mCPUQueue.remove(MAX_SHOW_COUNT - 1);
                            mCPUQueue.add(0, load);
                            mCPULabelTxt.setText(String.format(getResources().getString(R.string.fmt_label_cpu_load), (load + "%")));
                            updateCPUChartData();
                        }
                        lastCpuUsed = used;
                        lastCpuTotal = total;
                    } else if (datajson.has("mem")) {
                        // Response Data:{"result":true,"mem":{"free":923784,"total":1031572}}
                        json = datajson.getJSONObject("mem");
                        long free = json.getLong("free") ;
                        long total = json.getLong("total") ;
                        int load = 100 - (int) (free * 100 / total);
                        load = load > 0 ? load : 0;
                        mMemoryQueue.remove(MAX_SHOW_COUNT - 1);
                        mMemoryQueue.add(0, load);
                        mMemoryLabelTxt.setText(String.format(getResources().getString(R.string.fmt_label_memory),
                                Formatter.formatShortFileSize(SystemStatusActivity.this, total), (load + "%"),
                                Formatter.formatShortFileSize(SystemStatusActivity.this, free)));
                        updateMemoryChartData();
                    } else if (datajson.has("net")) {
                        json = datajson.getJSONObject("net");
                        if (name.equals("eth1") || name.equals("eth0")) {
                            long rx = json.getLong("rx");
                            long tx = json.getLong("tx");
                            if (lastEth1Rx != -1 || lastEth1Tx != -1) {
                                long crx = rx - lastEth1Rx;
                                long ctx = tx - lastEth1Tx;
                                crx = crx > 0 ? crx : 0;
                                ctx = ctx > 0 ? ctx : 0;
                                mEth1ReceiveQueue.remove(MAX_SHOW_COUNT - 1);
                                mEth1ReceiveQueue.add(0, (int) (crx / MB_IN_BYTES));
                                mEth1SendQueue.remove(MAX_SHOW_COUNT - 1);
                                mEth1SendQueue.add(0, (int) (ctx / MB_IN_BYTES));
                                mEth1LabelTxt.setText(String.format(getResources().getString(R.string.fmt_label_eth), "ETH1",
                                        Formatter.formatShortFileSize(SystemStatusActivity.this, crx),
                                        Formatter.formatShortFileSize(SystemStatusActivity.this, ctx)));
                                updateEth1ChartData();
                            }
                            lastEth1Rx = rx;
                            lastEth1Tx = tx;
                        } else {
                            long rx = json.getLong("rx");
                            long tx = json.getLong("tx");
                            if (lastEth2Rx != -1 || lastEth2Tx != -1) {
                                long crx = rx - lastEth2Rx;
                                long ctx = tx - lastEth2Tx;
                                crx = crx > 0 ? crx : 0;
                                ctx = ctx > 0 ? ctx : 0;
                                mEth2ReceiveQueue.remove(MAX_SHOW_COUNT - 1);
                                mEth2ReceiveQueue.add(0, (int) (crx / MB_IN_BYTES));
                                mEth2SendQueue.remove(MAX_SHOW_COUNT - 1);
                                mEth2SendQueue.add(0, (int) (ctx / MB_IN_BYTES));
                                mEth2LabelTxt.setText(String.format(getResources().getString(R.string.fmt_label_eth), "ETH2",
                                        Formatter.formatShortFileSize(SystemStatusActivity.this, crx),
                                        Formatter.formatShortFileSize(SystemStatusActivity.this, ctx)));
                                updateEth2ChartData();
                            }
                            lastEth2Rx = rx;
                            lastEth2Tx = tx;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String url, String dev, String name, int errorNo, String errorMsg) {

            }
        });
        infoAPI.query(dev, name);
    }
}
