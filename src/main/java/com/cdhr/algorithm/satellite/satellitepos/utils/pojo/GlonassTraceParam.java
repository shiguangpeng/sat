package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

/**
 * @author okyousgp
 * @date 2023/1/3 9:41
 * @description glonass星历提供的参数（卫星运动状态）
 */
public class GlonassTraceParam {
    // 位置
    private double position_x;
    private double position_y;
    private double position_z;

    // 速度
    private double velocity_x;
    private double velocity_y;
    private double velocity_z;

    // 加速度
    private double acc_x;
    private double acc_y;
    private double acc_z;

    // 重力位第二带谐系数，常量
    public static final double j_0_2 = 1.0826257 * Math.pow(10, -3);

    public GlonassTraceParam(double position_x, double position_y, double position_z, double velocity_x, double velocity_y, double velocity_z, double acc_x, double acc_y, double acc_z) {
        this.position_x = position_x;
        this.position_y = position_y;
        this.position_z = position_z;
        this.velocity_x = velocity_x;
        this.velocity_y = velocity_y;
        this.velocity_z = velocity_z;
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
    }

    public double getPosition_x() {
        return position_x;
    }

    public void setPosition_x(double position_x) {
        this.position_x = position_x;
    }

    public double getPosition_y() {
        return position_y;
    }

    public void setPosition_y(double position_y) {
        this.position_y = position_y;
    }

    public double getPosition_z() {
        return position_z;
    }

    public void setPosition_z(double position_z) {
        this.position_z = position_z;
    }

    public double getVelocity_x() {
        return velocity_x;
    }

    public void setVelocity_x(double velocity_x) {
        this.velocity_x = velocity_x;
    }

    public double getVelocity_y() {
        return velocity_y;
    }

    public void setVelocity_y(double velocity_y) {
        this.velocity_y = velocity_y;
    }

    public double getVelocity_z() {
        return velocity_z;
    }

    public void setVelocity_z(double velocity_z) {
        this.velocity_z = velocity_z;
    }

    public double getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(double acc_x) {
        this.acc_x = acc_x;
    }

    public double getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(double acc_y) {
        this.acc_y = acc_y;
    }

    public double getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(double acc_z) {
        this.acc_z = acc_z;
    }

    @Override
    public String toString() {
        return "GlonassTrackParam{" +
                "position_x=" + position_x +
                ", position_y=" + position_y +
                ", position_z=" + position_z +
                ", velocity_x=" + velocity_x +
                ", velocity_y=" + velocity_y +
                ", velocity_z=" + velocity_z +
                ", acc_x=" + acc_x +
                ", acc_y=" + acc_y +
                ", acc_z=" + acc_z +
                '}';
    }
}
