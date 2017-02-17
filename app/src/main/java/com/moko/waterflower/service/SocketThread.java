package com.moko.waterflower.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.moko.waterflower.module.LogModule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @Date 2017/2/10
 * @Author wenzheng.liu
 * @Description
 */

public class SocketThread extends Thread {

    private Handler mInHandle;
    private Handler mOutHandle;
    public Socket client = null;
    private String ip = "192.168.4.1";
    private int port = 1001;
    private int timeout = 10000;

    private DataOutputStream out;
    private DataInputStream in;
    public boolean isRun = true;

    public SocketThread(Handler inHandler, Handler outHandler, Context context) {
        mInHandle = inHandler;
        mOutHandle = outHandler;
    }


    @Override
    public void run() {
        LogModule.i("线程socket开始运行");
        if (!conn()) {
            return;
        }
        LogModule.i("1.run开始");
        String line;
        while (isRun) {
            try {
                if (client != null) {
                    LogModule.i("2.检测数据");
                    line = in.readUTF();
                    LogModule.i("3.getdata" + line + " len=" + line.length());
                    LogModule.i("4.start set Message");
                    Message msg = mInHandle.obtainMessage();
                    msg.obj = line;
                    mInHandle.sendMessage(msg);// 结果返回给UI处理
                    LogModule.i("5.send to handler");
                } else {
                    LogModule.i("没有可用连接");
                    connState("连接失败");
                }
            } catch (Exception e) {
                LogModule.i("数据接收错误" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接socket服务器
     */
    public boolean conn() {
        try {
            LogModule.i("获取到ip端口:" + ip + ";" + port);
            LogModule.i("连接中……");
            connState("连接中……");
            client = new Socket(ip, port);
            client.setSoTimeout(timeout);// 设置阻塞时间
            LogModule.i("连接成功");
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            LogModule.i("输入输出流获取成功");
            connState("连接成功");
            return true;
        } catch (UnknownHostException e) {
            LogModule.i("连接错误UnknownHostException 重新获取");
            e.printStackTrace();
            connState("连接失败");
        } catch (IOException e) {
            LogModule.i("连接服务器io错误");
            e.printStackTrace();
            connState("连接失败");
        } catch (Exception e) {
            LogModule.i("连接服务器错误Exception" + e.getMessage());
            e.printStackTrace();
            connState("连接失败");
        }
        return false;
    }

    private void connState(String mess) {
        Message msg = mOutHandle.obtainMessage();
        msg.obj = mess;
        msg.what = 2;
        mOutHandle.sendMessage(msg);// 结果返回给UI处理
    }

    /**
     * 发送数据
     *
     * @param mess
     */
    public void send(String mess) {
        try {
            if (client != null) {
                LogModule.i("发送" + mess + "至"
                        + client.getInetAddress().getHostAddress() + ":"
                        + String.valueOf(client.getPort()));
                out.writeUTF(mess);
                LogModule.i("发送成功");
                Message msg = mOutHandle.obtainMessage();
                msg.obj = mess;
                msg.what = 1;
                mOutHandle.sendMessage(msg);// 结果返回给UI处理
            } else {
                LogModule.i("client 不存在");
                Message msg = mOutHandle.obtainMessage();
                msg.obj = mess;
                msg.what = 0;
                mOutHandle.sendMessage(msg);// 结果返回给UI处理
                LogModule.i("连接不存在重新连接");
                conn();
            }
        } catch (Exception e) {
            LogModule.i("send error");
            e.printStackTrace();
        } finally {
            LogModule.i("发送完毕");

        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (client != null) {
                LogModule.i("close in");
                in.close();
                LogModule.i("close out");
                out.close();
                LogModule.i("close client");
                client.close();
            }
        } catch (Exception e) {
            LogModule.i("close err");
            e.printStackTrace();
        }

    }
}
