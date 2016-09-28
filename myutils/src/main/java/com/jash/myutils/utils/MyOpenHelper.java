package com.jash.myutils.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.internal.UnsafeAllocator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyOpenHelper extends SQLiteOpenHelper {
    MyOpenHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void save(Object o) {
        Class<?> table = o.getClass();
        createTableIfNotExists(table);
        SQLiteDatabase db = getWritableDatabase();
        save(o, table, db);
    }

    private void save(Object o, Class<?> table, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        Field[] fields = table.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers)){
                field.setAccessible(true);
                try {
                    values.put(field.getName(), field.get(o) + "");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        db.replace(table.getName().replaceAll("\\.", "_"), null, values);
    }

    public void saveAll(Collection collection){
        if (collection.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        Object next = collection.iterator().next();
        createTableIfNotExists(next.getClass());
        db.beginTransaction();
        for (Object o : collection) {
            save(o, o.getClass(), db);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    public<T> List<T> queryAll(Class<T> table) {
        if (!isTableExists(table)) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table.getName().replaceAll("\\.", "_"), null, null, null, null, null, null);
        List<T> list = initList(table, cursor);
        return list;
    }

    public<T> List<T> queryAll(Class<T> table, String orderBy) {
        if (!isTableExists(table)) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table.getName().replaceAll("\\.", "_"), null, null, null, null, null, orderBy);
        List<T> list = initList(table, cursor);
        return list;
    }

    public<T> List<T> queryAll(Class<T> table, String orderBy, int limit) {
        if (!isTableExists(table)) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table.getName().replaceAll("\\.", "_"), null, null, null, null, null, orderBy, String.valueOf(limit));
        List<T> list = initList(table, cursor);
        return list;
    }

    public<T> T queryById(Class<T> table, Object id) {
        Field idField = null;
        idField = getIdField(table);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table.getName().replaceAll("\\.", "_"),
                null,
                (idField == null ? "id" : idField.getName()) + " = ?",
                new String[]{id.toString()}, null, null, null);
        List<T> list = initList(table, cursor);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }

    }

    @Nullable
    private <T> Field getIdField(Class<T> table) {
        Field idField = null;
        try {
            idField = table.getDeclaredField("id");
        } catch (NoSuchFieldException ignored) {
        }
        if (idField == null) {
            try {
                idField = table.getDeclaredField("_id");
            } catch (NoSuchFieldException ignored) {
            }
        }
        return idField;
    }

    @NonNull
    private <T> List<T> initList(Class<T> table, Cursor cursor) {
        List<T> list = new ArrayList<>();
        UnsafeAllocator allocator = UnsafeAllocator.create();
        while (cursor.moveToNext()) {
            try {
                T t = allocator.newInstance(table);
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Field field = table.getDeclaredField(cursor.getColumnName(i));
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    String value = cursor.getString(i);
                    if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
                        field.set(t, Byte.parseByte(value));
                    } else if (type.equals(Short.class) || type.equals(Short.TYPE)){
                        field.set(t, Short.parseShort(value));
                    } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)){
                        field.set(t, Integer.parseInt(value));
                    } else if (type.equals(Long.class) || type.equals(Long.TYPE)){
                        field.set(t, Long.parseLong(value));
                    } else if (type.equals(Float.class) || type.equals(Float.TYPE)){
                        field.set(t, Float.parseFloat(value));
                    } else if (type.equals(Double.class) || type.equals(Double.TYPE)){
                        field.set(t, Double.parseDouble(value));
                    } else if (type.equals(Character.class) || type.equals(Character.TYPE)){
                        field.set(t, value.charAt(0));
                    } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)){
                        field.set(t, Boolean.parseBoolean(value));
                    } else if (type.equals(String.class)){
                        field.set(t, value);
                    }
                }
                list.add(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private boolean isTableExists(Class table){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("sqlite_master", null, "type = 'table' AND name = ?", new String[]{table.getName().replaceAll("\\.", "_")}, null, null, null);
        boolean flag = cursor.getCount() > 0;
        cursor.close();
        return flag;
    }
    private void createTableIfNotExists(Class table){
        if (!isTableExists(table)) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS ");
            builder.append(table.getName().replaceAll("\\.", "_"));
            builder.append(" (");
            Field id = null;
            id = getIdField(table);
            if (id == null) {
                builder.append("id PRIMARY KEY AUTOINCREMENT,");
            } else {
                builder.append(id.getName()).append(" PRIMARY KEY,");
            }
            Field[] fields = table.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (!field.equals(id) && !Modifier.isStatic(modifiers)) {
                    builder.append(field.getName()).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(");");
            getWritableDatabase().execSQL(builder.toString());
        }
    }
    public void clear(Class table) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(table.getName().replaceAll("\\.","_"), null, null);
    }
    public void delete(Object o) {
        SQLiteDatabase db = getWritableDatabase();
        delete(o, db);
    }

    private void delete(Object o, SQLiteDatabase db) {
        Field idField = getIdField(o.getClass());
        if (idField != null) {
            try {
                db.delete(o.getClass().getName().replaceAll("\\.", "_"),
                        idField.getName() + " = ?",
                        new String[]{idField.get(o).toString()});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteAll(Collection collection) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (Object o : collection) {
            delete(o, db);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
