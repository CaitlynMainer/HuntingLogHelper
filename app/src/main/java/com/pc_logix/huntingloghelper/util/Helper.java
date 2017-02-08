package com.pc_logix.huntingloghelper.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Michi on 1/2/2017.
 */

public class Helper {

    public static boolean canMakeSmores(){
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean doesTableExist(String tableName, boolean openDb, Context contextIn) {
        DBHelper dbHelper = new DBHelper(contextIn);
        SQLiteDatabase newDB = dbHelper.getWritableDatabase();
        if(openDb) {
            if(newDB == null || !newDB.isOpen()) {
                newDB = dbHelper.getWritableDatabase();
            }

            if(!newDB.isReadOnly()) {
                newDB.close();
                newDB = dbHelper.getWritableDatabase();
            }
        }

        Cursor cursor = newDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public static String getCompletionAmount(String classIn, Context contextIn, String tableName) {
        DBHelper dbHelper = new DBHelper(contextIn);
        SQLiteDatabase newDB = dbHelper.getWritableDatabase();
        int total = 0;
        int done = 0;
        Cursor c;
        c = newDB.rawQuery("SELECT * FROM " +  tableName +
                " where class='" + classIn + "'", null);
        if (c != null ) {
            if (c.moveToFirst()) {
                do {
                    total++;
                    if (c.getInt(c.getColumnIndex("done")) == 1) {
                        done++;
                    }
                } while (c.moveToNext());
            }
        }
        if (total > 0) {
            int percent = (done * 100) / total;
            return classIn + " " + Integer.toString(percent) +"%";
        } else {
            return classIn + " " + Integer.toString(0) +"%";
        }
    }

    private static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

    public static boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
