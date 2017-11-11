package za.org.grassroot2.database;

import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import za.org.grassroot2.model.AroundEntity;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Todo;
import za.org.grassroot2.model.task.Vote;

public class DatabaseTableHelper {

    private static Class[] tableClasses = new Class[]{
            Group.class, LiveWireAlert.class, MediaFile.class, UserProfile.class, Meeting.class, Vote.class, Todo.class, MemberRequest.class, AroundEntity.class
    };

    public static void setUpTables(ConnectionSource connectionSource, String TAG) {
        try {
            for (Class cls : tableClasses) {
                TableUtils.createTable(connectionSource, cls);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void dropTables(ConnectionSource connectionSource, String TAG) {
        try {
            for (Class cls : tableClasses) {
                TableUtils.dropTable(connectionSource, cls, false);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void clearTables(ConnectionSource connectionSource, String TAG) {
        try {
            for (Class cls : tableClasses) {
                TableUtils.clearTable(connectionSource, cls);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
