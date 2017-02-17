package com.moko.waterflower.module;

import android.text.TextUtils;

import com.moko.waterflower.utils.Utils;

import java.util.Calendar;

/**
 * @Date 2017/2/10
 * @Author wenzheng.liu
 * @Description
 */

public class MessModule {

    private static int serialNumber = 0;

    /**
     * @Date 2017/2/11
     * @Author wenzheng.liu
     * @Description 流水号
     */
    public static String getSerialNumber() {
        serialNumber++;
        return Utils.intToHexString(serialNumber, 1);
    }

    /**
     * @Date 2017/2/11
     * @Author wenzheng.liu
     * @Description 重置流水号
     */
    public static void resetSerialNumber() {
        MessModule.serialNumber = 0;
    }


    /**
     * @Date 2017/2/10
     * @Author wenzheng.liu
     * @Description 获取主控制器ID
     */
    public static String getMainDeviceId(String mess) {
        mess = mess.toUpperCase();
        if (!TextUtils.isEmpty(mess) && mess.startsWith("7E") && mess.endsWith("7E")) {
            String id = mess.substring(2, 14);
            return id;
        }
        return "";
    }

    /**
     * @Date 2017/2/10
     * @Author wenzheng.liu
     * @Description 接收到的数据，先替换7D01和7D02
     */
    public static String transformReceivedMess(String mess) {
        mess = mess.toUpperCase();
        if (!TextUtils.isEmpty(mess) && mess.startsWith("7E") && mess.endsWith("7E")) {
            String body = mess.substring(2, mess.length() - 2);
            int c = -1;
            for (int i = 0; i < body.length(); i++) {
                String a = body.substring(i, i + 2);
                if ("7D".equals(a)) {
                    String b = body.substring(i + 2, i + 4);
                    if ("01".equals(b)) {
                        c = 1;
                        break;
                    }
                    if ("02".equals(b)) {
                        c = 2;
                        break;
                    }
                } else {
                    i++;
                }
            }
            if (c == 1) {
                body = body.replaceAll("7D01", "7E");
            }
            if (c == 2) {
                body = body.replaceAll("7D02", "7D");
            }
            mess = "7E" + body + "7E";
            return mess;
        }
        return "";
    }

    /**
     * @Date 2017/2/10
     * @Author wenzheng.liu
     * @Description 发送的数据，替换7E、7D
     */
    public static String transformSendMess(String body) {
        StringBuilder sb = new StringBuilder();
        body = body.toUpperCase();
        if (!TextUtils.isEmpty(body)) {
            for (int i = 0; i < body.length(); i += 2) {
                String a = body.substring(i, i + 2);
                if ("7E".equals(a)) {
                    a = "7D01";
                }
                if ("7D".equals(a)) {
                    a = "7D02";
                }
                sb.append(a);
            }
            return "7E" + sb.toString() + "7E";
        }
        return "";
    }

    /**
     * @Date 2017/2/10
     * @Author wenzheng.liu
     * @Description 获得异或校验和
     */
    public static String getXorSum(String mess) {
        String[] data = new String[mess.length() / 2];
        for (int i = 0; i < mess.length() / 2; i++) {
            data[i] = mess.substring(i * 2, i * 2 + 2);
        }
        int ChkSum = 0;
        for (int i = 0; i < data.length; i++) {
            ChkSum = ChkSum ^ Integer.parseInt(data[i], 16);
        }
        return Utils.intToHexString(ChkSum, 1);
    }

    public static final String PATTERN_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * @Date 2017/2/11
     * @Author wenzheng.liu
     * @Description 获取bcd时间
     */
    public static String getBcdTime() {
        try {
            Calendar calendar = Calendar.getInstance();
            String timeStr = Utils.calendar2strDate(calendar, PATTERN_YYYY_MM_DD_HH_MM_SS);
            StringBuilder sb = new StringBuilder();
            String date = timeStr.split(" ")[0];
            String time = timeStr.split(" ")[1];
            String year = date.split("-")[0].substring(2, 4);
            String month = date.split("-")[1];
            String day = date.split("-")[2];
            String hour = time.split(":")[0];
            String min = time.split(":")[1];
            String second = time.split(":")[2];
            sb.append(year).append(month).append(day).append(hour).append(min).append(second);
            return sb.toString();
        } catch (Exception e) {
            return "170211120000";
        }
    }

    /**
     * @Date 2017/2/16
     * @Author wenzheng.liu
     * @Description 消息长度
     */
    public static String getMessLength(String body) {
        int length = body.length() / 2;
        return Utils.intToHexString(length, 2);
    }
}
