package za.org.grassroot2.util

import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.view.View

/**
 * Created by qbasso on 13.10.2017.
 */

object ViewAnimation {

    fun fadeOut(v: View) {
        ViewCompat.animate(v).alpha(0f).setListener(object : ViewPropertyAnimatorListenerAdapter() {
            override fun onAnimationEnd(view: View?) {
                super.onAnimationEnd(view)
                view!!.visibility = View.GONE
            }
        })
    }

    fun fadeIn(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 0f
        ViewCompat.animate(v).alpha(1f)
    }

}
