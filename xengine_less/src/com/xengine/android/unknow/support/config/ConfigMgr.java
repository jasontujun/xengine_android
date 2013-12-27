package com.xengine.android.unknow.support.config;

import java.util.List;

/**
 * <pre>
 * 针对某一数据类型T的配置文件管理器。
 * 配置文件管理器的本质是：配置文件和内存对象的相互映射。
 * 主要操作包括：从配置文件恢复对象，对象更新进配置文件，配置文件的增删操作等。
 * User: jasontujun
 * Date: 13-11-7
 * Time: 上午11:28
 * </pre>
 */
public interface ConfigMgr<T extends ConfigBean> {

    /**
     * 根据传入的根路径，从配置文件还原数据对象
     * @param rootPath 根路径
     * @return 还原出来的数据对象列表
     */
    List<T> readFromConfig(String rootPath);

    /**
     * 根据传入的根路径，从配置文件还原数据对象
     * @param rootPaths 多个根路径
     * @return 还原出来的数据对象列表
     */
    List<T> readFromConfig(List<String> rootPaths);

    /**
     * 将数据对象写进对应的配置文件中
     * @param bean 数据对象
     * @return 成功返回true,失败返回false
     */
    boolean writeToConfig(T bean);

    /**
     * 将一对数据对象批量写进对应的配置文件中
     * @param beans 数据对象
     * @return 成功返回true,失败返回false
     */
    boolean writeToConfig(List<T> beans);
}
