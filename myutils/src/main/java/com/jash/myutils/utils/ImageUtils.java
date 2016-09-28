package com.jash.myutils.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ImageUtils {
    static LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20 << 20) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };
    private static Executor executor = new ScheduledThreadPoolExecutor(3);
    public static void loadImage(ImageView image, String url, int id) {
        ImageTask task = (ImageTask) image.getTag();
        if (task != null) {
            task.cancel(false);
        }
        Bitmap bitmap = cache.get(url);
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                new ImageTask(image, id).executeOnExecutor(executor, url);
            } else {
                new ImageTask(image, id).execute(url);
            }
        }
    }
}
