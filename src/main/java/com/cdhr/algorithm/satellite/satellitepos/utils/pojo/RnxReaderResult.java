package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

import java.util.List;

/**
 * @author okyousgp
 * @date 2023/1/3 16:18
 * @description 封装每个文件rnx文件读取后的结果
 */
public class RnxReaderResult {
    private List<SatelliteOrbitParam> satelliteOrbitParamList;
    private List<GlonassSatelliteTraceArg> glonassSatelliteTraceArgList;

    public RnxReaderResult() {
    }

    public RnxReaderResult(List<SatelliteOrbitParam> satelliteOrbitParamList, List<GlonassSatelliteTraceArg> glonassSatelliteTraceArgList) {
        this.satelliteOrbitParamList = satelliteOrbitParamList;
        this.glonassSatelliteTraceArgList = glonassSatelliteTraceArgList;
    }

    public List<SatelliteOrbitParam> getSatelliteOrbitParamList() {
        return satelliteOrbitParamList;
    }

    public void setSatelliteOrbitParamList(List<SatelliteOrbitParam> satelliteOrbitParamList) {
        this.satelliteOrbitParamList = satelliteOrbitParamList;
    }

    public List<GlonassSatelliteTraceArg> getGlonassSatelliteTraceArgList() {
        return glonassSatelliteTraceArgList;
    }

    public void setGlonassSatelliteTraceArgList(List<GlonassSatelliteTraceArg> glonassSatelliteTraceArgList) {
        this.glonassSatelliteTraceArgList = glonassSatelliteTraceArgList;
    }

    @Override
    public String toString() {
        return "RnxReaderResult{" +
                "satelliteOrbitParamList=" + satelliteOrbitParamList.toString() +
                ", glonassSatelliteTraceArgList=" + glonassSatelliteTraceArgList.toString()+
                '}';
    }
}
