package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

import java.util.Arrays;
import java.util.List;

/**
 * @author okyousgp
 * @date 2023/1/5 11:41
 * @description 定义了算法输出的最终结果，方便调用者便捷解析算法返回的结果
 */
public class SatellitePositionResult {
    private String satPrn;
    private String referenceTime;
    private List<CZMLPosition> czmlPosition;

    public SatellitePositionResult() {
    }

    public SatellitePositionResult(String satPrn, String referenceTime, List<CZMLPosition> czmlPosition) {
        this.satPrn = satPrn;
        this.referenceTime = referenceTime;
        this.czmlPosition = czmlPosition;
    }

    public String getSatPrn() {
        return satPrn;
    }

    public void setSatPrn(String satPrn) {
        this.satPrn = satPrn;
    }

    public String getReferenceTime() {
        return referenceTime;
    }

    public void setReferenceTime(String referenceTime) {
        this.referenceTime = referenceTime;
    }

    public List<CZMLPosition> getCzmlPosition() {
        return czmlPosition;
    }

    public void setCzmlPosition(List<CZMLPosition> czmlPosition) {
        this.czmlPosition = czmlPosition;
    }

    @Override
    public String toString() {
        return "SatellitePositionResult{" +
                "satPrn='" + satPrn + '\'' +
                ", referenceTime='" + referenceTime + '\'' +
                ", czmlPositionList=" + czmlPosition +
                '}';
    }
}
