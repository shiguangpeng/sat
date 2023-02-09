package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

import java.util.List;

/**
 * @author okyousgp
 * @date 2023/1/4 14:08
 * @description 从星历文件中读取后经过转化，适合算法需要的数据结构实体类
 */
public class ParsedParamByRnx {

    /**
     * 从星历文件中解析出来的GPS/伽利略/北斗系统参数
     */
    private List<SatelliteOrbitParam[]> parsedOrbitParamList;
    /**
     * 从星历文件中解析出来的Glonass系统参数
     */
    private List<GlonassSatelliteTraceArg[]> parsedGlonassParamList;

    public ParsedParamByRnx() {
    }

    public ParsedParamByRnx(List<SatelliteOrbitParam[]> parsedOrbitParamList, List<GlonassSatelliteTraceArg[]> parsedGlonassParamList) {
        this.parsedOrbitParamList = parsedOrbitParamList;
        this.parsedGlonassParamList = parsedGlonassParamList;
    }

    public List<SatelliteOrbitParam[]> getParsedOrbitParamList() {
        return parsedOrbitParamList;
    }

    public void setParsedOrbitParamList(List<SatelliteOrbitParam[]> parsedOrbitParamList) {
        this.parsedOrbitParamList = parsedOrbitParamList;
    }

    public List<GlonassSatelliteTraceArg[]> getParsedGlonassParamList() {
        return parsedGlonassParamList;
    }

    public void setParsedGlonassParamList(List<GlonassSatelliteTraceArg[]> parsedGlonassParamList) {
        this.parsedGlonassParamList = parsedGlonassParamList;
    }

    @Override
    public String toString() {
        return "ParsedParamByRnx{" +
                "parsedOrbitParamList=" + parsedOrbitParamList.toString() +
                ", parsedGlonassParamList=" + parsedGlonassParamList.toString() +
                '}';
    }
}
