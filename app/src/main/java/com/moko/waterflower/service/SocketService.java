package com.moko.waterflower.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.elvishew.xlog.LogUtils;
import com.moko.waterflower.base.BaseHandler;
import com.moko.waterflower.entity.Device;
import com.moko.waterflower.module.LogModule;
import com.moko.waterflower.module.MessModule;

import java.util.HashMap;

public class SocketService extends Service {
    public static final String ACTION_CONN_STATE = "update_conn_state";
    public static final String ACTION_GET_DATA = "get_data";
    public static final String EXTRA_KEY_SEND_CODE = "send_code";
    public static final String EXTRA_KEY_CONN_MESS = "conn_mess";
    public static final String EXTRA_KEY_GET_DEVICES = "data_devices";
    public static final String EXTRA_KEY_DEVICE_ID = "data_device_id";
    public static final String MESS_HEADER_LOGIN = "0002";
    public static final String MESS_HEADER_DEVICES = "0003";
    public static final String MESS_HEADER_ACK = "0001";


    private Handler mInHandle;

    private Handler mOutHandle;

    public SocketThread getSocketThread() {
        return mSocketThread;
    }

    private SocketThread mSocketThread;
    public static String mMainDeviceId;

    public static class InHandle extends BaseHandler<SocketService> {
        HashMap<String, String> hm = new HashMap<>();

        public InHandle(SocketService service) {
            super(service);
        }

        @Override
        protected void handleMessage(SocketService service, Message msg) {
            if (msg.obj != null) {
                String s = msg.obj.toString();
                if (s.trim().length() > 0) {
                    // LogModule.i("mhandler接收到obj=" + s);
                    // LogModule.i("开始更新UI");
                    String mess = MessModule.transformReceivedMess(s);
                    String header = mess.substring(20, 24);
                    if (MESS_HEADER_LOGIN.equals(header)) {
                        if (hm.get(header) == null) {
                            hm.put(header, mess);
                        } else {
                            return;
                        }
                        // 登录
                        mMainDeviceId = MessModule.getMainDeviceId(mess);
                        String resp = service.renderSendMess("8002" + MessModule.getBcdTime() + "0001");
                        service.getSocketThread().send(resp);
                        Intent intent = new Intent(ACTION_CONN_STATE);
                        intent.putExtra(EXTRA_KEY_DEVICE_ID, mMainDeviceId);
                        intent.putExtra(EXTRA_KEY_SEND_CODE, 4);
                        service.sendBroadcast(intent);
                    }
                    if (MESS_HEADER_DEVICES.equals(header)) {
                        if (hm.get(header) == null) {
                            hm.put(header, mess);
                        } else {
                            return;
                        }
                        try {
                            HashMap<String, Device> map = new HashMap<>();
                            // 上报数据
                            int count = Integer.parseInt(mess.substring(24, 26), 16);
                            // 每组数据13byte，从26开始
                            for (int i = 26; i <= count * 26; i += 26) {
                                String id = mess.substring(i, i + 8);
                                Device device = map.get(id);
                                if (device == null) {
                                    device = new Device();
                                    device.id = id;
                                }
                                if (TextUtils.isEmpty(device.time)) {
                                    device.time = String.format("20%s-%s-%s %s:%s:%s",
                                            mess.substring(i + 10, i + 12), mess.substring(i + 12, i + 14),
                                            mess.substring(i + 14, i + 16), mess.substring(i + 16, i + 18),
                                            mess.substring(i + 18, i + 20), mess.substring(i + 20, i + 22));
                                }
                                if ("02".equals(mess.substring(i + 8, i + 10))) {
                                    device.envTemp = Integer.toString(Integer.parseInt(mess.substring(i + 22, i + 24), 16));
                                    device.envHumidity = Integer.toString(Integer.parseInt(mess.substring(i + 24, i + 26), 16));
                                }
                                if ("03".equals(mess.substring(i + 8, i + 10))) {
                                    device.light = Integer.toString(Integer.parseInt(mess.substring(i + 22, i + 26), 16));
                                }
                                if ("04".equals(mess.substring(i + 8, i + 10))) {
                                    device.power = Double.toString(Integer.parseInt(mess.substring(i + 22, i + 26), 16) * 0.01);
                                }
                                if ("05".equals(mess.substring(i + 8, i + 10))) {
                                    device.water = Integer.toString(Integer.parseInt(mess.substring(i + 22, i + 26), 16));
                                }
                                if ("06".equals(mess.substring(i + 8, i + 10))) {
                                    device.soilHumidity = Integer.toString(Integer.parseInt(mess.substring(i + 22, i + 26), 16));
                                }
                                map.put(id, device);
                            }
                            Intent intent = new Intent(ACTION_GET_DATA);
                            intent.putExtra(EXTRA_KEY_GET_DEVICES, map);
                            service.sendBroadcast(intent);
                            // 应答
                            service.getSocketThread().send(service.renderSendMess("8001" + header + "00"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            service.getSocketThread().send(service.renderSendMess("8001" + header + "01"));
                        }
                    }
                    if (MESS_HEADER_ACK.equals(header)) {
                        String result = mess.substring(28, 30);
                        Intent intent = new Intent(ACTION_CONN_STATE);
                        if ("00".equals(result)) {
                            intent.putExtra(EXTRA_KEY_SEND_CODE, 0);
                            service.sendBroadcast(intent);
                        }
                        if ("01".equals(result)) {
                            intent.putExtra(EXTRA_KEY_SEND_CODE, 1);
                            service.sendBroadcast(intent);
                        }
                    }

                } else {
                    LogModule.i("没有数据返回不更新");
                }
            }
        }
    }

    /**
     * @Date 2017/2/16
     * @Author wenzheng.liu
     * @Description 组装发送的数据
     */
    public String renderSendMess(String mess) {
        LogModule.i("要拼装的数据:" + mess);
        StringBuilder sb = new StringBuilder();
        sb.append(mMainDeviceId);
        sb.append(MessModule.getSerialNumber());
        sb.append(MessModule.getMessLength(mess));
        sb.append(mess);
        String xorSum = MessModule.getXorSum(sb.toString());
        String resp = MessModule.transformSendMess(sb.append(xorSum).toString());
        return resp;
    }


    public static class OutHandle extends BaseHandler<SocketService> {

        public OutHandle(SocketService service) {
            super(service);
        }

        @Override
        protected void handleMessage(SocketService service, Message msg) {
            Intent intent = new Intent(ACTION_CONN_STATE);
            switch (msg.what) {
                case 2:
                    intent.putExtra(EXTRA_KEY_SEND_CODE, 2);
                    intent.putExtra(EXTRA_KEY_CONN_MESS, (String) msg.obj);
                    break;
                case 3:
                    intent.putExtra(EXTRA_KEY_SEND_CODE, 3);
                    intent.putExtra(EXTRA_KEY_CONN_MESS, (String) msg.obj);
                    break;
            }
            service.sendBroadcast(intent);
        }
    }

    public void reConn() {
        mSocketThread.conn();
    }

    public SocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInHandle = new InHandle(this);
        mOutHandle = new OutHandle(this);
        startSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSocket();
    }

    public void startSocket() {
        mSocketThread = new SocketThread(mInHandle, mOutHandle, this);
        mSocketThread.start();
    }

    private void stopSocket() {
        mSocketThread.isRun = false;
        mSocketThread.close();
        mSocketThread = null;
        MessModule.resetSerialNumber();
        LogModule.i("Socket已终止");
    }
}
