package ru.yamap.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import com.yandex.runtime.DataProviderWithId
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL

class MapModel(
    private val id: String,
    private val data: ByteArray?
): DataProviderWithId {

    override fun providerId(): String {
        return id
    }

    override fun load(): ByteArray {
        if (data == null) {
            return ByteArray(0)
        }
        return data
    }

}
class ModelCacheManager {

    companion object {

        private val modelCache = mutableMapOf<String, MapModel?>()

        @Throws(IOException::class)
        fun getModelSync(context: Context, url: String): ByteArray {

            // 🌐 загрузка по сети
            if (url.contains("http://") || url.contains("https://")) {
                val aURL = URL(url)
                val conn = aURL.openConnection()
                conn.connect()

                val input = BufferedInputStream(conn.getInputStream())
                val output = ByteArrayOutputStream()

                val buffer = ByteArray(1024 * 4)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                input.close()

                return output.toByteArray()
            }

            // 🧾 base64 модель
            else if (url.contains("data:model")) {
                val pureBase64Encoded = url.substring(url.indexOf(",") + 1)
                return Base64.decode(pureBase64Encoded, Base64.DEFAULT)
            }

            // 📦 assets
            try {
                context.assets.open(url).use {
                    return it.readBytes()
                }
            } catch (_: Exception) { }

            // 📦 raw resources
            val id = context.resources.getIdentifier(url, "raw", context.packageName)

            if (id != 0) {
                context.resources.openRawResource(id).use {
                    return it.readBytes()
                }
            }

            throw IOException("Model not found: $url")
        }

        private fun downloadModel(
            context: Context,
            url: String,
            cb: Callback<ByteArray?>
        ) {
            object : Thread() {
                override fun run() {
                    try {
                        val model = getModelSync(context, url)

                        Handler(Looper.getMainLooper()).post {
                            cb.invoke(model)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            cb.invoke(null)
                        }
                    }
                }
            }.start()
        }

        fun getModel(
            context: Context,
            source: String,
            setModel: (model: MapModel?) -> Unit
        ) {

            modelCache[source]?.let {
                setModel(it)
                return
            }

            downloadModel(context, source, object : Callback<ByteArray?> {
                override fun invoke(arg: ByteArray?) {
                    try {
                        val newModel = MapModel(source, arg)
                        setModel(newModel)
                        modelCache[source] = newModel
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }
    }
}
