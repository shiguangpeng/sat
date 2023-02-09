package com.cdhr.algorithm.satellite.satellitepos.utils;

/**
 * @author okyousgp
 * @date 2022/12/29 17:14
 * @description
 */

/**
 *
 */
public enum BDSOrbitEnum {
    /**
     * 高轨道卫星，距离地球表面35786km。在这个高度上，一颗卫星几乎可以覆盖整个半球，形成一个区域性通信系统，该系统可以为其卫星覆盖范围内的任何地点提供服务
     */
    MEO,
    /**
     * 中轨道地球卫星。距离地球表面10000km左右。主要是指卫星轨道距离地球表面2000～20000km的地球卫星。
     * 它属于地球非同步卫星，主要是作为陆地移动通信系统的补充和扩展，与地面公众网有机结合，实现全球个人移动通信。也可以用作卫星导航系统
     */
    IGSO,
    /**
     * 倾斜地球同步轨道(Inclined GeoSynchronous Orbit)，又名GIO (Geosynchronous Inclined Orbit)。
     * 高度与GEO(Geostationary Orbit)相同，都是约35700km（我国北斗系统部分卫星就用该轨道，轨道高度约35786km），
     * 但是GEO的轨道倾角是0度，而IGSO的轨道倾角是大于0度的任何轨道（我国的北斗系统为55度）
     */
    GEO,

    /**
     * 未定义
     */
    UNDEFINED


}
