package com.cdhr.algorithm.satellite.satellitepos.utils.pojo;

/**
 * @author okyousgp
 * @date 2022/12/27 9:04
 * @description czml position字段结构：[基准偏移秒数, x, y, z]
 */
public class CZMLPosition {
    private double secondBias;
    private double posX;
    private double posY;
    private double posHeight;

    public CZMLPosition() {
    }

    public CZMLPosition(double secondBias, double posX, double posY, double posHeight) {
        this.secondBias = secondBias;
        this.posX = posX;
        this.posY = posY;
        this.posHeight = posHeight;
    }

    public double getSecondBias() {
        return secondBias;
    }

    public void setSecondBias(double secondBias) {
        this.secondBias = secondBias;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosHeight() {
        return posHeight;
    }

    public void setPosHeight(double posHeight) {
        this.posHeight = posHeight;
    }

    @Override
    public String toString() {
        return "CZMLPosition{" +
                "secondBias=" + secondBias +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posHeight=" + posHeight +
                '}';
    }
}
