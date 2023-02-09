package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

/**
 * @author okyousgp
 * @date 2022/12/26 16:03
 * @description
 */

/**
 * 轨道参数
 */
public class OrbitParam {
    // 3个时钟参数
    // a0为时钟偏移
    private double a0;
    // a1为时钟漂移
    private double a1;
    // a2为时钟漂移率
    private double a2;
    // 星历参考时间
    private long toe;
    // 卫星轨道长半轴A的平方根
    private double sqrtA;
    // 卫星轨道偏心率
    private double _e;
    // toe时的轨道倾角
    private double i_0;
    // 升交点赤经
    private double omega_0;
    // 近地点角距
    private double omega;
    // toe时的平近点角
    private double M_0;
    // 卫星平均角速度校正值
    private double delta_n;
    // 轨道倾角的变化率
    private double i_dot;
    // 轨道升交点赤经变化率
    private double omega_dot;
    // 升交点角距余弦调和校正振幅
    private double c_uc;
    // 升交点角距正弦调和校正振幅
    private double c_us;
    // 轨道半径余弦调和校正振幅
    private double c_rc;
    // 轨道半径正弦调和校正振幅
    private double c_rs;
    // 轨道倾角余弦调和校正振幅
    private double c_ic;
    // 轨道倾角正弦调和校正振幅
    private double c_is;

    public OrbitParam() {
    }

    public OrbitParam(double a0, double a1, double a2, long toe, double sqrtA, double _e, double i_0, double omega_0, double omega, double m_0, double delta_n, double i_dot, double omega_dot, double c_uc, double c_us, double c_rc, double c_rs, double c_ic, double c_is) {
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
        this.toe = toe;
        this.sqrtA = sqrtA;
        this._e = _e;
        this.i_0 = i_0;
        this.omega_0 = omega_0;
        this.omega = omega;
        M_0 = m_0;
        this.delta_n = delta_n;
        this.i_dot = i_dot;
        this.omega_dot = omega_dot;
        this.c_uc = c_uc;
        this.c_us = c_us;
        this.c_rc = c_rc;
        this.c_rs = c_rs;
        this.c_ic = c_ic;
        this.c_is = c_is;
    }

    public double getA0() {
        return a0;
    }

    public void setA0(double a0) {
        this.a0 = a0;
    }

    public double getA1() {
        return a1;
    }

    public void setA1(double a1) {
        this.a1 = a1;
    }

    public double getA2() {
        return a2;
    }

    public void setA2(double a2) {
        this.a2 = a2;
    }

    public long getToe() {
        return toe;
    }

    public void setToe(long toe) {
        this.toe = toe;
    }

    public double getSqrtA() {
        return sqrtA;
    }

    public void setSqrtA(double sqrtA) {
        this.sqrtA = sqrtA;
    }

    public double get_e() {
        return _e;
    }

    public void set_e(double _e) {
        this._e = _e;
    }

    public double getI_0() {
        return i_0;
    }

    public void setI_0(double i_0) {
        this.i_0 = i_0;
    }

    public double getOmega_0() {
        return omega_0;
    }

    public void setOmega_0(double omega_0) {
        this.omega_0 = omega_0;
    }

    public double getOmega() {
        return omega;
    }

    public void setOmega(double omega) {
        this.omega = omega;
    }

    public double getM_0() {
        return M_0;
    }

    public void setM_0(double m_0) {
        M_0 = m_0;
    }

    public double getDelta_n() {
        return delta_n;
    }

    public void setDelta_n(double delta_n) {
        this.delta_n = delta_n;
    }

    public double getI_dot() {
        return i_dot;
    }

    public void setI_dot(double i_dot) {
        this.i_dot = i_dot;
    }

    public double getOmega_dot() {
        return omega_dot;
    }

    public void setOmega_dot(double omega_dot) {
        this.omega_dot = omega_dot;
    }

    public double getC_uc() {
        return c_uc;
    }

    public void setC_uc(double c_uc) {
        this.c_uc = c_uc;
    }

    public double getC_us() {
        return c_us;
    }

    public void setC_us(double c_us) {
        this.c_us = c_us;
    }

    public double getC_rc() {
        return c_rc;
    }

    public void setC_rc(double c_rc) {
        this.c_rc = c_rc;
    }

    public double getC_rs() {
        return c_rs;
    }

    public void setC_rs(double c_rs) {
        this.c_rs = c_rs;
    }

    public double getC_ic() {
        return c_ic;
    }

    public void setC_ic(double c_ic) {
        this.c_ic = c_ic;
    }

    public double getC_is() {
        return c_is;
    }

    public void setC_is(double c_is) {
        this.c_is = c_is;
    }
}
