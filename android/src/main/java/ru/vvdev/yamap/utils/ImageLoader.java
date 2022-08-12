package ru.vvdev.yamap.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;

public class ImageLoader {
    private static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static Bitmap getBitmap(final Context context, final String url) throws IOException {
        if (url.contains("http://") || url.contains("https://")) {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            return bitmap;
        }
        int id = context.getResources().getIdentifier(url, "drawable", context.getPackageName());

        return BitmapFactory.decodeResource(context.getResources(), id); //getResId(url, R.drawable.class));
    }

    public static void DownloadImageBitmap(final Context context, final String url, final Callback<Bitmap> cb) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = getBitmap(context, url);
                    if (bitmap != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                cb.invoke(bitmap);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
