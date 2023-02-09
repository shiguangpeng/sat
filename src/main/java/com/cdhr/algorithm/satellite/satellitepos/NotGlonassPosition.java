package com.cdhr.algorithm.satellite.satellitepos;

import com.cdhr.algorithm.satellite.satellitepos.utils.BDSOrbitType;
import com.cdhr.algorithm.satellite.satellitepos.utils.CoordTransformer;
import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.CGCS2000EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.WGS84EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.impl.EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.OrbitParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.BDSOrbitEnum;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.CZMLPosition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author okyousgp
 * @date 2022/12/26 17:04
 * @description
 */
public class NotGlonassPosition {
    private boolean isBDSatellite;
    private EllipticParam ellipticParam;
    private OrbitParam orbitParam;
    private String prn;

    public NotGlonassPosition() {
    }

    /**
     * @param prn        卫星的prn号
     * @param orbitParam 轨道参数
     */
    public NotGlonassPosition(String prn, OrbitParam orbitParam) {
        this.prn = prn;
        // 初始化椭球参数，根据卫星的prn号确定
        if (prn.startsWith("C")) {
            this.ellipticParam = new CGCS2000EllipticParam();
        } else {
            this.ellipticParam = new WGS84EllipticParam();
        }
        this.orbitParam = orbitParam;
    }


    public EllipticParam getEllipticParam() {
        return ellipticParam;
    }

    public void setEllipticParam(EllipticParam ellipticParam) {
        this.ellipticParam = ellipticParam;
    }

    public OrbitParam getOrbitParam() {
        return orbitParam;
    }

    public void setOrbitParam(OrbitParam orbitParam) {
        this.orbitParam = orbitParam;
    }

    /**
     * @param date
     * @param bias
     * @param interval
     * @param isLBHFlag
     * @param lbhCoordList
     * @return
     */
    // todo: 2023.1.5 星下点轨迹计算？？？
    @SafeVarargs
    public final List<CZMLPosition> calcSatPosition(String date, int bias, int interval, boolean isLBHFlag, List<CZMLPosition>... lbhCoordList) {
        // 1. 计算长半径
        double a = this.calcA(this.orbitParam.getSqrtA());

        // 2. 计算将要观测的归化时间
        double[] obsTimeSeries;
        // todo: 根据rtklib中的描述，星历文件中的时间最好都统一到GPS时间系统中，避免处理跳秒，toc是标准的UTC时间
        // fix: 统一所有卫星系统（GPS/GALILEO/BDS/GLONASS）的参考时间（toe）到GPST，参考rtklib2.4.2手册中的“Time System”一节
        // long toe = this.prn.startsWith("C") ? this.orbitParam.getToe() + 14 : this.orbitParam.getToe();
        long toe = this.orbitParam.getToe();
        try {
            obsTimeSeries = this.genTkArray(date, bias, interval, toe);
        } catch (Exception e) {
            throw new RuntimeException("[ERROR]: 观测时间数组中存在超过本条星历数据参考时间（toe）的有效期，请检查时间偏移参数（bias）是否设置过大。");
        }

        // 3. 计算校正后的卫星平均角速度
        double n = this.computeN(this.ellipticParam.getMu(), this.orbitParam.getDelta_n(), a);

        // 4. 计算真近角点，相对位置，传入不同的归化时间以获得不同的相对位置
        double[] vk = new double[obsTimeSeries.length];
        for (int i = 0; i < obsTimeSeries.length; i++) {
            vk[i] = this.computeVk(this.orbitParam.getM_0(), n, obsTimeSeries[i]);
        }
        double[] omegaK = new double[obsTimeSeries.length];

        // 5. 计算升交点角距，多个时刻，循环调用多次计算多次
        for (int i = 0; i < vk.length; i++) {
            omegaK[i] = this.computeOmega_k(vk[i], this.orbitParam.getOmega());
        }

        // 6. 计算摄动校正后的升交点角距、卫星矢径长度、轨道倾角
        // 改正参数二维数组，vk.length行，3列，每一列就是对应的轨道参数
        double[][] recifyParams = new double[vk.length][3];
        for (int i = 0; i < vk.length; i++) {
            double E_k = this.computeEk(this.computeMk(this.orbitParam.getM_0(), n, obsTimeSeries[i]));
            double[] tempParmas = this.computeRectifyParams(omegaK[i], a, E_k, this.orbitParam.getC_us(), this.orbitParam.getC_rs(), this.orbitParam.getC_is(), this.orbitParam.getC_uc(), this.orbitParam.getC_rc(), this.orbitParam.getC_ic(), this.orbitParam.getI_0(), this.orbitParam.getI_dot(), obsTimeSeries[i]);
            System.arraycopy(tempParmas, 0, recifyParams[i], 0, 3);
        }

        // 7. 计算改正后的坐标
        //double[][] finalResult = new double[vk.length][4];
        List<CZMLPosition> finalResult = new ArrayList<>(vk.length);
        for (int i = 0; i < recifyParams.length; i++) {
            double[] tempCoordsArr = this.cartisan2SpatialCoordinate(recifyParams[i][0], recifyParams[i][1], recifyParams[i][2], this.orbitParam.getOmega_0(), this.orbitParam.getOmega_dot(), this.ellipticParam.getOmega_dot_e(), obsTimeSeries[i],toe);
            // 这个时间需要处理成什么格式
            finalResult.add(new CZMLPosition(obsTimeSeries[i], tempCoordsArr[0], tempCoordsArr[1], tempCoordsArr[2]));
            // finalResult[i][0] = obsTimeSeries[i];
            // finalResult[i][1] = tempCoordsArr[0];
            // finalResult[i][2] = tempCoordsArr[1];
            // finalResult[i][3] = tempCoordsArr[2];
        }
        // 8. 输出czml文件的position参数格式，其时间可为世界协调时
        // 若要根据需要选择是否输出经纬高
        if (isLBHFlag && lbhCoordList.length == 1) {
            CoordTransformer.wgsRcsToLBH(finalResult).forEach(czmlPosition -> lbhCoordList[0].add(czmlPosition));
        }
        return finalResult;
    }

    /**
     * @param a 卫星轨道长半轴的1/2次方
     * @return 卫星轨道长半轴
     */

    // 计算长半径，入参为sqrtA
    private double calcA(double a) {
        return Math.pow(a, 2);
    }


    /**
     * @param date     UTC +0 (协调世界时) 标准化日期字符串格式，例如 `2022-12-27 12:52:55`
     * @param bias     与参数<i>date</i>的偏移的秒数，偏移不可超过两小时，即±7200秒（second）
     * @param interval 采样间隔，单位：秒（second）
     * @return 经过分割的周内秒字符串数组
     * @description 按时间偏移获取时间
     */
    private double[] genTkArray(String date, int bias, int interval, long toe) throws ParseException {
        return getTk(date, bias, interval, toe);
    }

    private double[] getTk(String date, int bias, int interval, long toe) throws ParseException {
        // 要返回的tk数组，观测时刻数组
        double[] tk;
        // 一周内秒数
        // final long weekOfSecond = 604800L;
        // 星期几, 1为周日
        int currentWeek;
        // 当前周内秒GPS时，星历发送时刻toc是UTC，需要使用UTC转为GPS时
        long toc =  utc2GPST(date);

        // 计算观测时间段内的每个观测时刻
        // 如果这段观测时间取余为零，则说明该周期内恰好有count+1个观测时间点；若不为0则将结束时刻视为最后一个观测时间点（即观测时间点为count+最后一个结束时刻）
        int obsTimesFlag = bias % interval;
        // 有多少个时刻
        int obsTimesCount = bias / interval;
        // 加1是因为需要将最后一个观测时刻也纳入到当前系列的观测时刻

        long[] obsSecond = new long[obsTimesCount + 1];
        // 生成周内秒标准的观测时刻
        for (int i = 0; i <= obsTimesCount; i++) {
            if (obsTimesFlag != 0) {
                if (i == obsTimesCount) {
                    obsSecond[i] = toe + bias;
                    break;
                }
            }
            // 观测时间以toe为基准偏移
            obsSecond[i] = toe + (long) i * interval;
        }

        // 初始化将要返回的观测时刻tk数组
        tk = new double[obsTimesCount + 1];
        // 判断这些时间是否满足要求
        for (int i = 0; i < obsSecond.length; i++) {
            // todo: 2023.01.09 修改BUG: 归化时间，还需要加上3个时间改正参数
            double a0 = this.getOrbitParam().getA0();
            double a1 = this.getOrbitParam().getA1();
            double a2 = this.getOrbitParam().getA2();
            // obsSecond[i] - second中；obsSencond代表以toe时间为基准偏移的秒数，second代表toc时间的秒数部分
            // 时间改正后的归化时间
            double tempTK = obsSecond[i] - (a0 + a1 * (obsSecond[i] - toc) + a2 * Math.pow((obsSecond[i] - toc), 2)) - toe;
            if (tempTK > 302400L) {
                tempTK -= 604800L;
            }
            if (tempTK < -302400L) {
                tempTK += 604800L;
            }
            // tk需要在toe时刻的前后两个小时内才能够进行计算
            if (tempTK < -7201 || tempTK > 7201) {
                throw new RuntimeException("时间归化错误。");
            }
            tk[i] = tempTK;
        }
        return tk;
    }

    // 北斗观测时间的周内秒
//    private long getBDSTocSecond(String date) throws ParseException {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
//        LocalDateTime time = LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
//        Date dtTime = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
//        long timeSec = dtTime.getTime();
//        //BDT起始时间 2006年1月1日0时0分0秒 星期日
//        LocalDateTime time0 = LocalDateTime.of(2006, 1, 1, 0, 0, 0);
//        Date dtTime0 = Date.from(time0.atZone(ZoneId.systemDefault()).toInstant());
//        long time0Sec = dtTime0.getTime();
//        //求toc 时钟时间，归化到GPS时
//        return ((timeSec - time0Sec) / 1000) % (604800) - 14;
//    }

    private long utc2GPST(String date) throws ParseException {
        // 根据日期获取当前输入的日期是星期几，默认是格林尼治时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        // 24时计时法
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int currentWeek = week == 1 ? 7 : week - 1;
        // 开始时间：周内秒，加18是因为UTC转到GPS时间存在闰秒，并且闰秒不是一个固定的值，截止目前2023.1.12是18秒
        return  3600L * (currentWeek * 24L + hour) + minute * 60L + second + 18;
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
//        LocalDateTime time = LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
//        Date dtTime = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
//        long timeSec = dtTime.getTime();
//        //GPST起始时间
//        LocalDateTime time0 = LocalDateTime.of(1980, 1, 6, 0, 0, 0);
//        Date dtTime0 = Date.from(time0.atZone(ZoneId.systemDefault()).toInstant());
//        long time0Sec = dtTime0.getTime();
//        //求toc 时钟时间，周内秒
//        return ((timeSec - time0Sec) / 1000) % (604800);
//
//        // todo: 2023.1.12 由星历中的标准UTC时间字符串计算出GPS周，周内秒
////        String currentTime = date;
////        String startTime = "1980-01-06 00:00:00";
    }

    /**
     * @param mu      对应参考框架下的引力常数
     * @param delta_n 卫星平均角速度校正值
     * @param A       卫星轨道长半径（半轴）
     * @return 校正后的卫星平均角速度
     */
    private double computeN(double mu, double delta_n, double A) {
        return Math.sqrt(mu / Math.pow(A, 3)) + delta_n;
    }

    private double computeEk(double M_k) {
        // 2. 计算偏近点角Ek
        // 采用迭代法
        // 初始迭代参数
        double E_old = M_k;
        double E_new = M_k + this.orbitParam.get_e() * Math.sin(E_old);
        int i = 1;
        // 循环迭代10次或者E之间的值小于e-8次方
        while (Math.abs(E_new - E_old) > Math.pow(10, -8)) {
            E_old = E_new;
            E_new = M_k + this.orbitParam.get_e() * Math.sin(E_old);
            i += 1;
            if (i > 10)
                break;
        }
        return E_new;
    }

    private double computeMk(double M_0, double n, double tk) {
        // 1. 计算平近点角
        // M_0: toe时的平近点角
        double M_k = M_0 + n * tk;
        // 将角度化归到0-2Π
        if (M_k < 0) {
            M_k += 2 * Math.PI;
        }
        if (M_k > 2 * Math.PI) {
            M_k -= 2 * Math.PI;
        }
        return M_k;
    }
    // 计算真近点角vk

    /**
     * @param M_0 toe时的平近点角
     * @param n   校正后的卫星平均角速度
     * @param tk  归化时间
     * @return 真近点角vk, 真近点角代表的了卫星在轨道平面的相对位置
     */
    private double computeVk(double M_0, double n, double tk) {
        double M_k = this.computeMk(M_0, n, tk);
        double E_k = this.computeEk(M_k);
        // 3. 计算真近点角
        double Vk;
        double e = this.orbitParam.get_e();
        double cosVk = (Math.cos(E_k) - e) / (1 - e * Math.cos(E_k));
        double sinVk = (Math.sqrt(1 - Math.pow(e, 2)) * Math.sin(E_k)) / (1 - e * Math.cos(E_k));
        if (cosVk == 0) {
            if (sinVk > 0) {
                Vk = Math.PI / 2;
            } else {
                Vk = -Math.PI / 2;
            }
        } else {
            Vk = Math.atan(sinVk / cosVk);
        }
        if (cosVk < 0) {
            if (sinVk >= 0) {
                Vk += Math.PI;
            } else {
                Vk -= Math.PI;
            }
        }
        return Vk;
    }

    /**
     * @param Vk    真近点角
     * @param omega 近地点角距
     * @return 升交点角距
     */
    private double computeOmega_k(double Vk, double omega) {
        return Vk + omega;
    }

    /**
     * @param omega_k
     * @param a
     * @param E_k
     * @param cus
     * @param crs
     * @param cis
     * @param cuc
     * @param crc
     * @param cic
     * @param i_0
     * @param i_dot
     * @param tk
     * @return 返回的数组中依次是：摄动矫正后的升交点角距uk，卫星矢径长度rk，轨道倾角ik
     */
    private double[] computeRectifyParams(double omega_k, double a, double E_k, double cus, double crs, double cis, double cuc, double crc, double cic, double i_0, double i_dot, double tk) {
        double delta_uk = cus * Math.sin(2 * omega_k) + cuc * Math.cos(2 * omega_k);
        double delta_rk = crs * Math.sin(2 * omega_k) + crc * Math.cos(2 * omega_k);
        double delta_ik = cis * Math.sin(2 * omega_k) + cic * Math.cos(2 * omega_k);
        double[] result = new double[3];
        result[0] = omega_k + delta_uk;
        result[1] = a * (1 - this.orbitParam.get_e() * Math.cos(E_k)) + delta_rk;
        result[2] = i_0 + i_dot * tk + delta_ik;
        return result;
    }

    /**
     * @param prn         当isBDSatellite为假时，prn可以传任何字符串，因为此时该参数无用
     * @param omega_0
     * @param omega_dot
     * @param omega_e_dot
     * @param tk
     * @param toe
     * @return
     */
    private double getOmegaK(String prn, double omega_0, double omega_dot, double omega_e_dot, double tk, double toe) {
//        if (prn.startsWith("C")){
//            toe -= 14;
//        }
        // omegaK的默认计算方法
        double OmegaK = omega_0 + (omega_dot - omega_e_dot) * tk - omega_e_dot * toe;
        if (prn.startsWith("C")) {
            // String prnNo = prn.startsWith("C") ? prn.substring(1) : prn;
            // 根据prn在satelliteTypes类中查找其svn，通过svn来判断卫星的类型
            BDSOrbitType bdsOrbitType = new BDSOrbitType(prn);
            BDSOrbitEnum type = bdsOrbitType.getBDSatelliteType();
            switch (type) {
                case MEO:
                case IGSO:
                    break;
                case GEO:
                    OmegaK = omega_0 + omega_dot * tk - omega_e_dot * toe;
                    break;
            }
        }
        return OmegaK;
    }

    /**
     * @param r_k
     * @param u_k
     * @param i_k
     * @param omega_0
     * @param omega_dot
     * @param omega_e_dot
     * @param tk
     * @param toe
     * @return 转化到轨道直角坐标转换到WGS84参考系下的坐标，依次是x, y, height
     */
    private double[] cartisan2SpatialCoordinate(double u_k, double r_k, double i_k, double omega_0, double omega_dot, double omega_e_dot, double tk, double toe) {
        double x = r_k * Math.cos(u_k);
        double y = r_k * Math.sin(u_k);
        double Omega_k = this.getOmegaK(this.prn, omega_0, omega_dot, omega_e_dot, tk, toe);
        double xk = x * Math.cos(Omega_k) - y * Math.cos(i_k) * Math.sin(Omega_k);
        double yk = x * Math.sin(Omega_k) + y * Math.cos(i_k) * Math.cos(Omega_k);
        double zk = y * Math.sin(i_k);
        if (this.prn.startsWith("C")) {
            double a = omega_e_dot * tk;
            // 负5度
            double b = Math.PI / 180 * (-5);
            BDSOrbitType bdsOrbitType = new BDSOrbitType(prn);
            BDSOrbitEnum type = bdsOrbitType.getBDSatelliteType();
            // 将北斗geo卫星转化的奥cgcs2000下的直角坐标系中
            if (type == BDSOrbitEnum.GEO){
                xk = xk * Math.cos(a) + yk * Math.sin(a) * Math.cos(b) + zk * Math.sin(a) * Math.sin(b);
                yk = -xk * Math.sin(a) + yk * Math.cos(a) * Math.cos(b) + zk * Math.cos(a) * Math.sin(b);
                zk = -yk * Math.sin(b) + zk * Math.cos(b);
            }
        }
        return new double[]{xk, yk, zk};
    }
}