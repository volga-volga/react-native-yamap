package ru.vvdev.yamap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL

object ImageLoader {
    private fun getResId(resName: String, c: Class<*>): Int {
        try {
            val idField = c.getDeclaredField(resName)
            return idField.getInt(idField)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    @Throws(IOException::class)
    private fun getBitmap(context: Context, url: String): Bitmap {
        if (url.contains("http://") || url.contains("https://")) {
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            val bitmap = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
            return bitmap
        }
        val id = context.resources.getIdentifier(url, "drawable", context.packageName)

        return BitmapFactory.decodeResource(
            context.resources,
            id
        ) //getResId(url, R.drawable.class));
    }

    fun DownloadImageBitmap(context: Context, url: String, cb: Callback<Bitmap?>) {
        object : Thread() {
            override fun run() {
                try {
                    val bitmap = getBitmap(context, url)
                    Handler(Looper.getMainLooper()).post { cb.invoke(bitmap) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
