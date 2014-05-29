package com.xengine.android.media.graphics.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;

import java.io.ByteArrayOutputStream;

/**
 * 图片工具，主要用于分割现有的图片。
 * Created by 赵之韵.
 * Date: 12-1-10
 * Time: 上午9:09
 */
public class XGraphicUtil {

    /**
     * 将指定的图片，根据行列数分割成若干个小图片
     * @param pic 待分割的图片
     * @param row 要分割的行数
     * @param col 要分割的列数
     * @return 分割完成的图片数组
     */
    public static Bitmap[] splitBitmap(Bitmap pic, int row, int col) {
        int tileWidth = pic.getWidth() / col;
        int tileHeight = pic.getHeight() / row;

        Bitmap[] tiles = new Bitmap[row*col];
        for(int r = 0; r < row; r++) {
            for(int c = 0; c < col; c++) {
                tiles[r*col + c] = Bitmap.createBitmap(pic, c*tileWidth, r*tileHeight, tileWidth, tileHeight);
            }
        }
        return tiles;
    }

    /**
     * 创建一个可以自由伸缩的drawable（Android的9 patch drawable）
     * @param pic 处理过的bitmap。（将*.9.png文件compile之后，通过BitmapFactory.decode()获得的bitmap对象）
     * @return 可自由放大缩小的drawable对象
     */
    public static NinePatchDrawable createScalableDrawable(Bitmap pic) {
        NinePatch np = new NinePatch(pic, pic.getNinePatchChunk(), null);
        return new NinePatchDrawable(np);
    }

    /**
     * 旋转图片
     * @param rotateDegree
     * @param originalBitmap
     * @return
     */
    public static Bitmap rotate(int rotateDegree, Bitmap originalBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap rotateBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        return rotateBitmap;
    }


    /**
     * 将bitmap对象转换为字节数组
     * @param bitmap
     * @return
     */
    public static byte[] bitmap2ByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 将Bitmap压缩成PNG编码，质量为100%存储
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
}
