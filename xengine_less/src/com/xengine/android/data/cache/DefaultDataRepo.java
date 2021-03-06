package com.xengine.android.data.cache;


import java.util.HashMap;

/**
 * 数据仓库
 */
public class DefaultDataRepo implements XDataRepository {

    private static class SingletonHolder {
        final static DefaultDataRepo INSTANCE = new DefaultDataRepo();
    }

    public static DefaultDataRepo getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private HashMap<String, XDataSource> map;
    private DefaultDataRepo(){
        map = new HashMap<String, XDataSource>();
    }

    @Override
    public void registerDataSource(XDataSource source) {
        if (!map.containsKey(source.getSourceName()))
            map.put(source.getSourceName(), source);
    }

    @Override
    public void unregisterDataSource(XDataSource source) {
        map.remove(source.getSourceName());
    }

    @Override
    public XDataSource getSource(String sourceName) {
        return map.get(sourceName);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
