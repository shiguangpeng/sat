package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

/**
 * @author okyousgp
 * @date 2023/1/3 14:36
 * @description glonass卫星的速度分量和位置分量
 */
public class GlonassTraceVector {
    private long timeOffSet;
    private double x;
    private double y;
    private double z;
    private double vx;
    private double vy;
    private double vz;

    public GlonassTraceVector() {

    }

    public GlonassTraceVector(long timeOffSet, double x, double y, double z, double vx, double vy, double vz) {
        this.timeOffSet = timeOffSet;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public long getTimeOffSet() {
        return timeOffSet;
    }

    public void setTimeOffSet(long timeOffSet) {
        this.timeOffSet = timeOffSet;
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

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    @Override
    public String toString() {
        return "GlonassTraceVector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", vx=" + vx +
                ", vy=" + vy +
                ", vz=" + vz +
                '}';
    }
}
