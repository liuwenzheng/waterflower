package com.moko.waterflower.activity;

import android.Manifest;
import android.app.Activity;
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
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.moko.waterflower.R;
import com.moko.waterflower.adapter.DeviceAdapter;
import com.moko.waterflower.entity.Device;
import com.moko.waterflower.popup.CloudPlatformPopupWindow;
import com.moko.waterflower.popup.ConditionPopupWindow;
import com.moko.waterflower.popup.PVMPopupWindow;
import com.moko.waterflower.popup.RouterPopupWindow;
import com.moko.waterflower.popup.TimingPopupWindow;
import com.moko.waterflower.service.SocketService;
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
    private SocketService mBtService;
    private DeviceAdapter mAdapter;
    private List<Device> mDevices;
    private RouterPopupWindow mRouterPopupWindow;
    private CloudPlatformPopupWindow mCloudPlatformPopupWindow;
    private static final int PERMISSION_REQUEST_CODE = 1;

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
        mRouterPopupWindow = new RouterPopupWindow(this);
        mCloudPlatformPopupWindow = new CloudPlatformPopupWindow(this);
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
                        ToastUtils.showToast(MainActivity.this, "发送成功");
                    }
                    if (code == 1) {
                        ToastUtils.showToast(MainActivity.this, "发送失败");
                    }
                    if (code == 2) {
                        tvMainDeviceState.setText(intent.getStringExtra(SocketService.EXTRA_KEY_CONN_MESS));
                    }
                }
                if (SocketService.ACTION_GET_DATA.equals(intent.getAction())) {
                    HashMap<String, Device> map = (HashMap<String, Device>) intent.getSerializableExtra(SocketService.EXTRA_KEY_GET_DEVICES);
                    mDevices.addAll(map.values());
                    mAdapter.notifyDataSetChanged();
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

    @OnClick({R.id.btn_router_setting, R.id.btn_cloud_platform_setting, R.id.btn_water_all})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_router_setting:
                if (mRouterPopupWindow != null) {
                    mRouterPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.TOP, 0, 0);
                }
                break;
            case R.id.btn_cloud_platform_setting:
                if (mCloudPlatformPopupWindow != null) {
                    mCloudPlatformPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.TOP, 0, 0);
                }
                break;
            case R.id.btn_water_all:
                break;
        }
    }

    @Override
    public void waterCondition() {
        ConditionPopupWindow popupWindow = new ConditionPopupWindow(this);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }

    @Override
    public void waterTiming() {
        TimingPopupWindow popupWindow = new TimingPopupWindow(this);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }

    @Override
    public void water() {

    }

    @Override
    public void pvm() {
        PVMPopupWindow popupWindow = new PVMPopupWindow(this);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }
}
