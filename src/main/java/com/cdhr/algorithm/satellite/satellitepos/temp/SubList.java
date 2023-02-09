package com.cdhr.algorithm.satellite.satellitepos.temp;

import java.util.Arrays;

/**
 * @author okyousgp
 * @date 2023/1/18 15:19
 * @description
 */
public class SubList {
    private double[] blh;

    private String statTime;
    private double x;
    private double y;
    private double z;

    public SubList() {
    }

    public SubList(double[] blh, String statTime, double x, double y, double z) {
        this.blh = blh;
        this.statTime = statTime;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double[] getBlh() {
        return blh;
    }

    public void setBlh(double[] blh) {
        this.blh = blh;
    }

    public String getStatTime() {
        return statTime;
    }

    public void setStatTime(String statTime) {
        this.statTime = statTime;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "SubList{" +
                "blh=" + Arrays.toString(blh) +
                ", statTime='" + statTime + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
