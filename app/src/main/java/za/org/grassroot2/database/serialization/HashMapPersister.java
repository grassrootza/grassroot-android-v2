package za.org.grassroot2.database.serialization;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.support.DatabaseResults;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HashMapPersister extends StringType {

    private static final HashMapPersister instance = new HashMapPersister();
    private final Gson gson;

    public static HashMapPersister getSingleton() {
        return instance;
    }

    private HashMapPersister() {
        super(SqlType.STRING, new Class<?>[]{HashMap.class});
        gson = new Gson();
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) {
        return parse(defaultStr);
    }

    @Nullable
    private Object parse(String defaultStr) {
        if (TextUtils.isEmpty(defaultStr)) {
            return null;
        }
        Type type = new TypeToken<HashMap<String, Integer>>() {}.getType();
        return gson.fromJson(defaultStr, type);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return parse((String) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        return javaObject == null ? javaObject : gson.toJson(javaObject);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return super.resultToSqlArg(fieldType, results, columnPos);
    }

    @Override
    public boolean isValidForField(Field field) {
        return (field.getType() == List.class);
    }
}
