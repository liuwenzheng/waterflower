package com.moko.waterflower.entity;

import java.io.Serializable;

/**
 * @Date 2017/2/13
 * @Author wenzheng.liu
 * @Description
 */

public class Device implements Serializable {
    public String id;
    public String time;
    public String power;
    public String envHumidity;
    public String soilHumidity;
    public String envTemp;
    public String water;
    public String light;
}
