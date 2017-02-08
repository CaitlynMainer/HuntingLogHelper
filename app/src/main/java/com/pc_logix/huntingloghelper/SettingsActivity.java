package com.pc_logix.huntingloghelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pc_logix.huntingloghelper.util.DBHelper;
import com.pc_logix.huntingloghelper.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.pc_logix.huntingloghelper.MainActivity.dbHelper;


/**
 * Created by Michi on 12/30/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    ProgressDialog mDownloadProgressDialog;
    ProgressDialog mUnzipProgressDialog;
    ProgressDialog deleteProgressDialog;
    protected static SQLiteDatabase newDB;
    String strVersion;
    String strVersionCode;
    private Switch showAds;
    public static final String PREFS_NAME = "HuntingHelperPrefs";
    public Map<String, String> huntingLogDone = new HashMap<String, String>();
    public Map<String, String> craftingLogDone = new HashMap<String, String>();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            strVersion = "Version: " + info.versionName;
            strVersionCode = " Build: "+ info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setContentView(R.layout.settings_activity);
        Button btn = (Button) findViewById(R.id.button);
        Button res = (Button) findViewById(R.id.restore);
        Button bak = (Button) findViewById(R.id.backup);
        Button down = (Button) findViewById(R.id.downloadIcons);
        Button delete = (Button) findViewById(R.id.deleteIcons);
        Button copyDB = (Button) findViewById(R.id.copyDB);
        TextView versionText = (TextView) findViewById(R.id.versionText);
        versionText.setText(strVersion + strVersionCode + " DB Version " + DBHelper.version + " Legacy tables: " + Helper.doesTableExist("logs", true, this.getApplicationContext()));
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                resetProgress();
            }
        });
        bak.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                backupProgress();
            }
        });
        res.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                restoreProgress();
            }
        });
        down.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                downloadIcons();
            }
        });
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                deleteIcons();
            }
        });
        copyDB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                copyDB();
            }
        });
        setupActionBar();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        showAds = (Switch) findViewById(R.id.showAds);
        //attach a listener to check for changes in state
        showAds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("showAds", isChecked);
                editor.apply();
            }
        });
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean showAdsBool = settings.getBoolean("showAds", true);
        showAds.setChecked(showAdsBool);
        DBHelper dbHelper = new DBHelper(getApplicationContext());

        mDownloadProgressDialog = new ProgressDialog(SettingsActivity.this);
        mDownloadProgressDialog.setMessage("Downloading Icon Pack");
        mDownloadProgressDialog.setIndeterminate(true);
        mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgressDialog.setCancelable(true);

        mUnzipProgressDialog = new ProgressDialog(SettingsActivity.this);
        mUnzipProgressDialog.setMessage("Decompressing Icon Pack");
        mUnzipProgressDialog.setIndeterminate(true);
        mUnzipProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUnzipProgressDialog.setCancelable(true);

        deleteProgressDialog = new ProgressDialog(SettingsActivity.this);
        deleteProgressDialog.setMessage("Deleting Icon Pack");
        deleteProgressDialog.setIndeterminate(true);
        deleteProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        deleteProgressDialog.setCancelable(true);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(myIntent, 0);
            return true;
        }
        return false;
    }

    public String loadJSONFromStorage(File file) throws FileNotFoundException {
        String json = null;
        FileInputStream is = new FileInputStream(file);
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void restoreProgress(){
        newDB = dbHelper.getWritableDatabase();
        try {
            File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-loghelper" + File.separator + "FFXIV-HuntingLog.json");
            if (f.exists()) {
                JSONObject obj1 = new JSONObject(loadJSONFromStorage(f));
                Iterator<String> temp = obj1.keys();
                Cursor cursor = null;
                while (temp.hasNext()) {
                    String key = temp.next();
                    Object value = obj1.get(key);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("done", 1);
                    newDB.update(DBHelper.huntingLogsTable, contentValues, "_id='"+key+"'", null);
                }
                Toast.makeText(SettingsActivity.this, "Hunting Log Backup restored",
                        Toast.LENGTH_LONG).show();
            } else {
                Log.e("Hunting Log", "Error in Reading: " + f.getName());
                Toast.makeText(SettingsActivity.this, "Error reading Hunting Log backup",
                        Toast.LENGTH_LONG).show();
            }

            File f2 = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-loghelper" + File.separator + "FFXIV-CraftingLog.json");
            if (f2.exists()) {
                JSONObject obj2 = new JSONObject(loadJSONFromStorage(f2));
                Iterator<String> temp = obj2.keys();
                Cursor cursor = null;
                while (temp.hasNext()) {
                    String key = temp.next();
                    Object value = obj2.get(key);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("done", 1);
                    newDB.update(DBHelper.craftingLogsTable, contentValues, "_id='"+key+"'", null);
                }
                Toast.makeText(SettingsActivity.this, "Crafting Log Backup restored",
                        Toast.LENGTH_LONG).show();
            } else {
                Log.e("Hunting Log", "Error in Reading: " + f2.getName());
                Toast.makeText(SettingsActivity.this, "Error reading Crafting Log backup",
                        Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("Hunting Log", "Error in Reading: " + e.getLocalizedMessage());
            Toast.makeText(SettingsActivity.this, "Error reading backup IOException",
                    Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Log.e("Hunting Log", "Error in Reading: " + e.getLocalizedMessage());
            Toast.makeText(SettingsActivity.this, "Error reading backup JSONException",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void copyDB(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//com.pc_logix.huntingloghelper//databases//huntinglogs.db";
                String backupDBPath = "huntinglogs.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }

    public void backupProgress(){
        newDB = dbHelper.getWritableDatabase();
        Cursor c1 = newDB.rawQuery("SELECT * FROM " + DBHelper.huntingLogsTable + " where done='1'", null);
        Cursor c2 = newDB.rawQuery("SELECT * FROM " + DBHelper.craftingLogsTable + " where done='1'", null);
        if (c1 != null) {
            if (c1.moveToFirst()) {
                do {
                    huntingLogDone.put(c1.getString(c1.getColumnIndex("_id")),"1");
                } while (c1.moveToNext());
            }
        }
        if (c2 != null) {
            if (c2.moveToFirst()) {
                do {
                    craftingLogDone.put(c2.getString(c2.getColumnIndex("_id")),"1");
                } while (c2.moveToNext());
            }
        }
        File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-loghelper");
        if(!f.isDirectory()) {
            f.mkdirs();
        }
        try {
            if (!huntingLogDone.isEmpty()) {
                JSONObject jsonObj1 = new JSONObject(huntingLogDone);
                FileWriter file1 = new FileWriter(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-loghelper" + File.separator + "FFXIV-HuntingLog.json");
                file1.write(jsonObj1.toString());
                file1.flush();
                file1.close();
                Toast.makeText(SettingsActivity.this, "Hunting Log Backup created",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Hunting log progress is 0% skipping backup",
                        Toast.LENGTH_LONG).show();
            }

            if (!craftingLogDone.isEmpty()) {
                JSONObject jsonObj2 = new JSONObject(craftingLogDone);
                FileWriter file2 = new FileWriter(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-loghelper" + File.separator + "FFXIV-CraftingLog.json");
                file2.write(jsonObj2.toString());
                file2.flush();
                file2.close();
                Toast.makeText(SettingsActivity.this, "Crafting Log Backup created",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Crafting log progress is 0% skipping backup",
                        Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            Log.e("Hunting Log", "Error in Writing: " + e.getLocalizedMessage());
            Toast.makeText(SettingsActivity.this, "Unable to write backup",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void resetProgress() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                newDB = dbHelper.getWritableDatabase();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        Toast.makeText(SettingsActivity.this, "Resetting",
                                Toast.LENGTH_LONG).show();

                        newDB.beginTransaction();
                        Cursor cursor = newDB.rawQuery("UPDATE " +
                                DBHelper.huntingLogsTable +
                                " SET done = 0", null);
                        Cursor cursor2 = newDB.rawQuery("UPDATE " +
                                DBHelper.craftingLogsTable +
                                " SET done = 0", null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                Toast.makeText(SettingsActivity.this, "Hunting Logs reset done",
                                        Toast.LENGTH_LONG).show();
                            }
                            cursor.close();
                        }
                        if (cursor2 != null) {
                            if (cursor2.moveToFirst()) {
                                Toast.makeText(SettingsActivity.this, "Crafting Logs reset done",
                                        Toast.LENGTH_LONG).show();
                            }
                            cursor2.close();
                        }
                        newDB.setTransactionSuccessful();
                        newDB.endTransaction();
                        newDB.close();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        Toast.makeText(SettingsActivity.this, "Canceled",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteIcons() {
        final deleteIconsTask deleteTask = new deleteIconsTask(SettingsActivity.this);
        deleteTask.execute();
        deleteProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });

        mUnzipProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //unzipTask.cancel(true);
            }
        });
    }

    private void downloadIcons() {
        final DownloadTask downloadTask = new DownloadTask(SettingsActivity.this);
        downloadTask.execute("https://pc-logix.com/ffxiv/ffxiv-icons.zip");
        mDownloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });

        mUnzipProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //unzipTask.cancel(true);
            }
        });
    }

    private class deleteIconsTask extends AsyncTask<Void, Integer, Integer> {
        private int per = 0;
        private Context context;
        File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons");
        String[] entries = f.list();

        public deleteIconsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            deleteProgressDialog.show();
            deleteProgressDialog.setMax(f.list().length);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            deleteProgressDialog.setIndeterminate(false);

            deleteProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            for(String s: entries){
                File currentFile = new File(f.getPath(),s);
                currentFile.delete();
                per++;
                publishProgress(per);
            }
            f.delete();
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            deleteProgressDialog.dismiss();
            if (result != 1)
                Toast.makeText(context,"Decompression error", Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "Files Deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDownloadProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mDownloadProgressDialog.setIndeterminate(false);
            mDownloadProgressDialog.setMax(100);
            mDownloadProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mDownloadProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                final Decompress unzipTask = new Decompress(SettingsActivity.this, Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons.zip", Environment.getExternalStorageDirectory().toString() + File.separator);
                unzipTask.execute();
            }
        }


        /* Checks if external storage is available for read and write */
        public boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        /* Checks if external storage is available to at least read */
        public boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
            return false;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                if (isExternalStorageWritable())
                    output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + File.separator + sUrl[0].substring(sUrl[0].lastIndexOf('/') + 1));
                else
                    output = new FileOutputStream(context.getFilesDir() + File.separator + sUrl[0].substring(sUrl[0].lastIndexOf('/') + 1));

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

    private class Decompress extends AsyncTask<Void, Integer, Integer> {

        private Context context;

        private String _zipFile;
        private String _location;
        private int per = 0;
        private ZipFile zip;
        private ZipEntry ze = null;

        public Decompress(Context context, String zipFile, String location) {
            this.context = context;
            _zipFile = zipFile;
            _location = location;
            _dirChecker("ffxiv-icons");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUnzipProgressDialog.show();
        }

        private void _dirChecker(String dir) {
            File f = new File(_location + dir);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            InputStream is;
            ZipInputStream zis;
            try {
                String filename;
                is = new FileInputStream(_zipFile);
                zip = new ZipFile(_zipFile);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;

                while ((ze = zis.getNextEntry()) != null) {
                    filename = ze.getName();
                    if (ze.isDirectory()) {
                        File fmd = new File(_location + filename);
                        fmd.mkdirs();
                        continue;
                    }
                    per++;
                    publishProgress(per);
                    FileOutputStream fout = new FileOutputStream(_location + filename);
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                    zis.closeEntry();
                }
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mUnzipProgressDialog.setMax(zip.size());
            mUnzipProgressDialog.setIndeterminate(false);
            mUnzipProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            mUnzipProgressDialog.dismiss();
            if (result != 1)
                Toast.makeText(context,"Decompression error", Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File Decompressed", Toast.LENGTH_SHORT).show();
            }
            File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons.zip");
            f.delete();
        }
    }
}
