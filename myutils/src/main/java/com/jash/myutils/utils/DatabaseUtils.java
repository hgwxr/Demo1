package com.jash.myutils.utils;

import android.content.Context;

public class DatabaseUtils {
    private static MyOpenHelper helper;

    public static void initHelper(Context context, String name) {
        helper = new MyOpenHelper(context, name);
    }

    public static MyOpenHelper getHelper() {
        if (helper == null) {
            throw new RuntimeException("MyOpenHelper未初始化");
        }
        return helper;
    }
}
