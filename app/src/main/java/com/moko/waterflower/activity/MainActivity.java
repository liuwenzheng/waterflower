package com.moko.waterflower.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.moko.waterflower.R;
import com.moko.waterflower.adapter.DeviceAdapter;
import com.moko.waterflower.entity.Device;
import com.moko.waterflower.popup.CloudPlatformPopupWindow;
import com.moko.waterflower.popup.ConditionPopupWindow;
import com.moko.waterflower.popup.PWMPopupWindow;
import com.moko.waterflower.popup.RouterPopupWindow;
import com.moko.waterflower.popup.TimingPopupWindow;
import com.moko.waterflower.service.SocketService;
import com.moko.waterflower.utils.PreferencesUtil;
import com.moko.waterflower.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements DeviceAdapter.WaterOnClickListener {
    @Bind(R.id.tv_main_device_id)
    TextView tvMainDeviceId;
    @Bind(R.id.tv_main_device_state)
    TextView tvMainDeviceState;
    @Bind(R.id.rv_list)
    RecyclerView rvList;
    @Bind(R.id.btn_router_setting)
    Button btnRouterSetting;
    @Bind(R.id.btn_cloud_platform_setting)
    Button btnCloudPlatformSetting;
    @Bind(R.id.btn_water_all)
    Button btnWaterAll;
    @Bind(R.id.btn_reconn)
    Button btnReconn;

    private SocketService mBtService;
    private DeviceAdapter mAdapter;
    private List<Device> mDevices;
    private RouterPopupWindow mRouterPopupWindow;
    private CloudPlatformPopupWindow mCloudPlatformPopupWindow;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        initContentView();
    }

    private void initContentView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bindService(new Intent(this, SocketService.class), mServiceConnection, BIND_AUTO_CREATE);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(SocketService.ACTION_CONN_STATE);
        filter.addAction(SocketService.ACTION_GET_DATA);
        registerReceiver(mReceiver, filter);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        mDevices = new ArrayList<>();
        mAdapter = new DeviceAdapter(this, mDevices, this);
        rvList.setAdapter(mAdapter);
        btnRouterSetting.setEnabled(false);
        btnCloudPlatformSetting.setEnabled(false);
        btnWaterAll.setEnabled(false);
        btnReconn.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ToastUtils.showToast(MainActivity.this, "This app needs these permissions!");
                        MainActivity.this.finish();
                        return;
                    }
                }
                initContentView();
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (SocketService.ACTION_CONN_STATE.equals(intent.getAction())) {
                    int code = intent.getIntExtra(SocketService.EXTRA_KEY_SEND_CODE, -1);
                    if (code == 0) {
                        ToastUtils.showToast(MainActivity.this, "设备接收命令成功");
                    }
                    if (code == 1) {
                        ToastUtils.showToast(MainActivity.this, "设备接收命令失败");
                    }
                    if (code == 2) {
                        String conn = intent.getStringExtra(SocketService.EXTRA_KEY_CONN_MESS);
                        if ("连接成功".equals(conn)) {
                            if (mDialog == null) {
                                mDialog = new ProgressDialog(MainActivity.this);
                                mDialog.setCancelable(false);
                                mDialog.setMessage("接收登录命令中...");
                                mDialog.show();
                            }
                        }
                        tvMainDeviceState.setText(conn);
                        if ("连接失败".equals(conn)) {
                            ToastUtils.showToast(MainActivity.this, conn);
                            tvMainDeviceState.setText("未连接");
                            btnReconn.setEnabled(true);
                        }
                    }
                    // 超时弹出提示
                    if (code == 3) {
                        ToastUtils.showToast(MainActivity.this, intent.getStringExtra(SocketService.EXTRA_KEY_CONN_MESS));
                    }
                    // 获取路由云平台配置
                    if (code == 5) {
                        btnRouterSetting.setEnabled(true);
                        btnCloudPlatformSetting.setEnabled(true);
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }
                    // 获取登录信息，拿到设备ID
                    if (code == 4) {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        if (!TextUtils.isEmpty(tvMainDeviceId.getText().toString())) {
                            return;
                        }
                        tvMainDeviceId.setText("ID:" + intent.getStringExtra(SocketService.EXTRA_KEY_DEVICE_ID));
                    }
                }
                if (SocketService.ACTION_GET_DATA.equals(intent.getAction())) {
                    HashMap<String, Device> map = (HashMap<String, Device>) intent.getSerializableExtra(SocketService.EXTRA_KEY_GET_DEVICES);
                    if (map.values().size() != 0) {
                        mDevices.clear();
                        btnWaterAll.setEnabled(true);
                        mDevices.addAll(map.values());
                        mAdapter.notifyDataSetChanged();
                    }

                }
            }

        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBtService = ((SocketService.LocalBinder) service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtService = null;
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        stopService(new Intent(this, SocketService.class));
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @OnClick({R.id.btn_router_setting, R.id.btn_cloud_platform_setting, R.id.btn_water_all, R.id.btn_reconn, R.id.btn_router})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_router_setting:
                mRouterPopupWindow = new RouterPopupWindow(this, PreferencesUtil.getStringByName(this, "ssid", ""),
                        PreferencesUtil.getStringByName(this, "password", ""));
                mRouterPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.TOP, 0, 0);

                break;
            case R.id.btn_cloud_platform_setting:
                mCloudPlatformPopupWindow = new CloudPlatformPopupWindow(this, PreferencesUtil.getStringByName(this, "ip", ""),
                        PreferencesUtil.getStringByName(this, "port", ""));
                mCloudPlatformPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.TOP, 0, 0);
                break;
            case R.id.btn_water_all:
                for (int i = 0; i < mDevices.size(); i++) {
                    water(mDevices.get(i).id);
                }
                break;
            case R.id.btn_reconn:
                mBtService.reConn();
                break;
            case R.id.btn_router:
                getRouterAndCloud();
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setCancelable(false);
                mDialog.setMessage("获取路由云平台信息...");
                mDialog.show();
                break;
        }
    }

    @Override
    public void waterCondition(String id) {
        ConditionPopupWindow popupWindow = new ConditionPopupWindow(this, id);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }

    @Override
    public void waterTiming(String id) {
        TimingPopupWindow popupWindow = new TimingPopupWindow(this, id);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }

    @Override
    public void water(String id) {
        mBtService.getSocketThread().send(mBtService.renderSendMess("8007" + id + "010006"));
    }

    @Override
    public void pwm(String id) {
        PWMPopupWindow popupWindow = new PWMPopupWindow(this, id);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }


    public SocketService getBtService() {
        return mBtService;
    }

    public void getRouterAndCloud() {
        mBtService.getSocketThread().send(mBtService.renderSendMess("8004"));
    }
}
