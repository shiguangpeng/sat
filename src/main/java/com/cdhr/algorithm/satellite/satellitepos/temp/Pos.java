package com.cdhr.algorithm.satellite.satellitepos.temp;

import java.util.Arrays;

/**
 * @author okyousgp
 * @date 2023/1/18 15:15
 * @description
 */
public class Pos {
    private String end;
    private String mfid;
    private String naviCode;
    private String prnSn;
    private String start;
    private SubList[] subList;


    public Pos() {
    }

    public Pos(String end, String mfid, String naviCode, String prnSn, String start, SubList[] subList) {
        this.end = end;
        this.mfid = mfid;
        this.naviCode = naviCode;
        this.prnSn = prnSn;
        this.start = start;
        this.subList = subList;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getMfid() {
        return mfid;
    }

    public void setMfid(String mfid) {
        this.mfid = mfid;
    }

    public String getNaviCode() {
        return naviCode;
    }

    public void setNaviCode(String naviCode) {
        this.naviCode = naviCode;
    }

    public String getPrnSn() {
        return prnSn;
    }

    public void setPrnSn(String prnSn) {
        this.prnSn = prnSn;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public SubList[] getSubList() {
        return subList;
    }

    public void setSubList(SubList[] subList) {
        this.subList = subList;
    }

    @Override
    public String toString() {
        return "Pos{" +
                "end='" + end + '\'' +
                ", mfid='" + mfid + '\'' +
                ", naviCode='" + naviCode + '\'' +
                ", prnSn='" + prnSn + '\'' +
                ", start='" + start + '\'' +
                ", subList=" + Arrays.toString(subList) +
                '}';
    }
}
