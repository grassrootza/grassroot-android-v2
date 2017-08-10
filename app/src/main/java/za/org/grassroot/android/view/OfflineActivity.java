package za.org.grassroot.android.view;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import za.org.grassroot.android.R;
import za.org.grassroot.android.view.legacy.ArticleContract;

public class OfflineActivity extends AppCompatActivity {

    private ArticleObserver articleObserver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        // Create your sync account
        AccountGeneral.createSyncAccount(this);

        // Perform a manual sync by calling this:
        SyncAdapter.performSync();


        // Setup example content observer
        articleObserver = new ArticleObserver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register the observer at the start of our activity
        getContentResolver().registerContentObserver(
                ArticleContract.Articles.CONTENT_URI, // Uri to observe (our articles)
                true, // Observe its descendants
                articleObserver); // The observer
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (articleObserver != null) {
            // Unregister the observer at the stop of our activity
            getContentResolver().unregisterContentObserver(articleObserver);
        }
    }
    private void refreshArticles() {
        Log.i(getClass().getName(), "Articles data has changed!");
    }

    private final class ArticleObserver extends ContentObserver {
        private ArticleObserver() {
            // Ensure callbacks happen on the UI thread
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Handle your data changes here!!!
            refreshArticles();
        }
    }
}
