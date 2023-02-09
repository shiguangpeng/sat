package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author okyousgp
 * @date 2023/1/3 15:07
 * @description glonass卫星导航数据中反应的卫星轨迹参数
 */
public class GlonassSatelliteTraceArg {
    private String refTime;
    private String prn;
    private double position_x;
    private double position_y;
    private double position_z;
    private double velocity_x;
    private double velocity_y;
    private double velocity_z;
    private double acceleration_x;
    private double accerleration_y;
    private double acceleration_z;

    public GlonassSatelliteTraceArg() {
    }

    public GlonassSatelliteTraceArg(String refTime, String prn, double position_x, double position_y, double position_z, double velocity_x, double velocity_y, double velocity_z, double acceleration_x, double accerleration_y, double acceleration_z) {
        this.refTime = refTime;
        this.prn = prn;
        this.position_x = position_x;
        this.position_y = position_y;
        this.position_z = position_z;
        this.velocity_x = velocity_x;
        this.velocity_y = velocity_y;
        this.velocity_z = velocity_z;
        this.acceleration_x = acceleration_x;
        this.accerleration_y = accerleration_y;
        this.acceleration_z = acceleration_z;
    }

    public String getRefTime() {
        return refTime;
    }

    public void setRefTime(String refTime) {
        this.refTime = refTime;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
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

    public double getAcceleration_x() {
        return acceleration_x;
    }

    public void setAcceleration_x(double acceleration_x) {
        this.acceleration_x = acceleration_x;
    }

    public double getAccerleration_y() {
        return accerleration_y;
    }

    public void setAccerleration_y(double accerleration_y) {
        this.accerleration_y = accerleration_y;
    }

    public double getAcceleration_z() {
        return acceleration_z;
    }

    public void setAcceleration_z(double acceleration_z) {
        this.acceleration_z = acceleration_z;
    }

    @Override
    public String toString() {
        return "GlonassSatelliteTraceArg{" +
                "refTime='" + refTime + '\'' +
                ", prn='" + prn + '\'' +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                ", position_z=" + position_z +
                ", velocity_x=" + velocity_x +
                ", velocity_y=" + velocity_y +
                ", velocity_z=" + velocity_z +
                ", acceleration_x=" + acceleration_x +
                ", accerleration_y=" + accerleration_y +
                ", acceleration_z=" + acceleration_z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlonassSatelliteTraceArg that = (GlonassSatelliteTraceArg) o;
        return refTime.equals(that.refTime) && prn.equals(that.prn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refTime, prn);
    }
}
