package za.org.grassroot2.util;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;

/**
 * Created by qbasso on 13.10.2017.
 */

public class ViewAnimation {

    public static void fadeOut(View v) {
        ViewCompat.animate(v).alpha(0).setListener(new ViewPropertyAnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(View view) {
                super.onAnimationEnd(view);
                view.setVisibility(View.GONE);
            }
        });
    }

    public static void fadeIn(View v) {
        v.setVisibility(View.VISIBLE);
        ViewCompat.setAlpha(v, 0);
        ViewCompat.animate(v).alpha(1);
    }

}
