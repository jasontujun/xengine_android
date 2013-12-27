package com.xengine.android.unknow.support.scan;

import java.util.List;

/**
 * <pre>
 * 对设备的文件系统根路径扫描的扫描器接口。
 * User: jasontujun
 * Date: 13-10-30
 * Time: 下午3:55
 * </pre>
 */
public interface RootScanner {

	/**
	 * 搜索出设备当前所有可用磁盘的根目录
	 * @param resultPaths 扫描结果当参数传入
	 */
	void scanRoots(List<String> resultPaths);

}
