package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

import java.util.Objects;

/**
 * @author okyousgp
 * @date 2022/12/30 15:15
 * @description 卫星轨道参数，适用于gps/伽利略，北斗的非geos轨道卫星
 */
public class SatelliteOrbitParam {
    private String prn;
    private String time;
    private OrbitParam orbitParam;

    public SatelliteOrbitParam() {
    }

    public SatelliteOrbitParam(String prn, String time, OrbitParam orbitParam) {
        this.prn = prn;
        this.time = time;
        this.orbitParam = orbitParam;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public OrbitParam getOrbitParam() {
        return orbitParam;
    }

    public void setOrbitParam(OrbitParam orbitParam) {
        this.orbitParam = orbitParam;
    }

    @Override
    public String toString() {
        return "SatelliteOrbitParam{" +
                "prn='" + prn + '\'' +
                ", time='" + time + '\'' +
                ", orbitParam=" + orbitParam +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SatelliteOrbitParam that = (SatelliteOrbitParam) o;
        return prn.equals(that.prn) && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prn, time);
    }
}