package za.org.grassroot2.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v4.util.LruCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import za.org.grassroot2.R;

public class ImageUtil {

    private final Context context;

    private LruCache<Integer, Bitmap> cache = new LruCache<Integer, Bitmap>((int) (Runtime.getRuntime().maxMemory()/1024 / 10)) {
        @Override
        protected int sizeOf(Integer key, Bitmap value) {
            return value.getRowBytes() * value.getHeight() / 1024;
        }
    };

    @Inject
    public ImageUtil(Context c) {
        context = c;
    }

    public String resizeImageFile(String filePath, String dst, int desiredWidth, int desiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        int sampleSize = Math.min(imageHeight / desiredHeight, imageWidth / desiredWidth);
        float scale = Math.max((float) desiredHeight / imageHeight, (float) desiredWidth / imageWidth);
        if (scale < 1) {
            try {
                HashMap<String, String> attributes = copyExifAttributes(filePath);
                options.inJustDecodeBounds = false;
                options.inSampleSize = sampleSize;
                Bitmap b = BitmapFactory.decodeFile(filePath, options);
                b = Bitmap.createScaledBitmap(b, (int) (scale * imageWidth), (int) (scale * imageHeight), true);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                FileOutputStream fo;
                File f = new File(dst);
                fo = new FileOutputStream(f);
                fo.write(outStream.toByteArray());
                fo.flush();
                fo.close();
                saveExifAttributesToNewFile(dst, attributes);
                return f.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public Bitmap drawableToBitmap(int drawableRes) {
        Bitmap cached = cache.get(drawableRes);
        if (cached != null) {
            return cached;
        } else {
            Drawable d = context.getResources().getDrawable(drawableRes);
            Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, c.getWidth(), c.getHeight());
            d.draw(c);
            cache.put(drawableRes, b);
            return b;
        }
    }

    private void saveExifAttributesToNewFile(String dst, HashMap<String, String> attrs) throws IOException {
        ExifInterface exif = new ExifInterface(dst);
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            exif.setAttribute(e.getKey(), e.getValue());
        }
        exif.saveAttributes();
    }

    public HashMap<String, String> copyExifAttributes(String oldPath) throws IOException {
        HashMap<String, String> result = new HashMap<>();
        ExifInterface oldExif = new ExifInterface(oldPath);
        String[] attributes = new String[] { ExifInterface.TAG_ORIENTATION };
        for (int i = 0; i < attributes.length; i++) {
            String value = oldExif.getAttribute(attributes[i]);
            result.put(attributes[i], value);
        }
        return result;
    }

}
