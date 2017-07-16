package za.org.grassroot.android.view;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by luke on 2017/07/16.
 */

public class ViewUtil {

    public static void safeToggleProgressBar(ProgressBar progressBar, boolean shown) {
        if (progressBar != null) {
            progressBar.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }
}
