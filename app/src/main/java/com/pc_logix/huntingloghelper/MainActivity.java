package com.pc_logix.huntingloghelper;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pc_logix.huntingloghelper.LogViews.CraftingLogViewActivity;
import com.pc_logix.huntingloghelper.LogViews.HuntingLogViewActivity;
import com.pc_logix.huntingloghelper.util.DBHelper;
import com.pc_logix.huntingloghelper.util.Helper;

public class MainActivity extends AppCompatActivity {


    public static String myClass;
    public static DBHelper dbHelper;
    protected static String tableName = DBHelper.huntingLogsTable;
    protected static SQLiteDatabase newDB;
    private AdView mAdView;
    public static final String PREFS_NAME = "HuntingHelperPrefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean showAd = settings.getBoolean("showAds", true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DBHelper(this.getApplicationContext());
        newDB = dbHelper.getWritableDatabase();

        if(Helper.doesTableExist("logs", true, this.getApplicationContext()) && Helper.doesTableExist("hunting_logs", true, this.getApplicationContext())) {
            Log.e("Hunting Log", "Attempting to transfer data");
            Toast.makeText(this.getApplicationContext(),"Attempting to transfer progress", Toast.LENGTH_LONG).show();
            Cursor c = newDB.rawQuery("SELECT _id, done FROM logs", null);
            if (c != null ) {
                if (c.moveToFirst()) {
                    do {
                        int isDone = c.getInt(c.getColumnIndex("done"));
                        int id = c.getInt(c.getColumnIndex("_id"));
                        if (isDone == 1) {
                            ContentValues data=new ContentValues();
                            data.put("done",1);
                            newDB.update("hunting_logs", data, "_id=" + id, null);
                        }
                    }while (c.moveToNext());
                }
            }
            newDB.execSQL("DROP TABLE logs");
        }

        TextView t=(TextView)findViewById(R.id.content);
        t.setText(getResources().getText(R.string.welcome_text));
        if (showAd) {
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        if(Helper.canMakeSmores() && !hasPermission("android.permission.WRITE_EXTERNAL_STORAGE") && !hasPermission("android.permission.READ_EXTERNAL_STORAGE")) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
            int permsRequestCode = 200;
            ActivityCompat.requestPermissions(MainActivity.this, perms, permsRequestCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_HuntingLogs) {
            HuntingLogViewActivity.myClass = "Arcanist";
            Intent myIntent = new Intent(this, HuntingLogViewActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.action_CraftingLogs) {
            CraftingLogViewActivity.myClass = "Alchemist";
            Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    private boolean hasPermission(String permission){
        if(Helper.canMakeSmores()){
            return(ContextCompat.checkSelfPermission(MainActivity.this, permission)== PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean externalReadAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                boolean externalWriteAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                //boolean wakeLockAccepted = grantResults[2]==PackageManager.PERMISSION_GRANTED;
                break;
        }
    }
}
