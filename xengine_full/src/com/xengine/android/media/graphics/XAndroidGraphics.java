package com.xengine.android.media.graphics;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;
import com.xengine.android.utils.XLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 赵之韵.
 * Date: 12-2-28
 * Time: 下午8:02
 */
public class XAndroidGraphics implements XGraphics {

    private static final String TAG = "Graphics";

    /**
     * 缓存已经载入的图片
     */
    private HashMap<String, Bitmap> pool = new HashMap<String, Bitmap>();

    /**
     * 对图片的引用计数器
     */
    private HashMap<String, Integer> refCount = new HashMap<String, Integer>();

    /**
     * Assets资源管理服务
     */
    private AssetManager assets;

    public XAndroidGraphics(Context context) {
        assets = context.getAssets();
    }

    @Override
    public Bitmap getBitmap(String path) {
        StringBuilder logString = new StringBuilder();
        logString.append("Try to get ui bitmap: ").append(path).append(". ");
        Bitmap pic = pool.get(path);
        if(pic == null) {
            try {
                InputStream ins = assets.open(path);
                pic = BitmapFactory.decodeStream(ins);
                pool.put(path, pic);
                logString.append("Success! ").append(pic.toString());
                return pic;
            } catch (IOException e) {
                logString.append("Failed!");
            }
        }
        XLog.d(TAG, logString.toString());
        return pic;
    }

    @Override
    public Bitmap getScalableBitmap(String path) throws IllegalArgumentException{
        if(!path.endsWith(".9.png")){
            throw new IllegalArgumentException("The file is not end with '.9.png'!");
        }
        return getBitmap(path);
    }

    @Override
    public void increaseReferenceCount(String path) {
        Integer count = refCount.get(path);
        if(count == null) {
            count = 1;
        }else {
            count++;
        }
        XLog.d(TAG, path + " ref count increased to: " + count);
        refCount.put(path, count);
    }

    @Override
    public void decreaseReferenceCount(String path) {
        Integer count = refCount.get(path);
        if(count == null) {
            count = 0;
        }else {
            count--;
            if(count < 0) {
                count = 0;
            }
        }

        refCount.put(path, count);
    }

    @Override
    public void recyclePic(String path) {
        if(!pool.containsKey(path)) {
            return;
        }

        StringBuilder logString = new StringBuilder();
        logString.append("Try to recycle: ").append(path).append(". ");
        Integer count = refCount.get(path);

        if(count == null) {
            count = 0;
        }
        count--;

        if(count <= 0) {
            Bitmap pic = pool.get(path);
            refCount.remove(path);
            if(pic != null) {
                pool.remove(path);
                if(!pic.isRecycled()) {
                    logString.append("Recycled ").append(pic.toString());
                    pic.recycle();
                }
            }
        }else {
            logString.append("Not recycled, ref count left: " + count);
            refCount.put(path, count);
        }
        XLog.d(TAG, logString.toString());
    }

    @Override
    public void recycleAll(List<String> paths) {
        for(String path: paths) {
            recyclePic(path);
        }
    }

    @Override
    public void forceRecycleAll() {
        XLog.d(TAG, "Force recycle all bitmap.");
        for(Map.Entry entry: pool.entrySet()) {
            Bitmap bitmap = (Bitmap) entry.getValue();
            if(bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        pool.clear();
        refCount.clear();
    }

    /**
     * TODO 如果做到通用
     * 创建一个可以自由伸缩的drawable（Android的9 patch drawable）
     * @param pic 处理过的bitmap。（将*.9.png文件compile之后，通过BitmapFactory.decode()获得的bitmap对象）
     * @return 可自由放大缩小的drawable对象
     */
    @Override
    public NinePatchDrawable createScalableDrawable(Bitmap pic) {
        byte[] chunk = pic.getNinePatchChunk();
        NinePatch np = new NinePatch(pic, chunk, "");
        return new NinePatchDrawable(np);
    }
}
