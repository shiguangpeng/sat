package com.cdhr.algorithm.satellite.satellitepos;

/**
 * @author okyousgp
 * @date 2023/1/3 9:20
 * @description 使用四阶龙格库塔算法计算glonass卫星位置
 */

import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.PZ90EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.impl.EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.GlonassTraceParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.CZMLPosition;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.GlonassTraceVector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 经典四阶龙格库塔算法的实现，参考文献《自动积分步长的GLONASS卫星轨道龙格库塔积分法》，柯福阳等 reinx3.04版本的glonass导航星历数据文件中给出了再星历播发时刻卫星的运动状态，包括了x,y,z方向 上的地固系中的坐标，速度以及加速度 龙格库塔相较于欧拉公式和改进的欧拉公式，在工程领域常用（4阶），逼近的精度较高且运算简单。
 */

public class GlonassPosition {
    // 轨迹参数只能赋值一次
    private GlonassTraceParam glonassTraceParam;

    public GlonassPosition() {
    }

    public GlonassTraceParam getGlonassTraceParam() {
        return glonassTraceParam;
    }

    public void setGlonassTraceParam(GlonassTraceParam glonassTraceParam) {
        this.glonassTraceParam = glonassTraceParam;
    }

    /**
     * @param glonassTraceParam
     */

    public GlonassPosition(GlonassTraceParam glonassTraceParam) {
        this.glonassTraceParam = glonassTraceParam;
    }



    /**
     * 卫星运动方程 glonass卫星xy平面轨道方程
     *
     * @param r         卫星离地心之间的距离
     * @param mu        地球引力常数
     * @param a         pz-90椭球长半轴
     * @param omega     pz-90下的地球自转角速度
     * @param positionX pz-90中的平面上待求的方向
     * @param positionZ pz-90中的z方向的位置
     * @param velocityY 速度
     * @param accX      日月摄动加速度，可通过星历中得到
     * @return x, y各自方向上的瞬时加速度
     */
    private double satXAccModel(double r, double mu, double a, double omega, double positionX, double positionZ, double velocityY, double accX) {
        return -(mu / Math.pow(r, 3)) * positionX - 1.5 * GlonassTraceParam.j_0_2 * (mu * Math.pow(a, 2) / Math.pow(r, 5)) * positionX * (1 - 5 * Math.pow(positionZ, 2) / Math.pow(r, 2)) + accX + Math.pow(omega, 2) * positionX + 2 * omega * velocityY;
    }

    /**
     * @param r         卫星离地心之间的距离
     * @param mu        地球引力常数
     * @param a         pz-90椭球长半轴
     * @param omega     pz-90下的地球自转角速度
     * @param positionY pz-90中的平面上待求的方向
     * @param positionZ pz-90中的z方向的位置
     * @param velocityX 速度
     * @param accY      日月摄动加速度，可通过星历中得到
     * @return x, y各自方向上的瞬时加速度
     * @return
     */
    private double satYAccModel(double r, double mu, double a, double omega, double positionY, double positionZ, double velocityX, double accY) {
        return -(mu / Math.pow(r, 3)) * positionY - 1.5 * GlonassTraceParam.j_0_2 * (mu * Math.pow(a, 2) / Math.pow(r, 5)) * positionY * (1 - 5 * Math.pow(positionZ, 2) / Math.pow(r, 2)) + accY+ Math.pow(omega, 2) * positionY  - 2 * omega * velocityX ;
    }

    /**
     * @param r
     * @param mu
     * @param a
     * @param positionZ
     * @param accZ
     * @return
     */
    private double satZAccModel(double r, double mu, double a, double positionZ, double accZ) {
        return -(mu / Math.pow(r, 3)) * positionZ - 1.5 * GlonassTraceParam.j_0_2 * (mu * Math.pow(a, 2) / Math.pow(r, 5)) * positionZ * (3 - 5 * Math.pow(positionZ, 2) / Math.pow(r, 2)) + accZ;
    }

    /**
     * @param term 积分区间，以星历播发时间计，±15minutes为佳，建议最多2小时，积分时间越长，误差越大
     * @param step 积分步长，默认选择30秒，30秒是估计精度剧烈变化的分水岭，因此建议积分步长最多30s
     * @return 返回对应时刻的位置和速度
     * @exception RuntimeException  未初始化算法需要的必要参数（参见：{@link GlonassTraceParam}）而抛出的运行时异常
     * @apiNote <i>Notes: </i>出于可复用性考虑，算法不输出卫星信息，在传入多个卫星参数时，自行维护卫星与其参数的关系
     */
    public List<GlonassTraceVector> rk4Alg(int term, int step) throws RuntimeException {
        EllipticParam ellipticParam = new PZ90EllipticParam();
        // 地球引力常数
        double mu = ellipticParam.getMu();
        //参考椭球长半轴a
        double a = ellipticParam.getA();
        // 地球自转角速度
        double omega = ellipticParam.getOmega_dot_e();
        if (this.glonassTraceParam == null) {
            throw new RuntimeException("[ERROR]未初始化GlonassTraceParam类，需要 [GlonassTraceParam] 参数！");
        }
        // 卫星的位置分量
        double x = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getPosition_x())).doubleValue();
        double y = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getPosition_y())).doubleValue();
        double z = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getPosition_z())).doubleValue();
        // 卫星与地心之间的距离
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        // 卫星的瞬时速度分量
        double velocityX = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getVelocity_x())).doubleValue();
        double velocityY = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getVelocity_y())).doubleValue();
        double velocityZ = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getVelocity_z())).doubleValue();
        // 日月摄动加速度分量
        double accX = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getAcc_x())).doubleValue();
        double accY = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getAcc_y())).doubleValue();
        double accZ = BigDecimal.valueOf(1000.0).multiply(BigDecimal.valueOf(this.glonassTraceParam.getAcc_z())).doubleValue();

        // 4阶龙格库塔法
        int isIntCountFlag = term % step;
        int count = term / step;
        // 生成时间点数组，有count+1个元素，对应的结果也是count+1个，因为都包含了初始值，即步长step=0时的结果也在里面
        List<Long> calcTimes = new ArrayList<>(count + 1);
        // 记录结果，每一组6个元素（一个时间点算出的结果包括velocityX,velocityY,velocityZ和x,y,z）
        List<GlonassTraceVector> results = new ArrayList<>(count + 1);
        for (int i = 0; i <= count; i++) {
            // 判断间隔时间数组中的最后一个元素的值；能整除直接就是i*step，不能整除，最终的值就是term
            if (i == count && isIntCountFlag == 0) {
                calcTimes.add((long) term);
                break;
            }
            calcTimes.add((long) step * i);
        }
        // 保存初始值结果
        results.add(new GlonassTraceVector(0, x, y, z, velocityX, velocityY, velocityZ));
        // 在term内按step步长重复计算
        for (int j = 1; j < calcTimes.size(); j++) {
            // 需要更新变量
            double[] a1 = {satXAccModel(r, mu, a, omega, x, z, velocityY, accX), satYAccModel(r, mu, a, omega, y, z, velocityX, accY), satZAccModel(r, mu, a, z, accZ)};
            double[] v1 = {velocityX, velocityY, velocityZ};
            double[] a2 = {satXAccModel(r, mu, a, omega, x + v1[0] * 0.5 * step, z + v1[2] * 0.5 * step, velocityY + a1[1] * 0.5 * step, accX), satYAccModel(r, mu, a, omega, y + v1[1] * 0.5 * step, z + v1[2] * 0.5 * step, velocityX + a1[0] * 0.5 * step, accY), satZAccModel(r, mu, a,  z + v1[2] * 0.5 * step, accZ)};
            double[] v2 = {velocityX + 0.5 * step * a1[0], velocityY + 0.5 * a1[1], velocityZ + 0.5 * a1[2]};
            double[] a3 = {satXAccModel(r, mu, a, omega, x + v2[0] * 0.5 * step, z + v2[2] * 0.5 * step, velocityY + a2[1] * 0.5 * step, accX), satYAccModel(r, mu, a, omega, y + v2[1] * 0.5 * step, z + v2[2] * 0.5 * step, velocityX + a2[0] * 0.5 * step, accY), satZAccModel(r, mu, a, z + v1[2] * 0.5 * step, accZ)};
            double[] v3 = {velocityX + 0.5 * step * a2[0], velocityY + 0.5 * a2[1], velocityZ + 0.5 * a2[2]};
            double[] a4 = {satXAccModel(r, mu, a, omega, x + v3[0] * step, z + v3[2] * step, velocityY + a3[1] * step, accX), satYAccModel(r, mu, a, omega, y + v3[1] * step, z + v3[2] * step, velocityX + a3[0] * step, accY), satZAccModel(r, mu, a, z + v3[2] * step, accZ)};
            double[] v4 = {velocityX + step * a3[0], velocityY + a3[1], velocityZ + a3[2]};
            // 每次更新velocityX,velocityY,velocityZ和x,y,z，如此循环就可以外推区间内任意一点的值
            double tempVx_next = velocityX + step * (a1[0] + 2.0 * a2[0] + 2.0 * a3[0] + a4[0]) / 6.0;
            double tempVy_next = velocityY + step * (a1[1] + 2.0 * a2[1] + 2.0 * a3[1] + a4[1]) / 6.0;
            double tempVz_next = velocityZ + step * (a1[2] + 2.0 * a2[2] + 2.0 * a3[2] + a4[2]) / 6.0;
            double tempX_next = x + step * (v1[0] + 2.0 * v2[0] + 2.0 * v3[0] + v4[0]) / 6.0;
            double tempY_next = y + step * (v1[1] + 2.0 * v2[1] + 2.0 * v3[1] + v4[1]) / 6.0;
            double tempZ_next = z + step * (v1[2] + 2.0 * v2[2] + 2.0 * v3[2] + v4[2]) / 6.0;
            // 保存当前步骤的计算结果
            results.add(new GlonassTraceVector(calcTimes.get(j), tempX_next, tempY_next, tempZ_next, tempVx_next, tempVy_next, tempVz_next));
            // 更新参数
            velocityX = tempVx_next;
            velocityY = tempVy_next;
            velocityZ = tempVz_next;
            x = tempX_next;
            y = tempY_next;
            z = tempZ_next;
            List<CZMLPosition> lbhCoordList = new ArrayList<>();
        }
        return results;
    }

    /**
     * @description 格式化龙格库塔算法计算的结果
     * @param result 卫星x,y,z三个方向上的位置和速度分量
     * @return 预定义CZMLposition字段需要的格式，请参见：{@link CZMLPosition} 类
     */
    public List<CZMLPosition> formatRk4AlgResult(List<GlonassTraceVector> result){
        List<CZMLPosition> czmlPositionList = new ArrayList<>(result.size());
        for (GlonassTraceVector vector : result){
            czmlPositionList.add(new CZMLPosition(vector.getTimeOffSet(), vector.getX(), vector.getY(), vector.getZ()));
        }
        return czmlPositionList;
    }
}
