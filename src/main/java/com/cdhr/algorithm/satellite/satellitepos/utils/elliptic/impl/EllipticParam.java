package com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.impl;

/**
 * @author okyousgp
 * @date 2022/12/30 8:56
 * @description
 */
public interface EllipticParam {
    // 基准椭球体长半径
    double a = 6378137.0;
    // 基准椭球体扁率
    double f = 1 / 298.257222101;
    // 地球自转角速度
    double omega_dot_e = 7.2921150 * Math.pow(10, -5);
    // 地球引力常数
    double mu = 3.986004418 * Math.pow(10, 14);
    // 真空中的光速
    double c = 2.99792458 * Math.pow(10, 8);


    double getMu();

    double getOmega_dot_e();

    double getA();

    double getF();
    double getC();
}
