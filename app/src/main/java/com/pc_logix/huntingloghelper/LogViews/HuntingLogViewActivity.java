package com.pc_logix.huntingloghelper.LogViews;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.pc_logix.huntingloghelper.util.DBHelper;
import com.pc_logix.huntingloghelper.MainActivity;
import com.pc_logix.huntingloghelper.SettingsActivity;
import com.pc_logix.huntingloghelper.R;
import com.pc_logix.huntingloghelper.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class HuntingLogViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static String myClass;
    private ArrayList<String> results = new ArrayList<String>();
    private ArrayList<Integer> done = new ArrayList<Integer>();
    private ArrayList<String> ranks = new ArrayList<String>();
    private ArrayList<String> icons = new ArrayList<String>();
    private LinkedHashMap<Integer, Integer> ids = new LinkedHashMap<Integer, Integer>();
    private String selectedRank;
    protected static String tableName = DBHelper.huntingLogsTable;
    protected static SQLiteDatabase newDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //myClass = MainActivity.myClass;
        this.setTitle(Helper.getCompletionAmount(myClass,this.getApplicationContext(),tableName));
        selectedRank = "all";
        setupActionBar();
        setContentView(R.layout.activity_log_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        openAndQueryDatabase(selectedRank);
        displayResultList();
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
        final String classes[] = {"Arcanist","Archer","Conjurer","Gladiator","Lancer","Marauder","Pugilist","Rogue","Thaumaturge","Immortal Flame","Maelstrom","Order of the Twin Adder"};
        getMenuInflater().inflate(R.menu.hunting_log_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, classes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(Arrays.asList(classes).indexOf(myClass));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            Boolean meh = false;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (meh) {
                    HuntingLogViewActivity.myClass = classes[position];
                    Intent myIntent = new Intent(getApplicationContext(), HuntingLogViewActivity.class);
                    startActivity(myIntent);
                }
                meh = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == 16908332) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivityForResult(myIntent, 0);
            //this.finish();
            return true;
        } else {
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_markAll) {
                ListView listView = (ListView) findViewById(R.id.loglist);
                View v;
                DBHelper dbHelper = new DBHelper(this.getApplicationContext());
                newDB = dbHelper.getWritableDatabase();
                newDB.beginTransaction();
                for (int i = 0; i < listView.getCount(); i++) {
                    v = listView.getChildAt(i);
                    listView.setItemChecked(i, true);
                    Cursor cursor = newDB.rawQuery("UPDATE " +
                            HuntingLogViewActivity.tableName +
                            " SET done = 1 where class='" + myClass + "' AND _id = " + ids.get(i), null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {

                        }
                        cursor.close();
                    }
                }
                newDB.setTransactionSuccessful();
                newDB.endTransaction();
                newDB.close();
                this.setTitle(Helper.getCompletionAmount(myClass,this.getApplicationContext(),tableName));
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private void displayResultList() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ranks));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            Boolean canRun = false;
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long id) {
                if (canRun == true) {
                    selectedRank = arg0.getSelectedItem().toString();
                    openAndQueryDatabase(selectedRank.toLowerCase());
                    displayResultList();
                    arg0.setSelection(position);
                } else {
                    canRun = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        ListView listView = (ListView) findViewById(R.id.loglist);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.checkedTextView1, results) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null) {
                    LayoutInflater lInflater = LayoutInflater.from(HuntingLogViewActivity.this);
                    convertView = lInflater.inflate(R.layout.list_item, null);
                }
                ImageView logImage = (ImageView) convertView.findViewById(R.id.logIconView);
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons" + File.separator + icons.get(position));
                    if (f.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons" + File.separator + icons.get(position));
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getApplicationContext().getResources(), bitmap);
                        logImage.setBackground(bitmapDrawable);
                    } else {
                        Log.e("Hunting Log", "Icon file " + icons.get(position) + " doesn't exist!");
                    }
                }
                CheckedTextView tv = (CheckedTextView) convertView.findViewById(R.id.checkedTextView1);
                tv.setText(Helper.fromHtml(results.get(position)));
                if(position %2 == 1)
                    convertView.setBackgroundColor(Color.parseColor("#FFDCDCDC"));
                else
                    convertView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
                return convertView;
            }
        });
        listView.setTextFilterEnabled(true);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(this);
        // we want multiple clicks
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for (Integer pos : done) {
            listView.setItemChecked(pos, true);
        }
    }

    private void openAndQueryDatabase(String rankIn) {
        int loop = 0;
        results.clear();
        done.clear();
        ids.clear();
        icons.clear();

        ListView listView = (ListView) findViewById(R.id.loglist);
        listView.setAdapter(null);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.checkedTextView1, results));
        listView.setTextFilterEnabled(true);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(this);
        // we want multiple clicks
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setAdapter(null);
        ranks.clear();
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ranks));
        try {
            DBHelper dbHelper = new DBHelper(this.getApplicationContext());
            newDB = dbHelper.getWritableDatabase();
            Cursor c;
            if (rankIn.toLowerCase().equals("all")) {
                c = newDB.rawQuery("SELECT * FROM " +  tableName +
                        " where class='" + myClass + "' ORDER BY rank, region, area, y_loc, x_loc", null);
            } else {
                c = newDB.rawQuery("SELECT * FROM " + tableName +
                        " where class='" + myClass + "' AND rank='" + rankIn + "' ORDER BY rank, region, area, y_loc, x_loc", null);
            }
            Cursor c2 = newDB.rawQuery("SELECT * FROM " + tableName + " where class='" + myClass + "' GROUP BY rank", null);
            ranks.add("All");
            if (c2 != null) {
                if (c2.moveToFirst()) {
                    do {
                        ranks.add(Integer.toString(c2.getInt(c.getColumnIndex("rank"))));
                    }while (c2.moveToNext());
                }
            }
            if (c != null ) {
                if (c.moveToFirst()) {
                    do {
                        String region = c.getString(c.getColumnIndex("region"));
                        String area = c.getString(c.getColumnIndex("area"));
                        String enemy = c.getString(c.getColumnIndex("enemy"));
                        String title = c.getString(c.getColumnIndex("set"));
                        int id = c.getInt(c.getColumnIndex("_id"));
                        int rank = c.getInt(c.getColumnIndex("rank"));
                        int num = c.getInt(c.getColumnIndex("num"));
                        int x_loc = c.getInt(c.getColumnIndex("x_loc"));
                        int y_loc = c.getInt(c.getColumnIndex("y_loc"));
                        int isDone = c.getInt(c.getColumnIndex("done"));
                        String icon = c.getString(c.getColumnIndex("icon"));
                        if (icon != null && icon.length() > 1){
                            Log.e("Hunting Log", "Adding icon for item: " + enemy + " " + icon);
                            File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-icons" + File.separator + icon.replaceAll("\\'","%27"));
                            if (!f.exists()) {
                                Log.e("Hunting Log", "But it doesn't exist");
                            }
                            icons.add(icon);
                        } else {
                            Log.e("Hunting Log", "Missing icon for item: " + enemy);
                            icons.add("");
                        }
                        ids.put(loop, id);
                        results.add(
                                "Rank: " + rank + " Set: " + myClass + " " + title + "<br>" +
                                "Area: " + region + " - " + area + "<br>" +
                                "Enemy: <b>"+enemy+"</b>" + " X" + num + "<br>" +
                                "Location: X:" + x_loc + " Y:" + y_loc);
                        if (isDone == 1)
                            done.add(loop);
                        loop++;
                    }while (c.moveToNext());
                } else {
                    results.add("No data");
                }
            }
        } catch (SQLiteException se ) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {
            newDB.close();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBHelper dbHelper = new DBHelper(this.getApplicationContext());
        newDB = dbHelper.getWritableDatabase();
        CheckedTextView ctv = (CheckedTextView)view.findViewById(R.id.checkedTextView1);
        if(ctv.isChecked()) {
            newDB.beginTransaction();
            Cursor cursor = newDB.rawQuery("UPDATE " +
                    HuntingLogViewActivity.tableName +
                    " SET done = 1 where class='" + myClass + "' AND _id = " + ids.get(position), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                }
                cursor.close();
            }
            newDB.setTransactionSuccessful();
            newDB.endTransaction();
            newDB.close();
        } else {
            newDB.beginTransaction();
            Cursor cursor = newDB.rawQuery("UPDATE " +
                    HuntingLogViewActivity.tableName +
                    " SET done = 0 where class='" + myClass + "' AND _id = " + ids.get(position), null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                }
                cursor.close();
            }
            newDB.setTransactionSuccessful();
            newDB.endTransaction();
            newDB.close();
        }
        this.setTitle(Helper.getCompletionAmount(myClass,this.getApplicationContext(),tableName));
    }
}
