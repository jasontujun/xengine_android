package com.xengine.android.system.root;

import android.content.Context;
import com.xengine.android.base.filter.XFilter;

import java.util.List;

/**
 * <pre>
 * 设备的根路径获取接口
 * User: jasontujun
 * Date: 14-5-27
 * Time: 下午5:41
 * </pre>
 */
public interface XRoot {

    /**
     * 初始化设备的所有可用根路径(可能有耗时操作，在异步线程调用)
     * @param context
     */
    void init(Context context);

    /**
     * 获取所有可用根路径的集合。
     * @param results 根路径集合(返回值)
     * @return 如果未初始化或在初始化中返回false；初始化完成返回true
     */
    boolean getRoots(List<String> results);


    /**
     * 获取一个可用的根路径(无需初始化，马上返回)
     * @return 返回一个可用的根路径；如果没有，则返回null
     */
    String getRootNow();

    /**
     * 设置路径过滤器
     * @param filter
     */
    void setRootFilter(XFilter<String> filter);

}
