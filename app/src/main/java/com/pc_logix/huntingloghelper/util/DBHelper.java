package com.pc_logix.huntingloghelper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Michi on 12/27/2016.
 */

public class DBHelper extends SQLiteAssetHelper {

    public SQLiteDatabase DB;
    public String DBPath;
    public static String DBName = "huntinglogs.db";
    public static final int version = 3;
    public static Context currentContext;
    public static String huntingLogsTable = "hunting_logs";
    public static String craftingLogsTable = "crafting_logs";

    public DBHelper(Context context) {
        super(context, DBName, null, version);
    }
}