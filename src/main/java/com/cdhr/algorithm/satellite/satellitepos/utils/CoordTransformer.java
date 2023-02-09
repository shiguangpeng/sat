package com.cdhr.algorithm.satellite.satellitepos.utils;

import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.WGS84EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.CZMLPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author okyousgp
 * @date 2023/1/5 9:53
 * @description WGS-84平面坐标转经纬高；PZ-90坐标转WGS84坐标；CGCS2000转WGS84
 */
public class CoordTransformer {
    /**
     * @param czmlPositionList 算法输出的结果
     * @description PZ-90参考转WGS84转换参数取自论文《PZ-90与 WGS-84之间的转换参数》 高星伟 李毓麟 -中国测绘科学研究院 按照本程序的数据格式处理坐标之间的转换
     */
    public static List<CZMLPosition> pz90ToWGS84(List<CZMLPosition> czmlPositionList) {
        // 转换后坐标
        List<CZMLPosition> wgsCoordList = new ArrayList<>(czmlPositionList.size());
        for (CZMLPosition czmlPosition : czmlPositionList) {
            wgsCoordList.add(new CZMLPosition(czmlPosition.getSecondBias(), czmlPosition.getPosX() - 1.9 * Math.pow(10, -6) * czmlPosition.getPosY(), 2.5 + 1.9 * Math.pow(10, -6) * czmlPosition.getPosX() + czmlPosition.getPosY(), czmlPosition.getPosHeight()));
        }
        return wgsCoordList;
    }

    public static List<CZMLPosition> wgsRcsToLBH(List<CZMLPosition> czmlPositionList) {
        WGS84EllipticParam wgs84EllipticParam = new WGS84EllipticParam();
        double e = wgs84EllipticParam.getE();
        double a = wgs84EllipticParam.getA();
        // 转换后坐标
        List<CZMLPosition> lbhCoordList = new ArrayList<>(czmlPositionList.size());
        for (CZMLPosition czmlPosition : czmlPositionList) {
            double L = (Math.atan(czmlPosition.getPosY() / czmlPosition.getPosX())) * 180 / Math.PI + 180;
            double B = (Math.atan(czmlPosition.getPosHeight() / (Math.sqrt(Math.pow(czmlPosition.getPosX(), 2) + Math.pow(czmlPosition.getPosY(), 2)) * (1 - Math.pow(e, 2))))) * 180 / Math.PI;
            double H = Math.sqrt(Math.pow(czmlPosition.getPosX(), 2)+Math.pow(czmlPosition.getPosY(), 2)+Math.pow(czmlPosition.getPosHeight(), 2)/Math.pow((1-e*e), 2))-a;
            lbhCoordList.add(new CZMLPosition(czmlPosition.getSecondBias(), L, B, H));
        }
        return lbhCoordList;
    }

}
