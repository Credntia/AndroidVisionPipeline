package online.devliving.mobilevisionpipeline;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 8/10/17.
 */

public class Util {
    public static boolean isPortraitMode(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        return false;
    }

    public static Bitmap getBitmap(Context context, byte[] yuvData, int width, int height){
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Allocation bmData = renderScriptNV21ToRGBA888(
                    context,
                    width,
                    height,
                    yuvData);
            bmData.copyTo(bitmap);
        }
        else{
            YuvImage image = new YuvImage(yuvData, ImageFormat.NV21, width, height, null);
            File tempFile = new File(context.getCacheDir(), "prv_tmp.jpg");
            FileOutputStream outputStream = null;
            boolean success = true;
            try {
                outputStream = new FileOutputStream(tempFile);
                image.compressToJpeg(new Rect(0, 0, width, height), 100, outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                success = false;
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(success){
                bitmap = getBitmapFromPath(tempFile.getAbsolutePath(), width, height);
            }
        }

        return bitmap;
    }

    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, boolean keepAspectRatio)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        final float aspectRatio = (float)height/width;
        int inSampleSize = 1;

        if(keepAspectRatio)
        {
            reqHeight = Math.round(reqWidth * aspectRatio);
        }

        if (reqHeight > 0 && reqWidth > 0) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    static Bitmap getBitmapFromPath(String path, int width, int height)
    {
        if(path != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            int sampleSize = calculateInSampleSize(options, width, height, true);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeFile(path, options);
        }

        return null;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    public interface FrameSizeProvider{
        int frameWidth();
        int frameHeight();
    }
}
