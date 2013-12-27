package com.xengine.android.unknow.support.config;

/**
 * <pre>
 * 带有配置文件的bean数据对象的接口。
 * User: jasontujun
 * Date: 13-11-7
 * Time: 下午3:28
 * </pre>
 */
public interface ConfigBean {

    /**
     * 获取配置文件所在的目录的绝对路径。
     * @return 返回配置文件所在的目录的绝对路径。
     */
    String getConfigDir();
}
