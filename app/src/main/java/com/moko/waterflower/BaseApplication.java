package com.moko.waterflower;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.moko.waterflower.service.SocketService;

import java.io.File;

public class BaseApplication extends Application {
    private static final String appFolder = "waterFlower";
    private static String PATH_LOGCAT;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appFolder;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + appFolder;
        }
        Printer filePrinter = new FilePrinter.Builder(PATH_LOGCAT)
                .fileNameGenerator(new DateFileNameGenerator())
                .build();
        LogConfiguration config = new LogConfiguration.Builder().tag("waterFlower").build();
        XLog.init(BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.NONE, config, new AndroidPrinter(), filePrinter);
        // 启动蓝牙服务
        startService(new Intent(this, SocketService.class));
    }
}
