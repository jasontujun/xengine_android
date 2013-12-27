package com.xengine.android.unknow.support.scan;

import android.text.TextUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * 继承自根路径扫描器接口的实现类。
 * 用底层Linux命令获取可用路径：
 * 主要由于一些山寨手机，无法用Android的来获取根目录的路径。
 * User: jasontujun
 * Date: 13-11-01
 * Time: 上午11:44
 * </pre>
 */
public class CmdRootScanner implements RootScanner {
//    private static final String TAG = CmdRootScanner.class.getSimpleName();
	private static final String TMPFS = "tmpfs";
    private ArrayList<String> dfPaths;
    // Map<设备，挂载点>，排除同一设备的有多个不同挂载点的情况
    private HashMap<String, String> devPathMap;

    public CmdRootScanner() {
        dfPaths = new ArrayList<String>();
        devPathMap = new HashMap<String, String>();
    }

    @Override
	public void scanRoots(List<String> resultPaths) {
		if (resultPaths == null)
			return;

        dfPaths.clear();
        devPathMap.clear();
		// 通过DF命令来获取可用路径。 DF命令：检查文件系统的磁盘空间占用情况
        // m1手机，df命令第一列不是挂载点路径
		Runtime runtime = Runtime.getRuntime();
		try {
//            Log.d(TAG, ">df...................");
			Process process = runtime.exec("df");
			InputStream input = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String strLine;
			while ((strLine = br.readLine()) != null) {
                if (TextUtils.isEmpty(strLine))
                    continue;
//                Log.d(TAG, ">" + strLine);
                // 取出df命令第一列的路径名
                String path = strLine;
				int splitIndex = strLine.indexOf(" ");
				if (splitIndex > 0)
                    path = strLine.substring(0, splitIndex);
                if (path.length() > 1) {
                    // 去除结尾异常字符
                    char c = path.charAt(path.length() - 1);
                    if (!Character.isLetterOrDigit(c) && c != '-' && c != '_')
                        path = path.substring(0, path.length() - 1);
                    // 判断该路径是否存在并可写
                    File canW = new File(path);
                    if (canW.exists() && canW.canRead() && canW.canWrite())
                        if (!dfPaths.contains(path))
                            dfPaths.add(path);
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//        for (String df : dfPaths)
//                Log.d(TAG, "df-result: " + df);

        // 用mount命令去除dfPaths中的属性为tmpfs的路径，并生成devPathMap
		try {
//            Log.d(TAG, ">mount...................");
			Process process = runtime.exec("mount");
			InputStream input = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String strLine;
			while (null != (strLine = br.readLine())) {
                if (TextUtils.isEmpty(strLine))
                    continue;
//                Log.d(TAG, ">" + strLine);
                // 判断mount这一行是否含有df中的路径
                int indexOfDfName = getIndexOfDfNames(strLine);
                if (indexOfDfName == -1)
                    continue;
                // mount这一行路径为tmpfs,则去除dfPaths中该path
                if (strLine.contains(TMPFS)) {
                    dfPaths.remove(indexOfDfName);
                }
                // 否则，该path为有效的，添加进devPathMap
                else {
                    String path = dfPaths.get(indexOfDfName);
                    int index = strLine.indexOf(" ");
                    if (index != -1) {
                        String devName = strLine.substring(0, index);
                        if (!devPathMap.containsKey(devName))
                            devPathMap.put(devName, path);
                        else {
                            // 如果同一设备挂载点有多个，则保留路径名短的挂载点
                            String sameDfName = devPathMap.get(devName);
                            if (path.length() < sameDfName.length())
                                devPathMap.put(devName, path);
                        }
                    }
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        // 返回结果
        resultPaths.addAll(devPathMap.values());
//        for (String result : resultPaths)
//            Log.d(TAG, "mount-result: " + result);
	}

    /**
     * 根据当前的mount命令结果的一行，找到对应的dfPaths中的索引
     * @param mountLine 当前的mount命令行
     * @return 找到则返回对应index，否则返回-1
     */
    private int getIndexOfDfNames(String mountLine) {
        String[] mountColumns = mountLine.split(" ");
        for (int i = 0; i < dfPaths.size(); i++) {
            String path = dfPaths.get(i);
            boolean match = false;
            for (String mountColumn : mountColumns) {
                if (mountColumn.equals(path))
                    match = true;
            }
            if (match)
                return i;
        }
        return -1;
    }
}
