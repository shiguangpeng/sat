package com.cdhr.algorithm.satellite.satellitepos.utils.ephemerisreader;

import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.OrbitParam;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.GlonassSatelliteTraceArg;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.ParsedParamByRnx;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.RnxReaderResult;
import com.cdhr.algorithm.satellite.satellitepos.utils.pojo.SatelliteOrbitParam;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author okyousgp
 * @date 2022/12/30 10:38
 * @description 基于reinx3.04实现的星历导航数据文件的读取类(按混合星历的标准来读取 ， 兼容不同卫星系统的星历导航数据文件) ver1.0
 */
public class RnxReader {
    public RnxReader() {
    }

    /**
     * @param root   需要遍历的根目录路径
     * @param suffix 文件后缀，如“.rnx”
     * @return root目录下的所有以suffix后缀结尾的文件列表
     * @throws Exception 方法需要抛出异常
     */

    public static List<String> walkPath(String root, String suffix) throws Exception {
        List<String> filePath = new ArrayList<>(12);
        try (Stream<Path> pathStream = Files.walk(Paths.get(root), 2)) {
            Stream<String> temp = pathStream.map(Path::toString).filter(f -> f.endsWith(suffix));
            temp.forEach(filePath::add);
        }
        System.out.printf("[DEBUG]当前文件路径：%s%n", filePath);
        return filePath;
    }

    /**
     * @param filePath
     * @return 读入的文件块
     * @throws FileNotFoundException
     */
    public static List<BufferedReader> readFile(List<String> filePath) throws FileNotFoundException {
        List<BufferedReader> bufferedReaders = new ArrayList<>(24);
        for (String file : filePath) {
            bufferedReaders.add(new BufferedReader(new FileReader(file)));
        }
        return bufferedReaders;
    }

    /**
     * 注意：混合rnx文件中包括了几种卫星，需要判断。因此该方法适合任何<b>导航数据文件</b></br> 处理星历文件分两个步骤:
     * <h4>1. 若有重复的轨道参数，则取最新的轨道参数</h4>
     * <h4>2. 再合并不同文件中的相同卫星的轨道参数</h4>
     *
     * @return 文件中不同卫星的最新的轨道参数
     */
    public static List<RnxReaderResult> getOribitParam(List<BufferedReader> bufferedReaderList) throws IOException {
        // 函数最终返回的数据
        List<RnxReaderResult> results = new ArrayList<>(20);
        char satellitePRNPrefix = ' ';
        final int GLONASSBLOCKLINES = 4;
        final int OTHERBLOCKLINES = 8;
        for (BufferedReader reader : bufferedReaderList) {
            // 一个文件内的卫星prn
            List<String> allPrn = new ArrayList<>(100);
            // 未去重时的数据
            List<SatelliteOrbitParam> satelliteOrbitParamList = new ArrayList<>(30);
            List<GlonassSatelliteTraceArg> glonassSatelliteTraceArgList = new ArrayList<>(30);
            // 读取完成后去除重复的卫星的数据，留下最新的数据
            List<SatelliteOrbitParam> notGlonassResults = new ArrayList<>(30);
            List<GlonassSatelliteTraceArg> glonassResults = new ArrayList<>(30);
            // 文件头结束标志
            boolean isHeaderEnd = false;
            // 一颗卫星的导航数据结束标志
            boolean isOtherSatellite = false;
            try {
                // 循环读取每一个文件的行
                String line = reader.readLine();
                int count = 0;
                while (line != null) {
                    // 找到文件头结尾标志
                    if (line.endsWith("END OF HEADER")) {
                        isHeaderEnd = true;
                    }
                    // 找到一个卫星的数据块结束标志
                    if (isHeaderEnd && Character.toString(line.charAt(0)).matches("^[A-Z].*")) {
                        satellitePRNPrefix = line.charAt(0);
                        isOtherSatellite = true;
                        // System.out.println("[DEBUG]************************新的数据块开始******************************");
                        // System.out.println(line);
                        count++;
                    }

                    // 读取每颗卫星的第一行
                    if (isHeaderEnd && isOtherSatellite) {
                        // 除了GLONASS以外的卫星系统使用这个参数
                        SatelliteOrbitParam satelliteOrbitParam = new SatelliteOrbitParam();
                        // GLONASS系统使用这个参数
                        GlonassSatelliteTraceArg glonassSatelliteTraceArg = new GlonassSatelliteTraceArg();
                        // 开始处理每颗卫星的星历数据的第一行数据块内容，按照reinx3.04规范对照相应的参数
                        // 格洛纳斯 GLONASS（代号为'R'）的每个数据块只有4行，其他的是8行
                        // 解析第一行
                        // 卫星prn号
                        String prn = line.substring(0, 3);
                        // todo: 2023.01.09 新增获取a0, a1, a2三个时间改正数
                        allPrn.add(prn);
                        // 除了GLONASS都是星历下发的utc标准时，读取的如：2022 12 23 23 10 00；中间有空格的数据；按空格分割后再重新组合成标准格式: yyyy-MM-dd HH:mm:ss
                        String[] accuracyTimeArr = line.substring(4, 23).split(" ");

                        String accuracyTime = accuracyTimeArr[0] + "-" + accuracyTimeArr[1] + "-" + accuracyTimeArr[2] + " " + accuracyTimeArr[3] + ":" + accuracyTimeArr[4] + ":" + accuracyTimeArr[5];
                        // 解析剩下的行数
                        // 除了GLONASS系统以外的通用这个轨道参数
                        OrbitParam orbitParam = new OrbitParam();
                        if (satellitePRNPrefix != 'R'){
                            String[] a0Arr = line.substring(23, 42).split("e");
                            double a0 = Double.parseDouble(a0Arr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(a0Arr[1]));
                            String[] a1Arr = line.substring(42, 61).split("e");
                            double a1 = Double.parseDouble(a1Arr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(a1Arr[1]));
                            String[] a2Arr = line.substring(61, 80).split("e");
                            double a2 = Double.parseDouble(a2Arr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(a2Arr[1]));
                            orbitParam.setA0(a0);
                            orbitParam.setA1(a1);
                            orbitParam.setA2(a2);
                        }
                        for (int i = 0; i < (satellitePRNPrefix == 'R' ? GLONASSBLOCKLINES - 1 : OTHERBLOCKLINES - 1); i++) {
                            // 针对不同卫星继续读对应的行数
                            line = reader.readLine();

                            // System.out.println(line);
                            // todo： 将读取的内容在此处处理好，下面的判断可以直接用这里的变量，就不用重复写处理每一个参数的代码了，以此减少重复代码
                            if (satellitePRNPrefix == 'R') {
                                // 处理 GLONASS 格洛纳斯卫星的卫星运动状态，保存到 类 GlonassSatelliteTraceArg 中
                                if (i == 0) {
                                    // 在第二行时处理prn号与时间
                                    glonassSatelliteTraceArg.setRefTime(accuracyTime);
                                    glonassSatelliteTraceArg.setPrn(prn);
                                    // posX
                                    String[] posXArr = line.substring(4, 23).split("e");
                                    double posX = BigDecimal.valueOf(Double.parseDouble(posXArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(posXArr[1])))).doubleValue();
                                    // velocityX
                                    String[] velocityXArr = line.substring(23, 42).split("e");
                                    double velocityX = BigDecimal.valueOf(Double.parseDouble(velocityXArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(velocityXArr[1])))).doubleValue();
                                    // accX
                                    String[] accXArr = line.substring(42, 61).split("e");
                                    double accX = BigDecimal.valueOf(Double.parseDouble(accXArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(accXArr[1])))).doubleValue();
                                    glonassSatelliteTraceArg.setPosition_x(posX);
                                    glonassSatelliteTraceArg.setVelocity_x(velocityX);
                                    glonassSatelliteTraceArg.setAcceleration_x(accX);
                                } else if (i == 1) {
                                    // posY
                                    String[] posYArr = line.substring(4, 23).split("e");
                                    double posY = BigDecimal.valueOf(Double.parseDouble(posYArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(posYArr[1])))).doubleValue();
                                    // velocityY
                                    String[] velocityYArr = line.substring(23, 42).split("e");
                                    double velocityY = BigDecimal.valueOf(Double.parseDouble(velocityYArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(velocityYArr[1])))).doubleValue();
                                    // accY
                                    String[] accYArr = line.substring(42, 61).split("e");
                                    double accY = BigDecimal.valueOf(Double.parseDouble(accYArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(accYArr[1])))).doubleValue();
                                    glonassSatelliteTraceArg.setPosition_y(posY);
                                    glonassSatelliteTraceArg.setVelocity_y(velocityY);
                                    glonassSatelliteTraceArg.setAccerleration_y(accY);
                                } else {
                                    // posZ
                                    String[] posZArr = line.substring(4, 23).split("e");
                                    double posZ = BigDecimal.valueOf(Double.parseDouble(posZArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(posZArr[1])))).doubleValue();
                                    // velocityZ
                                    String[] velocityZArr = line.substring(23, 42).split("e");
                                    double velocityZ = BigDecimal.valueOf(Double.parseDouble(velocityZArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(velocityZArr[1])))).doubleValue();
                                    // accZ
                                    String[] accZArr = line.substring(42, 61).split("e");
                                    double accZ = BigDecimal.valueOf(Double.parseDouble(accZArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(accZArr[1])))).doubleValue();
                                    glonassSatelliteTraceArg.setPosition_z(posZ);
                                    glonassSatelliteTraceArg.setVelocity_z(velocityZ);
                                    glonassSatelliteTraceArg.setAcceleration_z(accZ);
                                }

                            } else {
                                // 处理其他卫星的轨道参数
                                if (i == 0) {
                                    // 在第二行时处理prn号与时间
                                    satelliteOrbitParam.setPrn(prn);
                                    satelliteOrbitParam.setTime(accuracyTime);
                                    // crs
                                    String[] crsStrArr = line.substring(23, 42).split("e");
                                    double crs = BigDecimal.valueOf(Double.parseDouble(crsStrArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(crsStrArr[1])))).doubleValue();
                                    // delta_n
                                    String[] deltaNArr = line.substring(42, 61).split("e");
                                    double deltaN = BigDecimal.valueOf(Double.parseDouble(deltaNArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(deltaNArr[1])))).doubleValue();
                                    // M0
                                    String[] m0Arr = line.substring(61, 80).split("e");
                                    double m0 = BigDecimal.valueOf(Double.parseDouble(m0Arr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(m0Arr[1])))).doubleValue();
                                    orbitParam.setC_rs(crs);
                                    orbitParam.setDelta_n(deltaN);
                                    orbitParam.setM_0(m0);
                                } else if (i == 1) {
                                    // cuc
                                    String[] cucStrArr = line.substring(4, 23).split("e");
                                    double cuc = Double.parseDouble(cucStrArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(cucStrArr[1]));
                                    // e 扁率
                                    String[] eArr = line.substring(23, 42).split("e");
                                    double e = Double.parseDouble(eArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(eArr[1]));
                                    // cus
                                    String[] cusArr = line.substring(42, 61).split("e");
                                    double cus = Double.parseDouble(cusArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(cusArr[1]));
                                    // sqrtA
                                    String[] sqrtAArr = line.substring(61, 80).split("e");
                                    double sqrtA = Double.parseDouble(sqrtAArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(sqrtAArr[1]));
                                    orbitParam.setC_uc(cuc);
                                    orbitParam.set_e(e);
                                    orbitParam.setC_us(cus);
                                    orbitParam.setSqrtA(sqrtA);
                                } else if (i == 2) {
                                    // toe
                                    String[] toeStrArr = line.substring(4, 23).split("e");
                                    long toe = new BigDecimal(toeStrArr[0].replace(" ", "")).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(toeStrArr[1])))).longValue();
                                    // cic
                                    String[] cicArr = line.substring(23, 42).split("e");
                                    double cic = BigDecimal.valueOf(Double.parseDouble(cicArr[0].replace(" ", ""))).multiply(BigDecimal.valueOf(Math.pow(10, Double.parseDouble(cicArr[1])))).doubleValue();
                                    // omega0
                                    String[] omega0Arr = line.substring(42, 61).split("e");
                                    double omega0 = Double.parseDouble(omega0Arr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(omega0Arr[1]));
                                    // cis
                                    String[] cisArr = line.substring(61, 80).split("e");
                                    double cis = Double.parseDouble(cisArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(cisArr[1]));
                                    orbitParam.setToe(toe);
                                    orbitParam.setC_ic(cic);
                                    orbitParam.setOmega_0(omega0);
                                    orbitParam.setC_is(cis);

                                } else if (i == 3) {
                                    // i0
                                    String[] i0StrArr = line.substring(4, 23).split("e");
                                    double i0 = Double.parseDouble(i0StrArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(i0StrArr[1]));
                                    // crc
                                    String[] crcArr = line.substring(23, 42).split("e");
                                    double crc = Double.parseDouble(crcArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(crcArr[1]));
                                    // omega
                                    String[] omegaArr = line.substring(42, 61).split("e");
                                    double omega = Double.parseDouble(omegaArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(omegaArr[1]));
                                    // omegaDot
                                    String[] omegaDotArr = line.substring(61, 80).split("e");
                                    double omegaDot = Double.parseDouble(omegaDotArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(omegaDotArr[1]));
                                    orbitParam.setI_0(i0);
                                    orbitParam.setC_rc(crc);
                                    orbitParam.setOmega(omega);
                                    orbitParam.setOmega_dot(omegaDot);
                                } else if (i == 4) {
                                    // iDot
                                    String[] iDotStrArr = line.substring(4, 23).split("e");
                                    double iDot = Double.parseDouble(iDotStrArr[0].replace(" ", "")) * Math.pow(10, Double.parseDouble(iDotStrArr[1]));
                                    orbitParam.setI_dot(iDot);
                                }
                            }

                        }
                        if (satellitePRNPrefix == 'R') {
                            glonassSatelliteTraceArgList.add(glonassSatelliteTraceArg);
                        } else {
                            satelliteOrbitParam.setOrbitParam(orbitParam);
                            satelliteOrbitParamList.add(satelliteOrbitParam);
                        }
                        // 重新找下一个卫星的数据块
                        isOtherSatellite = false;
                    }
                    line = reader.readLine();
                }
                // System.out.printf("文件共计 %s block数据块。%n", count);
            } catch (IOException e) {
                throw new IOException("文件读取异常");
            } finally {
                reader.close();
                // System.out.println("[DEBUG]#############################当前文件读取完成##################################");
                //results.add(new RnxReaderResult(satelliteOrbitParamList, glonassSatelliteTraceArgList));
            }
            // 读取完一个文件就开始在这个文件内去重
            for (GlonassSatelliteTraceArg satParam1 : glonassSatelliteTraceArgList) {
                for (GlonassSatelliteTraceArg satParam2 : glonassSatelliteTraceArgList) {
                    // 判断只出现一次的卫星prn
                    int prnOccurCount = 0;
                    for (String s : allPrn){
                        if (s.equals(satParam1.getPrn())){
                            ++prnOccurCount;
                        }
                    }
                    // 去重
                    int compareFlag = satParam1.getRefTime().compareTo(satParam2.getRefTime());
                    boolean greaterFlag;
                    // 判断时间是否前大于后
                    greaterFlag = compareFlag > 0;
                    if (satParam1.getPrn().equals(satParam2.getPrn()) && greaterFlag || prnOccurCount == 1) {
                        if (glonassResults.contains(satParam1))
                            continue;
                        glonassResults.add(satParam1);
                    }
                }
            }
            for (SatelliteOrbitParam satParam1 : satelliteOrbitParamList) {
                for (SatelliteOrbitParam satParam2 : satelliteOrbitParamList) {
                    // 判断只出现一次的卫星prn
                    int prnOccurCount = 0;
                    for (String s : allPrn){
                        if (Objects.equals(s, satParam1.getPrn())){
                            ++prnOccurCount;
                        }
                    }
                    // 去重
                    int compareFlag = satParam1.getTime().compareTo(satParam2.getTime());
                    boolean greaterFlag;
                    // 判断时间是否前大于后
                    greaterFlag = compareFlag > 0;
                    if (satParam1.getPrn().equals(satParam2.getPrn()) && greaterFlag || prnOccurCount == 1) {
                        if (notGlonassResults.contains(satParam1))
                            continue;
                        notGlonassResults.add(satParam1);
                    }
                }
            }
            results.add(new RnxReaderResult(notGlonassResults, glonassResults));
        }
        // 注意：显然，若文件中不存在glonass卫星时，RnxReaderResult中的第二个参数为一个容量为0的ArrayList，这里需要注意该问题
        return results;
    }

    /**
     * @description： 合并多rnx文件中的相同卫星的不同时间段的星历数据
     * @param results 读取的RnxReaderResult列表数据
     * @return 分类好的各自卫星的一定时间段内的数据（与目标文件夹下`.rnx`文件组合成的时长有关）
     */
    public static ParsedParamByRnx mergeSatDataInMultiFiles(List<RnxReaderResult> results){
        // results中的一个元素就是一个文件中的glonass卫星数据与非格洛纳斯卫星数据
        List<SatelliteOrbitParam> allOtherSatDataInFile = new ArrayList<>();
        List<GlonassSatelliteTraceArg> allGlonassSatDataInFile = new ArrayList<>();
        for (RnxReaderResult result : results) {
            allOtherSatDataInFile.addAll(result.getSatelliteOrbitParamList());
            allGlonassSatDataInFile.addAll(result.getGlonassSatelliteTraceArgList());
        }
        // 将该结构转化成按卫星prn排列的结构，算法需要的是参数列表，因此需要转化为某个卫星的参数
        // 先处理非glonass卫星
        String[] notGlonassPrn = new String[allOtherSatDataInFile.size()];

        for (int i = 0; i < allOtherSatDataInFile.size(); i++) {
            notGlonassPrn[i] = allOtherSatDataInFile.get(i).getPrn();
        }
        // 使用Set去重
        Set<String> simplifiedNotGlonassPRN = new HashSet<>(Arrays.asList(notGlonassPrn));
        // 记录所有的非GLONASS卫星的PRN号，去重后，根据PRN号在对应的解析结构中寻找
        List<SatelliteOrbitParam[]> parsedNotGlonassResult = new ArrayList<>(50);
        for (String prn : simplifiedNotGlonassPRN){
            List<SatelliteOrbitParam> temp = new ArrayList<>(24);
            for (SatelliteOrbitParam param : allOtherSatDataInFile){
                // 过滤卫星
                if (prn.equals(param.getPrn())){
                    temp.add(param);
                }
            }
            // 每次过滤的卫星添加到结果中
            parsedNotGlonassResult.add(temp.toArray(new SatelliteOrbitParam[0]));
        }

        String[] glonassPrn = new String[allGlonassSatDataInFile.size()];

        for (int i = 0; i < allGlonassSatDataInFile.size(); i++) {
            glonassPrn[i] = allGlonassSatDataInFile.get(i).getPrn();
        }
        // 使用Set去重
        Set<String> simplifiedGlonassPRN = new HashSet<>(Arrays.asList(glonassPrn));
        // 记录所有的GLONASS卫星的PRN号，去重后，根据PRN号在对应的解析结构中寻找
        List<GlonassSatelliteTraceArg[]> parsedGlonassRsult = new ArrayList<>(50);
        for (String prn : simplifiedGlonassPRN){
            List<GlonassSatelliteTraceArg> temp = new ArrayList<>(24);
            for (GlonassSatelliteTraceArg param : allGlonassSatDataInFile){
                // 过滤卫星
                if (prn.equals(param.getPrn())){
                    temp.add(param);
                }
            }
            // 每次过滤的卫星添加到结果中
            parsedGlonassRsult.add(temp.toArray(new GlonassSatelliteTraceArg[0]));
        }

        // todo: 不同文件中的相同卫星还存在时间重复的情况，这里还需要将相同卫星的相同参考时间去重，考虑重写GlonassSatelliteTraceArg类
        //  与SatelliteOrbitParam的HashCode与equals方法，然后使用Set去重
        List<GlonassSatelliteTraceArg[]> finalGlonassResult = new ArrayList<>();
        for (GlonassSatelliteTraceArg[] g : parsedGlonassRsult){
            // 数组中有重复的数据
            Set<GlonassSatelliteTraceArg> gg = new HashSet<>(Arrays.asList(g));
            finalGlonassResult.add(gg.toArray(new GlonassSatelliteTraceArg[0]));
        }

        List<SatelliteOrbitParam[]> finalNotGlonassResult = new ArrayList<>();
        for (SatelliteOrbitParam[] sat : parsedNotGlonassResult){
            // 数组中有重复的数据
            Set<SatelliteOrbitParam> res = new HashSet<>(Arrays.asList(sat));
            finalNotGlonassResult.add(res.toArray(new SatelliteOrbitParam[0]));
        }
        return new ParsedParamByRnx(finalNotGlonassResult, finalGlonassResult);
        //return new ParsedParamByRnx(parsedNotGlonassResult, parsedGlonassRsult);
    }
}
