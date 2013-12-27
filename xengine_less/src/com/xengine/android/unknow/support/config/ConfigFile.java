package com.xengine.android.unknow.support.config;

import java.io.File;

/**
 * <pre>
 * 配置文件类型的接口。
 * 定义了对该类型配置文件的基本操作。
 * User: jasontujun
 * Date: 13-11-7
 * Time: 上午11:12
 * </pre>
 */
public interface ConfigFile<T extends ConfigBean> {

    /**
     * 创建配置文件，并将数据对象的内容写入配置文件。
     * @param bean 数据对象
     * @return 成功则返回true;否则返回false
     */
    boolean create(T bean);

    /**
     * 删除配置文件
     * @param bean 数据对象
     * @return 成功则返回true;否则返回false
     */
    boolean delete(T bean);

    /**
     * 更新配置文件。
     * @param bean 数据对象
     * @return 成功则返回true;否则返回false
     */
    boolean update(T bean);

    /**
     * 解析配置文件，并还原数据对象
     * @param configFile 配置文件
     * @param bean 数据对象(返回值)
     * @return 解析成功则返回true;否则返回false
     */
    boolean parse(File configFile, T bean);

    /**
     * 判断传入的文件是否是合法的配置文件。
     * @param file 传入的文件
     * @return 如果传入的文件是合法的，则返回true;否则返回false
     */
    boolean isValidate(File file);

    /**
     * 获取数据对象对应的配置文件。
     * @param bean 数据对象
     * @return 配置文件（不一定存在）
     */
    File getFile(T bean);
}
