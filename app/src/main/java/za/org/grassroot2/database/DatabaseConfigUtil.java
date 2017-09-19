package za.org.grassroot2.database;

import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    private static final String TAG = DatabaseConfigUtil.class.getSimpleName();

    public static void main(String[] args) {
        try {
            writeConfigFile("ormlite_config.txt");
        } catch (SQLException | IOException error) {
            Log.e(TAG, error.getMessage(), error);
        }
    }
}