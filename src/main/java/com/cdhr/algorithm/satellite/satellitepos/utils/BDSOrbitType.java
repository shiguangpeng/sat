package com.cdhr.algorithm.satellite.satellitepos.utils;

import java.util.Objects;

/**
 * @author okyousgp
 * @date 2022/12/29 17:22
 * @description
 */
public class BDSOrbitType {

    /**
     * 北斗卫星前缀，固定为C
     */
    private final String bdsPrefix = "C";
    /**
     * 初始化卫星prn与其svn之间的对应关系，格式：[prn, svn]
     */
    private final String[][] satelliteTypes = new String[][]{
            {"01", "GEO-8"},
            {"02", "GEO-6"},
            {"03", "GEO-7"},
            {"04", "GEO-4"},
            {"05", "GEO-5"},
            {"06", "IGSO-1"},
            {"07", "IGSO-2"},
            {"08", "IGSO-3"},
            {"09", "IGSO-4"},
            {"10", "IGSO-5"},
            {"11", "MEO-3"},
            {"12", "MEO-4"},
            {"13", "IGSO-6"},
            {"14", "MEO-6"},
            {"16", "IGSO-7"},
            {"19", "MEO-1"},
            {"20", "MEO-2"},
            {"21", "MEO-3"},
            {"22", "MEO-4"},
            {"23", "MEO-5"},
            {"24", "MEO-6"},
            {"25", "MEO-11"},
            {"26", "MEO-12"},
            {"27", "MEO-7"},
            {"28", "MEO-8"},
            {"29", "MEO-9"},
            {"30", "MEO-10"},
            {"31", "IGSO-1S"},
            {"32", "MEO-13"},
            {"33", "MEO-14"},
            {"34", "MEO-15"},
            {"35", "MEO-16"},
            {"36", "MEO-17"},
            {"37", "MEO-18"},
            {"38", "IGSO-1"},
            {"39", "IGSO-2"},
            {"40", "IGSO-3"},
            {"41", "MEO-19"},
            {"42", "MEO-20"},
            {"43", "MEO-21"},
            {"44", "MEO-22"},
            {"45", "MEO-23"},
            {"46", "MEO-24"},
            {"56", "IGSO-2S"},
            {"57", "MEO-1S"},
            {"58", "MEO-2S"},
            {"59", "GEO-1"},
            {"60", "GEO-2"},
            {"61", "GEO-3"}
    };
    /**
     * prn，卫星的唯一标识符
     */
    private String prn;
    /**
     * svn号，表征对应prn的卫星的轨道类型
     */
    private String svn;

    public BDSOrbitType() {

    }

    public BDSOrbitType(String prn) {
        this.prn = prn;
    }

    public BDSOrbitEnum getBDSatelliteType() {
        BDSOrbitEnum satelliteType = BDSOrbitEnum.valueOf("UNDEFINED");
        for (String[] type : this.satelliteTypes) {
            if (Objects.equals(bdsPrefix + type[0], this.prn)) {
                // 仅返回卫星的轨道类型
                return BDSOrbitEnum.valueOf(type[1].split("-")[0]);
            }
        }
        return satelliteType;
    }
}
