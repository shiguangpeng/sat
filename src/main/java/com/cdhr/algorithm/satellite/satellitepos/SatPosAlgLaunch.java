package com.cdhr.algorithm.satellite.satellitepos;

import com.cdhr.algorithm.satellite.satellitepos.utils.CoordTransformer;
import com.cdhr.algorithm.satellite.satellitepos.utils.ephemerisreader.RnxReader;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.*;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author okyousgp
 * @date 2023/1/19 8:40
 * @description 算法调用入口
 */
public class SatPosAlgLaunch {
    private String path;

    private boolean debug;

    public SatPosAlgLaunch() {
    }

    public SatPosAlgLaunch(boolean debug) {

        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     *
     * @param path 文件夹路径
     * @param fileType 文件后缀，如”.rnx“
     * @return SatellitePositionResult 类 列表
     * @throws Exception
     */
    public List<SatellitePositionResult> startAlg(String path, String fileType) throws Exception {
        // note: 时间复杂度测试
        long programStart = System.currentTimeMillis();
        // step 1: 读取某个文件夹下所有的rnx文件"C:\\Users\\jiangyanwei\\Desktop\\work\\gpsposition\\sampledata\\358\\358\\00",".rnx"
        List<String> filePath = RnxReader.walkPath(path, fileType);
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
        for (GlonassSatelliteTraceArg[] traceArgs : parsedGlonassParamList){
            // 给每个卫星在一天内的时间，按时间先后顺序排序
            Arrays.sort(traceArgs, (o1, o2) -> {
                int rs = o1.getRefTime().compareTo(o2.getRefTime());
                return Integer.compare(rs, 0);
            });
            // 合并同一个卫星一天内的数据，并且根据时间生成统一的时间偏移，方便输出
            List<SatellitePositionResult> temp = new ArrayList<>();
            for (GlonassSatelliteTraceArg currentSatellite : traceArgs){
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
                int offsetSeconds = (calendar2.get(Calendar.DATE) - calendar1.get(Calendar.DATE))*24*3600 + 3600*(calendar2.get(Calendar.HOUR_OF_DAY) - calendar1.get(Calendar.HOUR_OF_DAY))+60*(calendar2.get(Calendar.MINUTE)-calendar1.get(Calendar.MINUTE)+(calendar2.get(Calendar.SECOND) - calendar1.get(Calendar.SECOND)));
                // 遍历CZMPoistion
                List<CZMLPosition> currentTimePosition = temp.get(i).getCzmlPosition();
                for (CZMLPosition czmlPosition : currentTimePosition){
                    czmlPosition.setSecondBias(offsetSeconds+czmlPosition.getSecondBias());
                    position.add(czmlPosition);
                }
            }
            positionResultList.add(new SatellitePositionResult(currentSatFirstResult.getSatPrn(), currentSatFirstResult.getReferenceTime(), position));
        }

        // 3.2 非格洛纳斯卫星位置计算
        NotGlonassPosition notGlonassPosition;
        for (SatelliteOrbitParam[] paramsArgs : parsedOrbitParamList){
            // 给每个卫星在一天内的时间，按时间先后顺序排序
            Arrays.sort(paramsArgs, (o1, o2) -> {
                int rs = o1.getTime().compareTo(o2.getTime());
                return Integer.compare(rs, 0);
            });
            // 合并同一个卫星一天内的数据，并且根据时间生成统一的时间偏移，方便输出
            List<SatellitePositionResult> temp = new ArrayList<>();
            for (SatelliteOrbitParam oneSatellite : paramsArgs){
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
                int offsetSeconds = (calendar2.get(Calendar.DATE) - calendar1.get(Calendar.DATE))*24*3600 + 3600*(calendar2.get(Calendar.HOUR_OF_DAY) - calendar1.get(Calendar.HOUR_OF_DAY))+60*(calendar2.get(Calendar.MINUTE)-calendar1.get(Calendar.MINUTE)+(calendar2.get(Calendar.SECOND) - calendar1.get(Calendar.SECOND)));
                // 遍历CZMPoistion
                List<CZMLPosition> currentTimePosition = temp.get(i).getCzmlPosition();
                for (CZMLPosition czmlPosition : currentTimePosition){
                    czmlPosition.setSecondBias(offsetSeconds+czmlPosition.getSecondBias());
                    position.add(czmlPosition);
                }
            }
            positionResultList.add(new SatellitePositionResult(currentSatFirstResult.getSatPrn(), currentSatFirstResult.getReferenceTime(), position));
        }

        //4. 计算完成后，算法返回一个列表，只需要解析列表结果即可
        long programEnd= System.currentTimeMillis();
        if (this.debug){
            System.out.printf("[DEBUG]:程序开始于：%d，结束于%d，程序耗时：%f 秒\n", programStart, programEnd, (programEnd - programStart)/1000.0);
        }
        return positionResultList;
    }
}
