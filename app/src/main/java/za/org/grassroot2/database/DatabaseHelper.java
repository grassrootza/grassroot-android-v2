package za.org.grassroot2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.support.ConnectionSource;

import timber.log.Timber;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.R;
import za.org.grassroot2.database.serialization.HashMapPersister;
import za.org.grassroot2.database.serialization.StringCollectionPersister;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public DatabaseHelper(Context context) {
        super(context, BuildConfig.databaseName, null, BuildConfig.databaseVersion, R.raw.ormlite_config);
        DataPersisterManager.registerDataPersisters(StringCollectionPersister.getSingleton(), HashMapPersister.getSingleton());
    }

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, BuildConfig.databaseVersion);
        DataPersisterManager.registerDataPersisters(StringCollectionPersister.getSingleton());
    }

    public synchronized void clearDatabase() {
        try {
            DatabaseTableHelper.clearTables(connectionSource, TAG);
        } catch (IllegalStateException ignore) {
            Timber.e("Ran into issue trying to close the instance.");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        Timber.e("about to set up tables ...");
        DatabaseTableHelper.setUpTables(connectionSource, TAG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        DatabaseTableHelper.dropTables(connectionSource,TAG);
        DatabaseTableHelper.setUpTables(connectionSource,TAG);
    }

    @Override
    public void close() {
        super.close();
    }
}