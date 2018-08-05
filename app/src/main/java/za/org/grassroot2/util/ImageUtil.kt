package za.org.grassroot2.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.support.v4.util.LruCache

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.HashMap

import javax.inject.Inject

import za.org.grassroot2.R

class ImageUtil @Inject
constructor(private val context: Context) {

    private val cache = object : LruCache<Int, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 10).toInt()) {
        override fun sizeOf(key: Int?, value: Bitmap): Int {
            return value.rowBytes * value.height / 1024
        }
    }

    fun resizeImageFile(filePath: String, dst: String, desiredWidth: Int, desiredHeight: Int): String {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        val sampleSize = Math.min(imageHeight / desiredHeight, imageWidth / desiredWidth)
        val scale = Math.max(desiredHeight.toFloat() / imageHeight, desiredWidth.toFloat() / imageWidth)
        if (scale < 1) {
            try {
                val attributes = copyExifAttributes(filePath)
                options.inJustDecodeBounds = false
                options.inSampleSize = sampleSize
                var b = BitmapFactory.decodeFile(filePath, options)
                b = Bitmap.createScaledBitmap(b, (scale * imageWidth).toInt(), (scale * imageHeight).toInt(), true)
                val outStream = ByteArrayOutputStream()
                b.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                val fo: FileOutputStream
                val f = File(dst)
                fo = FileOutputStream(f)
                fo.write(outStream.toByteArray())
                fo.flush()
                fo.close()
                saveExifAttributesToNewFile(dst, attributes)
                return f.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return filePath
    }

    fun drawableToBitmap(drawableRes: Int): Bitmap {
        val cached = cache.get(drawableRes)
        if (cached != null) {
            return cached
        } else {
            val d = context.resources.getDrawable(drawableRes)
            val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            d.setBounds(0, 0, c.width, c.height)
            d.draw(c)
            cache.put(drawableRes, b)
            return b
        }
    }

    @Throws(IOException::class)
    private fun saveExifAttributesToNewFile(dst: String, attrs: HashMap<String, String>) {
        val exif = ExifInterface(dst)
        for ((key, value) in attrs) {
            exif.setAttribute(key, value)
        }
        exif.saveAttributes()
    }

    @Throws(IOException::class)
    private fun copyExifAttributes(oldPath: String): HashMap<String, String> {
        val result = HashMap<String, String>()
        val oldExif = ExifInterface(oldPath)
        val attributes = arrayOf(ExifInterface.TAG_ORIENTATION)
        for (i in attributes.indices) {
            val value = oldExif.getAttribute(attributes[i])
            result[attributes[i]] = value
        }
        return result
    }

}
