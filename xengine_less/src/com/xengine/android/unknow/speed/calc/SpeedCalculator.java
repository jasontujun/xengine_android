package com.xengine.android.unknow.speed.calc;

/**
 * <pre>
 * 速度计算器的接口。
 * User: jasontujun
 * Date: 13-10-22
 * Time: 下午3:15
 * </pre>
 */
public interface SpeedCalculator {

    /**
     * 获取当前速度。
     * @param size 当前的文件大小（单位：byte）
     * @return 当前下载速度（单位：byte/s）
     */
    long getSpeed(long size);
}
