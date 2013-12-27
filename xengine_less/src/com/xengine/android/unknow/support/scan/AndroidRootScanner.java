package com.xengine.android.unknow.support.scan;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * <pre>
 * 继承自根路径扫描器接口的实现类。
 * 用Android提供的API获取可用的根路径。
 * User: jasontujun
 * Date: 13-11-01
 * Time: 上午11:43
 * </pre>
 */
public class AndroidRootScanner implements RootScanner {
	private static final String TAG = CmdRootScanner.class.getSimpleName();

	@Override
	public void scanRoots(List<String> resultPaths) {
		if (resultPaths == null)
			return;

        String path = getRoot();
        Log.d(TAG, "Android-root根路径:" + path);
        if (path != null && !resultPaths.contains(path))
            resultPaths.add(path);
	}

    /**
     * 根据Android的API获取根路径
     * @return 返回根路径
     */
    public static String getRoot() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory().getPath();
            if (existAndWritable(path))
                return path;
        }
        return null;
    }

    /** 判断文件是否存在 */
    private static boolean existAndWritable(String path) {
        File file = new File(path);
        return file.exists() && file.canWrite();
    }
}
