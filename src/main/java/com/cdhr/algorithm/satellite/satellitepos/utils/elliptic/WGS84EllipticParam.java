package com.cdhr.algorithm.satellite.satellitepos.utils.elliptic;

import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.impl.EllipticParam;

/**
 * @author okyousgp
 * @date 2022/12/26 16:34
 * @description 椭球参数
 */
public class WGS84EllipticParam implements EllipticParam {
    /**
     * 基准椭球体长半径
     */
    private final double a = 6378137.0;
    /**
     * 基准椭球体扁率
     */
    private final double f = 1 / 298.257223563;
    /**
     * 地球自转角速度
     */
    private final double omega_dot_e = 7.2921151467 * Math.pow(10, -5);
    /**
     * 地球引力常数
     */
    private final double mu = 3.986005 * Math.pow(10, 14);
    /**
     * 真空中的光速
     */
    private final double c = 2.99792458 * Math.pow(10, 8);

    /**
     * 第一偏心率
     */
    private final double e = 0.00669437999013;

    public double getA() {
        return a;
    }

    public double getF() {
        return f;
    }

    public double getOmega_dot_e() {
        return omega_dot_e;
    }

    public double getMu() {
        return mu;
    }

    public double getC() {
        return c;
    }

    public double getE() {
        return e;
    }
}
