package io.github.mayubao.commontools;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Image 的工具类
 *
 * Created by mayubao on 2016/5/24.
 * Contact me 345269374@qq.com
 */
public class ImageUtils {

    /**
     * 缩放srcBitmap的宽度为w，高度为宽度缩放比（wRadio）*srcBitmap.getWidth()。如“过高”，“过宽”则另外处理。
     *
     * @param srcBitmap
     *            目标位图，缩放成功后会被回收
     * @param w
     *            目标宽
     * @param h
     *            目标高
     * @return
     */
    public static Bitmap fitBitmap(Bitmap srcBitmap, int w, int h, boolean shouldRecycle) {
        if (srcBitmap != null) {
            Bitmap resizedBitmap = null;
            try {
                int srcWidth = srcBitmap.getWidth();
                int srcHeight = srcBitmap.getHeight();

                float wRadio = ((float) w) / srcWidth;
                float hRadio = ((float) h) / srcHeight;

                if (srcWidth / srcHeight > 4) { // "过宽"
                    if (hRadio > 1.0f) {
                        hRadio = 1.0f;
                    }
                } else if (srcHeight / srcWidth > 4) {// “过高”
                    if (wRadio > 1.0f) {
                        wRadio = 1.0f;
                    }
                } else {// 根据宽比率缩放高
                    hRadio = wRadio;
                }

                Matrix matrix = new Matrix();
                matrix.postScale(wRadio, hRadio);
                // DLog.i("fitBitmap",
                // "original bitmap size "+srcBitmap.getByteCount()+"original width : "+
                // srcWidth + " original height : " + srcHeight+"\n"
                // +"scaleWidth : "+wRadio+" scaleHeight : "+hRadio);

                resizedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth,
                        srcHeight, matrix, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (resizedBitmap != null && srcBitmap != resizedBitmap) {
                if(shouldRecycle){
                    srcBitmap.recycle();
                    srcBitmap = null;
                }
            } else {
                resizedBitmap = srcBitmap;
            }
            // DLog.i("fitBitmap",
            // "scaled bitmap size "+resizedBitmap.getByteCount()+"scaled width : "+
            // resizedBitmap.getWidth() + " scaled height : " +
            // resizedBitmap.getHeight());
            return resizedBitmap;
        } else {
            return null;
        }
    }

    /**
     * bitmap转换成byte[]————注意，图片尺寸没变，变的只是文件大小（图片的位深度改变了）
     *
     * @param maxKByteCount
     *            最大千字节数（比方说图片要压缩成32K，则传32）
     **/
    public static byte[] bitmap2Byte(Bitmap srcBitmap, int maxKByteCount) {
        if (srcBitmap == null) {
            return null;
        }

        byte[] bitmapByte = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int option = 98;
            while (baos.toByteArray().length / 1024 >= maxKByteCount && option > 0) {// 微信分享的图片不能找个32K
                baos.reset();
                srcBitmap.compress(Bitmap.CompressFormat.JPEG, option, baos);
                option -= 2;
            }
            bitmapByte = baos.toByteArray();
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!srcBitmap.isRecycled()) {
                srcBitmap.recycle();
                srcBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmapByte;
    }


    /**
     * 压缩图片到指定的文件去————注意，图片尺寸没变，变的只是文件大小（图片的位深度改变了）
     *
     * @param srcBitmap
     * @param maxKByteCount 最大千字节数（比方说图片要压缩成32K，则传32）
     * @param targetPath	目标图片地址
     * @throws IOException
     */
    public static boolean compressBitmap(Bitmap srcBitmap, int maxKByteCount, String targetPath) {
        boolean result = false;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int option = 98;
            while (baos.toByteArray().length / 1024 >= maxKByteCount && option > 0) {
                baos.reset();
                srcBitmap.compress(Bitmap.CompressFormat.JPEG, option, baos);
                option -= 2;
            }
            byte[] bitmapByte = baos.toByteArray();

            File targetFile = new File(targetPath);
            if(!targetFile.exists()){
                targetFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(bitmapByte);

            result = true;

            try {
                fos.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!srcBitmap.isRecycled()) {
                srcBitmap.recycle();
                srcBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 压缩图片大小不超过maxByteCount，成功则回收srcBitmap。
     *
     * @param srcBitmap
     *            原图
     * @param maxByteCount
     *            最大字节数
     * @return
     */
    private static Bitmap compressBitmap(Bitmap srcBitmap, long maxByteCount) {
        if (srcBitmap == null) {
            return null;
        }
        Bitmap retBitmap = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int quality = 100;
            while (bos.toByteArray().length > maxByteCount) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
                bos.reset();// 重置baos即清空baos
                srcBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);// 这里压缩options%，把压缩后的数据存放到baos中
                quality -= 1;// 每次都减少15
            }
            ByteArrayInputStream isBm = new ByteArrayInputStream(
                    bos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
            Options options = new Options();
            options.inPreferredConfig = srcBitmap.getConfig();
            retBitmap = BitmapFactory.decodeStream(isBm, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (retBitmap != null && retBitmap != srcBitmap) {
            srcBitmap.recycle();
            srcBitmap = null;
        } else {
            retBitmap = srcBitmap;
        }
        return retBitmap;
    }

    /**
     * 除了按w、h缩放，再乘以缩放因子ratio
     *
     * @param path
     *            图片路径
     * @param w
     *            显示宽度
     * @param h
     *            显示高度
     * @param ratio
     *            缩放比例 （》1 缩小，《1放大）
     * @return
     */
    public static Bitmap scaleBitmap(String path, int w, int h, int ratio) {
        Bitmap bit = null;
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        bit = BitmapFactory.decodeFile(path, opts);
        int imageHeight = opts.outHeight;
        int imageWidth = opts.outWidth;
        int heightRatio = Math.round((float) imageHeight / (float) h);
        int widthRatio = Math.round((float) imageWidth / ((float) w));
        opts.inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;

        if (ratio != 1) {
            opts.inSampleSize = opts.inSampleSize * ratio;
        }

        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Config.RGB_565;
        try {
            bit = BitmapFactory.decodeFile(path, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bit;
    }

    /**
     * 创建bitmap，会缩放，w，h只作为缩放参考值，不是bitmap的宽高值
     *
     * @param path
     *            图片路径
     * @param w
     *            参考宽
     * @param h
     *            参考高
     * @return
     */
    public static Bitmap getBitmapWithScale(String path, int w, int h) {
        Bitmap bit = null;
        bit = scaleBitmap(path, w, h, 1);
        return bit;
    }

    /**
     * 创建bitmap，不缩放
     *
     * @param path
     *            文件路径
     * @return bitmap
     */
    public static Bitmap getBitmapNoScale(String path) {
        Bitmap bit = null;
        bit = BitmapFactory.decodeFile(path);
        return bit;
    }

    /**
     * 获取手机本地图片
     *
     * @param remoteUrl
     * @param w
     * @param h
     * @return
     */
    public static Bitmap getBitmapByStream(String remoteUrl, int w, int h) {
        Bitmap bit = null;
        try {
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            bit = BitmapFactory.decodeStream(new URL(remoteUrl).openStream(),
                    null, opts);
            int i = (int) (opts.outWidth * 1.0 / w);
            int i1 = computeSampleSize(opts, -1, 100 * 100);
            opts.inSampleSize = (i + i1) / 2;
            System.out.println("pre:==" + i + "        ed:=="
                    + opts.inSampleSize);
            opts.inJustDecodeBounds = false;

            bit = BitmapFactory.decodeStream(new URL(remoteUrl).openStream(),
                    null, opts);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bit;
    }

    /**
     * 合并两张bitmap为一张
     *
     * @param background
     * @param foreground
     * @return Bitmap
     */
    public static Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if (background == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap
                .createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2,
                (bgHeight - fgHeight) / 2, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newmap;
    }

    public static int computeSampleSize(Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 100 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return -1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 获得圆边图片的方法
     *
     * @param bitmap
     * @param needEdge
     *            是否需要边框
     * @param edgeColor
     *            边框颜色
     * @param edgeWidth
     *            边框宽度
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,
                                                boolean needEdge, int edgeColor, float edgeWidth) {
        if (null != bitmap) {
            Bitmap output = null;
            try {
                output = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), Config.ARGB_4444);
                Canvas canvas = new Canvas(output);

                final int color = 0xffff0000;
                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                        bitmap.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                float radius = bitmap.getWidth() < bitmap.getHeight() ? (rectF.right - rectF.left) / 2
                        : (rectF.bottom - rectF.top) / 2;
                canvas.drawCircle(rectF.left + (rectF.right - rectF.left) / 2,
                        rectF.top + (rectF.bottom - rectF.top) / 2, radius,
                        paint);

                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                if (needEdge) {
                    paint.setColor(edgeColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(edgeWidth);
                    canvas.drawCircle(rectF.left + (rectF.right - rectF.left)
                                    / 2, rectF.top + (rectF.bottom - rectF.top) / 2,
                            radius, paint);
                    paint.setColor(Color.GRAY);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1f);
                    canvas.drawCircle(rectF.left + (rectF.right - rectF.left)
                                    / 2, rectF.top + (rectF.bottom - rectF.top) / 2,
                            radius, paint);
                }

                bitmap.recycle();
                bitmap = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return output;
        } else {
            return null;
        }
    }

    public static boolean addToGallery(Activity mAct, String filePath) {
        try {
            String extension = null;
            if (filePath.contains(".")) {
                extension = filePath.substring(filePath.lastIndexOf(".") + 1);
            }
            String mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);

            if (!TextUtils.isEmpty(mimeType) && mimeType.startsWith("image")) {
                final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
                final String IMAGE_MIME_TYPE = "image/jpeg";

                ContentValues values = new ContentValues(4);

                values.put(Images.Media.TITLE,
                        filePath.substring(filePath.lastIndexOf("/") + 1));
                values.put(Images.Media.DISPLAY_NAME,
                        filePath.substring(filePath.lastIndexOf("/") + 1));
                values.put(Images.Media.MIME_TYPE, IMAGE_MIME_TYPE);
                values.put(Images.Media.DATA, filePath);

                mAct.getContentResolver().insert(STORAGE_URI, values);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean saveImageToLocalGallery(Context ctx, Bitmap bm) {
        String url = Images.Media.insertImage(
                ctx.getContentResolver(), bm, "", "");

        if (null == url) {
            return false;
        } else {
            try {
                ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                        .parse("file://"
                                + Environment.getExternalStorageDirectory())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    public static Bitmap drawTextOnBitmap(Bitmap bmp, String text,
                                          int textColor, float textSize) {
        if (null == bmp) {
            return null;
        }

        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Bitmap resBmp = null;

        try {
            resBmp = Bitmap
                    .createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(resBmp);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);

            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawBitmap(bmp, new Matrix(), paint);

            paint.setColor(textColor);
            paint.setTextSize(textSize);
            Rect txtBounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), txtBounds);
            canvas.drawText(text, (width - txtBounds.width()) / 2,
                    (height - txtBounds.height()) / 2 + 3, paint);

            bmp.recycle();
            bmp = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resBmp;
    }


    /**
     * 压缩指定的Bitmap到目标文件（根据位深度去压缩）
     *
     * @param srcBitmap
     * @param targetPath
     * @return
     */
    public static boolean compressBitmapTo(Bitmap srcBitmap, String targetPath){
        boolean result = false;
        byte[] bytes = bitmap2Byte(srcBitmap, 100);


        try {
            File targetFile = new File(targetPath);
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(bytes);

            result = true;

            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!srcBitmap.isRecycled()) {
                srcBitmap.recycle();
                srcBitmap = null;
            }
        }catch (Exception e){
            Log.i("ImageUtils", "writeBitmapTo======>>>Exception");
        }

        return result;
    }


    /**
     * 根据获取的源文件 压缩成屏幕等宽
     * @param context
     * @param srcPath
     * @param targetPath
     * @return
     */
    public static boolean scaleBitmapWith(Context context, String srcPath, String targetPath){
        //1.根据屏幕大小尺寸压缩
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
        Bitmap bitmap = scaleBitmap(srcPath, screenWidth, screenHeight, 1);

        //2.压缩到指定的文件
        boolean result = compressBitmapTo(bitmap, targetPath);
        return result;
    }

    /**
     * 生成时间戳图片名称
     *
     * @return
     */
    public static String getTimestampImageName(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(new Date());
        String timestampImageName = "picture_" + timeStamp + ".jpg";

        return timestampImageName;
    }

}
