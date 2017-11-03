package za.org.grassroot2.extensions

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned

fun Context.getColorCompat(colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.getHtml(stringRes: Int, vararg args: Any) : Spanned{
    val s = getString(stringRes, *args)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(s, 0)
    } else {
        Html.fromHtml(s)
    }
}
