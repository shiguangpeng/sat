import com.alibaba.fastjson.JSON;
import com.cdhr.algorithm.satellite.satellitepos.GlonassPosition;
import com.cdhr.algorithm.satellite.satellitepos.SatPosAlgLaunch;
import com.cdhr.algorithm.satellite.satellitepos.utils.CoordTransformer;
import com.cdhr.algorithm.satellite.satellitepos.temp.Pos;
import com.cdhr.algorithm.satellite.satellitepos.temp.SubList;
import com.cdhr.algorithm.satellite.satellitepos.utils.elliptic.WGS84EllipticParam;
import com.cdhr.algorithm.satellite.satellitepos.NotGlonassPosition;
import com.cdhr.algorithm.satellite.satellitepos.utils.ephemerisreader.RnxReader;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.GlonassTraceParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.*;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.OrbitParam;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author okyousgp
 * @date 2022/12/28 9:28
 * @description 测试类
 */
public class MainTest {
    /**
     * 伽利略卫星，gps卫星，北斗卫星单条星历计算的测试方法
     *
     * @throws Exception
     */
    @Test
    public void OrbitCalculationTest() throws Exception {
        // 1. 椭球体参数
        WGS84EllipticParam ellipticParam = new WGS84EllipticParam();
        // 2. 轨道参数，以rnx文件中的G15星为例
        OrbitParam orbitParam = new OrbitParam();
        orbitParam.setToe((long) (5.184 * Math.pow(10, 5)));
        orbitParam.setSqrtA(5.153653558730 * Math.pow(10, 3));
        orbitParam.set_e(1.472081558310 * Math.pow(10, -2));
        orbitParam.setI_0(9.314654616750 * Math.pow(10, -1));
        orbitParam.setOmega_0(8.055405879650 * Math.pow(10, -1));
        orbitParam.setOmega(1.169873470130);
        orbitParam.setM_0(8.001123353420 * Math.pow(10, -1));
        orbitParam.setDelta_n(5.149857369370 * Math.pow(10, -9));
        orbitParam.setI_dot(6.557415999840 * Math.pow(10, -10));
        orbitParam.setOmega_dot(-8.319632260360 * Math.pow(10, -9));
        orbitParam.setC_uc(-1.171603798870 * Math.pow(10, -6));
        orbitParam.setC_us(1.038052141670 * Math.pow(10, -5));
        orbitParam.setC_rc(1.689375000000 * Math.pow(10, 2));
        orbitParam.setC_rs(-2.487500000000 * Math.pow(10, 1));
        orbitParam.setC_ic(-1.713633537290 * Math.pow(10, -7));
        orbitParam.setC_is(6.891787052150 * Math.pow(10, -8));
        // 3. 计算结果
        NotGlonassPosition notGlonassPosition = new NotGlonassPosition("G15", orbitParam);
        // double[] tkArray = orbitCalculation.genTkArray("2022-12-24 00:00:00", 3600, 60);
        // System.out.println(Arrays.toString(tkArray));
        // 算法是计算某一刻星历数据的，多个星历数据直接以数据条数循环即可
        List<CZMLPosition> lbhCoordList = new ArrayList<>();
        List<CZMLPosition> satPosition = notGlonassPosition.calcSatPosition("2022-12-24 00:00:00", 7200, 60, false, lbhCoordList);
    }

    /**
     * rnx文件读取测试
     *
     * @throws Exception
     */
    @Test
    public void testRnxReader() throws Exception {
        List<String> filePath = RnxReader.walkPath("C:\\Users\\jiangyanwei\\Desktop\\358\\358", ".rnx");
        List<BufferedReader> readerList = RnxReader.readFile(filePath);
        // OrbitParam oribitParam = RnxReader.getOribitParam(readerList);
        List<RnxReaderResult> rnxReaderResults = RnxReader.getOribitParam(readerList);
        ParsedParamByRnx parsedParamByRnx = RnxReader.mergeSatDataInMultiFiles(rnxReaderResults);
    }

    /**
     * 测试直接读取文件夹下的数据，直接进行一天数据的计算，全流程测试
     */
    @Test
    public void allProgressTest() throws Exception {
        // note: 时间复杂度测试
        long programStart = System.currentTimeMillis();
        // step 1: 读取某个文件夹下所有的rnx文件
        List<String> filePath = RnxReader.walkPath("C:\\Users\\jiangyanwei\\Desktop\\work\\gpsposition\\sampledata\\358\\358\\00", ".rnx");
        List<BufferedReader> readerList = RnxReader.readFile(filePath);
        List<RnxReaderResult> rnxReaderResults = RnxReader.getOribitParam(readerList);
        ParsedParamByRnx parsedParamByRnx = RnxReader.mergeSatDataInMultiFiles(rnxReaderResults);
        // step 2： 通过返回的已经被解析的参数，循环调用算法，根据结构，也分为GLONASS和非GLONASS
        List<GlonassSatelliteTraceArg[]> parsedGlonassParamList = parsedParamByRnx.getParsedGlonassParamList();
        List<SatelliteOrbitParam[]> parsedOrbitParamList = parsedParamByRnx.getParsedOrbitParamList();

        // 3.1： 格洛纳斯卫星姿态计算
        // 初始化算法的输出对象列表
        List<SatellitePositionResult> positionResultList = new ArrayList<>(parsedGlonassParamList.size() + parsedOrbitParamList.size());
        // 初始化输出对象中的结果列表

        GlonassPosition glonassPosition = new GlonassPosition();
        List<CZMLPosition> lbhCoordList = new ArrayList<>();
        for (GlonassSatelliteTraceArg[] traceArgs : parsedGlonassParamList) {
            // 给每个卫星在一天内的时间，按时间先后顺序排序
            Arrays.sort(traceArgs, (o1, o2) -> {
                int rs = o1.getRefTime().compareTo(o2.getRefTime());
                return Integer.compare(rs, 0);
            });
            // 合并同一个卫星一天内的数据，并且根据时间生成统一的时间偏移，方便输出
            List<SatellitePositionResult> temp = new ArrayList<>();
            for (GlonassSatelliteTraceArg currentSatellite : traceArgs) {
                // 循环调用算法
                double positionX = currentSatellite.getPosition_x();
                double positionY = currentSatellite.getPosition_y();
                double positionZ = currentSatellite.getPosition_z();
                double velocityX = currentSatellite.getVelocity_x();
                double velocityY = currentSatellite.getVelocity_y();
                double velocityZ = currentSatellite.getVelocity_z();
                double accelerationX = currentSatellite.getAcceleration_x();
                double accerlerationY = currentSatellite.getAccerleration_y();
                double accelerationZ = currentSatellite.getAcceleration_z();
                // 设置参数
                glonassPosition.setGlonassTraceParam(new GlonassTraceParam(positionX, positionY, positionZ, velocityX, velocityY, velocityZ, accelerationX, accerlerationY, accelerationZ));
                // 积分步长30s为佳
                List<GlonassTraceVector> glonassTraceVectors = glonassPosition.rk4Alg(7200, 30);
                // 将结果转化为输出格式，这就是一个卫星的计算结果
                List<CZMLPosition> czmlPositionList = glonassPosition.formatRk4AlgResult(glonassTraceVectors);
                temp.add(new SatellitePositionResult(currentSatellite.getPrn(), currentSatellite.getRefTime(), czmlPositionList));

                lbhCoordList.addAll(CoordTransformer.wgsRcsToLBH(czmlPositionList));
            }
            // 将同一个卫星的多个SatellitePositionResult参数整合成一个 整合position参数，生成统一的时间偏移
            // 1. 解析时间，因为前面已经按时间先后顺序排序了，因此取temp列表的第一个元素便是该颗卫星的开始时间
            SatellitePositionResult currentSatFirstResult = temp.get(0);
            String startTime = currentSatFirstResult.getReferenceTime();
            List<CZMLPosition> position = currentSatFirstResult.getCzmlPosition();
            for (int i = 1; i < temp.size(); i++) {
                // 从第2个时间开始添加，计算当前时间与开始时间的时间差，再将该时间差统一加到后面的时间偏移中
                String currentTime = temp.get(i).getReferenceTime();
                // 转化为日期对象
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime));
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentTime));
                // 计算时间差，单位秒
                // todo: 若传负数？？？
                int offsetSeconds = (calendar2.get(Calendar.DATE) - calendar1.get(Calendar.DATE)) * 24 * 3600 + 3600 * (calendar2.get(Calendar.HOUR_OF_DAY) - calendar1.get(Calendar.HOUR_OF_DAY)) + 60 * (calendar2.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE) + (calendar2.get(Calendar.SECOND) - calendar1.get(Calendar.SECOND)));
                // 遍历CZMPoistion
                List<CZMLPosition> currentTimePosition = temp.get(i).getCzmlPosition();
                for (CZMLPosition czmlPosition : currentTimePosition) {
                    czmlPosition.setSecondBias(offsetSeconds + czmlPosition.getSecondBias());
                    position.add(czmlPosition);
                }
            }
            positionResultList.add(new SatellitePositionResult(currentSatFirstResult.getSatPrn(), currentSatFirstResult.getReferenceTime(), position));
        }

        // 3.2 非格洛纳斯卫星位置计算
        NotGlonassPosition notGlonassPosition;
        for (SatelliteOrbitParam[] paramsArgs : parsedOrbitParamList) {
            // 给每个卫星在一天内的时间，按时间先后顺序排序
            Arrays.sort(paramsArgs, (o1, o2) -> {
                int rs = o1.getTime().compareTo(o2.getTime());
                return Integer.compare(rs, 0);
            });
            // 合并同一个卫星一天内的数据，并且根据时间生成统一的时间偏移，方便输出
            List<SatellitePositionResult> temp = new ArrayList<>();
            for (SatelliteOrbitParam oneSatellite : paramsArgs) {
                // 1. 轨道参数
                OrbitParam orbitParam = new OrbitParam();
                OrbitParam currentSatOrbitParam = oneSatellite.getOrbitParam();
                orbitParam.setA0(currentSatOrbitParam.getA0());
                orbitParam.setA1(currentSatOrbitParam.getA1());
                orbitParam.setA2(currentSatOrbitParam.getA2());
                orbitParam.setToe(currentSatOrbitParam.getToe());
                orbitParam.setSqrtA(currentSatOrbitParam.getSqrtA());
                orbitParam.set_e(currentSatOrbitParam.get_e());
                orbitParam.setI_0(currentSatOrbitParam.getI_0());
                orbitParam.setOmega_0(currentSatOrbitParam.getOmega_0());
                orbitParam.setOmega(currentSatOrbitParam.getOmega());
                orbitParam.setM_0(currentSatOrbitParam.getM_0());
                orbitParam.setDelta_n(currentSatOrbitParam.getDelta_n());
                orbitParam.setI_dot(currentSatOrbitParam.getI_dot());
                orbitParam.setOmega_dot(currentSatOrbitParam.getOmega_dot());
                orbitParam.setC_uc(currentSatOrbitParam.getC_uc());
                orbitParam.setC_us(currentSatOrbitParam.getC_us());
                orbitParam.setC_rc(currentSatOrbitParam.getC_rc());
                orbitParam.setC_rs(currentSatOrbitParam.getC_rs());
                orbitParam.setC_ic(currentSatOrbitParam.getC_ic());
                orbitParam.setC_is(currentSatOrbitParam.getC_is());
                NotGlonassPosition orbitCompute = new NotGlonassPosition(oneSatellite.getPrn(), orbitParam);
                // 算法是计算某一刻星历数据的，多个星历数据直接以数据条数循环即可
                List<CZMLPosition> lbhCoordList2 = new ArrayList<>();
                List<CZMLPosition> satPosition = orbitCompute.calcSatPosition(oneSatellite.getTime(), 7200, 60, true, lbhCoordList2);
                temp.add(new SatellitePositionResult(oneSatellite.getPrn(), oneSatellite.getTime(), satPosition));
            }
            // 将同一个卫星的多个SatellitePositionResult参数整合成一个 整合position参数，生成统一的时间偏移
            // 1. 解析时间，因为前面已经按时间先后顺序排序了，因此取temp列表的第一个元素便是该颗卫星的开始时间
            SatellitePositionResult currentSatFirstResult = temp.get(0);
            String startTime = currentSatFirstResult.getReferenceTime();
            List<CZMLPosition> position = currentSatFirstResult.getCzmlPosition();
            for (int i = 1; i < temp.size(); i++) {
                // 从第2个时间开始添加，计算当前时间与开始时间的时间差，再将该时间差统一加到后面的时间偏移中
                String currentTime = temp.get(i).getReferenceTime();
                // 转化为日期对象
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime));
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(currentTime));
                // 计算时间差，单位秒
                // todo: 若传负数？？？
                int offsetSeconds = (calendar2.get(Calendar.DATE) - calendar1.get(Calendar.DATE)) * 24 * 3600 + 3600 * (calendar2.get(Calendar.HOUR_OF_DAY) - calendar1.get(Calendar.HOUR_OF_DAY)) + 60 * (calendar2.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE) + (calendar2.get(Calendar.SECOND) - calendar1.get(Calendar.SECOND)));
                // 遍历CZMPoistion
                List<CZMLPosition> currentTimePosition = temp.get(i).getCzmlPosition();
                for (CZMLPosition czmlPosition : currentTimePosition) {
                    czmlPosition.setSecondBias(offsetSeconds + czmlPosition.getSecondBias());
                    position.add(czmlPosition);
                }
            }
            positionResultList.add(new SatellitePositionResult(currentSatFirstResult.getSatPrn(), currentSatFirstResult.getReferenceTime(), position));
        }

        //4. 计算完成后，算法返回一个列表，只需要解析列表结果即可

        long programEnd = System.currentTimeMillis();
        System.out.printf("[DEBUG]:程序开始于：%d，结束于%d，程序耗时：%f 秒\n", programStart, programEnd, (programEnd - programStart) / 1000.0);
        System.out.println(positionResultList);
        // 2023.01.06 临时测试代码，同以G15星为例
        List<String> temp = new ArrayList<>();
//        for (SatellitePositionResult r: positionResultList){
//            if (r.getSatPrn().equals("G15")) {
//                for(CZMLPosition p: r.getCzmlPosition()){
//                    temp.add(BigDecimal.valueOf(p.getSecondBias()).toPlainString());
//                    temp.add(BigDecimal.valueOf(p.getPosX()).toPlainString());
//                    temp.add(BigDecimal.valueOf(p.getPosY()).toPlainString());
//                    temp.add(BigDecimal.valueOf(p.getPosHeight()).toPlainString());
//                }
//            }
//        }
//        System.out.println(temp);
    }


    @Test
    public void test() throws IOException {
        File file = new File("C:\\Users\\jiangyanwei\\Desktop\\work\\gpsposition\\sampledata\\358\\358\\CDDH000X0-2023010418.source");
        String json = FileUtils.readFileToString(file);
        List<Pos> pos = JSON.parseArray(json, Pos.class);
        SubList[] subList = new SubList[0];
        for (Pos p : pos) {
            if (Objects.equals(p.getPrnSn(), "G05")) {
                subList = p.getSubList();
            }
        }
        for (SubList s : subList) {
            System.out.println(Arrays.toString(s.getBlh()));
        }
    }

    /**
     * @throws IOException
     * @author okyousgp
     * @description 测试ecf2eci
     */
    @Test
    public void testEci() throws Exception {
        /*
         *
         * step:
         *      1. UTC先转儒略日
         *      2. 使用儒略日计算GMST
         *      3. 使用ecf2Eci转换为地心惯性坐标系
         */

//        // 1. UTC转儒略日
//        double julianDay = jdayInternal(2023, 2, 7, 12, 0, 0, 0.0);
//        // System.out.println(v);
//        // 2. 利用儒略日计算GMST
//        double gmsTime = gstimeInternal(julianDay);
//        // 3. ecf2Eci
        SatPosAlgLaunch launch = new SatPosAlgLaunch(true);
        List<SatellitePositionResult> positionResultList = launch.startAlg("C:\\Users\\jiangyanwei\\Desktop\\work\\gpsposition\\sampledata\\358\\358", ".rnx");

        // eci reference
        List<CZMLPosition> eciPosition = new ArrayList<>(6000);

        for (SatellitePositionResult res : positionResultList){
            if (res.getSatPrn().equals("G01")){
                System.out.println(res);
                System.out.println("\n");
                // ecfToeci
                List<CZMLPosition> position = res.getCzmlPosition();
                String baseTime = res.getReferenceTime();
                for (CZMLPosition pos : position){
                    double secondBias = pos.getSecondBias();
                    double x = pos.getPosX();
                    double y = pos.getPosY();
                    double z = pos.getPosHeight();
                    // 当前对应时刻的儒略日
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = format.parse(baseTime);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int yr = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutes = calendar.get(Calendar.MINUTE);
                    int seconds = calendar.get(Calendar.SECOND);

                    int offsetHours = (int) (Math.ceil(secondBias) / 3600);
                    int trailSeconds = (int) (Math.ceil(secondBias) % 3600);
                    // 若偏移到下一天，小时数
                    int hour2 = offsetHours + hour;
                    if (hour2 >= 24){
                        hour2 = hour2 - 24;
                        day += 1;
                    }
                    // 偏移分钟数
                    int offsetMins = (int) trailSeconds / 60;
                    int offsetSeconds = (int) trailSeconds % 60;
                    int minutes2 = offsetMins+minutes;
                    if (minutes2 > 60){
                        hour2 += 1;
                        minutes2 = minutes2 - 60;
                    }

                    int seconds2 = seconds + trailSeconds;
                    double gstime = gstimeInternal(jdayInternal(yr, month, day, hour2, minutes2, seconds2, 0));
                    double[] eci = ecfToEci(new double[]{x, y, z}, gstime);
                    eciPosition.add(new CZMLPosition(secondBias, eci[0], eci[1], eci[2]));
                }
            }
        }

        // System.out.println(eciPosition);

        // 构造czml中的position属性形式
        double[] position = new double[eciPosition.size()*4];
        for (int i = 0; i < eciPosition.size(); i++) {
            for (int j = 0; j < 4; j++) {
                CZMLPosition currentPos = eciPosition.get(i);
                position[i * 4] = Math.ceil(currentPos.getSecondBias());
                position[i*4+1] = BigDecimal.valueOf(currentPos.getPosX()).doubleValue();
                position[i*4+2] = BigDecimal.valueOf(currentPos.getPosY()).doubleValue();
                position[i*4+3] = BigDecimal.valueOf(currentPos.getPosHeight()).doubleValue();
            }
        }
        System.out.println(Arrays.toString(position));

    }

    /**
     * @param year
     * @param mon
     * @param day
     * @param hr
     * @param minute
     * @param sec
     * @param msec
     * @return 略儒日
     * @description utc转略儒日，from statellite.js
     */
    private double jdayInternal(int year, int mon, int day, int hr, int minute, int sec, double msec) {
        return (
                ((367.0 * year) - Math.floor((7 * (year + Math.floor((mon + 9) / 12.0))) * 0.25))
                        + Math.floor((275 * mon) / 9.0)
                        + day + 1721013.5
                        + (((((msec / 60000) + (sec / 60.0) + minute) / 60.0) + hr) / 24.0) // ut in days
                // # - 0.5*sgn(100.0*year + mon - 190002.5) + 0.5;
        );
    }

    private double gstimeInternal(double jdut1) {
        final double twoPi = Math.PI * 2;
        final double deg2rad = Math.PI / 180.0;
        final double tut1 = (jdut1 - 2451545.0) / 36525.0;
        double temp = (-6.2e-6 * tut1 * tut1 * tut1)
                + (0.093104 * tut1 * tut1)
                + (((876600.0 * 3600) + 8640184.812866) * tut1) + 67310.54841; // # sec
        temp = ((temp * deg2rad) / 240.0) % twoPi; // 360/86400 = 1/240, to deg, to rad
        //  ------------------------ check quadrants ---------------------
        if (temp < 0.0) {
            temp += twoPi;
        }
        return temp;
    }

    private double[] ecfToEci(double[] ecf, double gmst) {
        // ccar.colorado.edu/ASEN5070/handouts/coordsys.doc
        //
        // [X]     [C -S  0][X]
        // [Y]  =  [S  C  0][Y]
        // [Z]eci  [0  0  1][Z]ecf
        //
        double X = (ecf[0] * Math.cos(gmst)) - (ecf[1] * Math.sin(gmst));
        double Y = (ecf[0] * (Math.sin(gmst))) + (ecf[1] * Math.cos(gmst));
        double Z = ecf[2];
        return new double[]{X, Y, Z};
    }
}


