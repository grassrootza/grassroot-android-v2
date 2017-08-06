package za.org.grassroot.android.view;

import android.view.View;
import android.widget.ProgressBar;

public class ViewUtil {

    public static void safeToggleProgressBar(ProgressBar progressBar, boolean shown) {
        if (progressBar != null) {
            progressBar.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }
}
