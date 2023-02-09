package com.cdhr.algorithm.satellite.satellitepos.utils.elliptic;

import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.impl.EllipticParam;

/**
 * @author okyousgp
 * @date 2022/12/29 15:26
 * @description
 */
public class CGCS2000EllipticParam implements EllipticParam {
    // 基准椭球体长半径
    private final double a = 6378137.0;
    // 基准椭球体扁率
    private final double f = 1 / 298.257222101;
    // 地球自转角速度
    private final double omega_dot_e = 7.2921150 * Math.pow(10, -5);
    // 地球引力常数
    private final double mu = 3.986004418 * Math.pow(10, 14);
    // 真空中的光速
    private final double c = 2.99792458 * Math.pow(10, 8);

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
}
