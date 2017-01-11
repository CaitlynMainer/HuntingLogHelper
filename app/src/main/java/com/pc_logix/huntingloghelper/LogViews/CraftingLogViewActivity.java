package com.pc_logix.huntingloghelper.LogViews;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import com.pc_logix.huntingloghelper.util.DownloadImageTask;
import com.pc_logix.huntingloghelper.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CraftingLogViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static String myClass;
    private ArrayList<String> results = new ArrayList<String>();
    private ArrayList<Integer> done = new ArrayList<Integer>();
    private ArrayList<String> ranks = new ArrayList<String>();
    private ArrayList<String> icons = new ArrayList<String>();
    private LinkedHashMap<Integer, Integer> ids = new LinkedHashMap<Integer, Integer>();
    private String selectedRank;
    protected static String tableName = DBHelper.craftingLogsTable;
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
        openAndQueryDatabase("1-10");
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.crafting_log_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            menu.findItem(R.id.action_Alchemist).setTitle(Helper.getCompletionAmount("Alchemist",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Armorer).setTitle(Helper.getCompletionAmount("Armorer",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Blacksmith).setTitle(Helper.getCompletionAmount("Blacksmith",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Carpenter).setTitle(Helper.getCompletionAmount("Carpenter",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Culinarian).setTitle(Helper.getCompletionAmount("Culinarian",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Goldsmith).setTitle(Helper.getCompletionAmount("Goldsmith",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Leatherworker).setTitle(Helper.getCompletionAmount("Leatherworker",this.getApplicationContext(),tableName));
            menu.findItem(R.id.action_Weaver).setTitle(Helper.getCompletionAmount("Weaver",this.getApplicationContext(),tableName));
        }
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
            if (id == R.id.action_Alchemist) {
                CraftingLogViewActivity.myClass = "Alchemist";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Armorer) {
                CraftingLogViewActivity.myClass = "Armorer";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Blacksmith) {
                CraftingLogViewActivity.myClass = "Blacksmith";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Carpenter) {
                CraftingLogViewActivity.myClass = "Carpenter";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Culinarian) {
                CraftingLogViewActivity.myClass = "Culinarian";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Goldsmith) {
                CraftingLogViewActivity.myClass = "Goldsmith";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Leatherworker) {
                CraftingLogViewActivity.myClass = "Leatherworker";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_Weaver) {
                CraftingLogViewActivity.myClass = "Weaver";
                Intent myIntent = new Intent(this, CraftingLogViewActivity.class);
                startActivity(myIntent);
            } else if (id == R.id.action_settings) {
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
                            tableName +
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
                    LayoutInflater lInflater = LayoutInflater.from(CraftingLogViewActivity.this);
                    convertView = lInflater.inflate(R.layout.list_item, null);
                }

                ImageView logImage = (ImageView) convertView.findViewById(R.id.logIconView);
                if (icons.get(position).length() > 1) {
                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + File.separator + "ffxiv-item-icons" + File.separator + icons.get(position));
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getApplicationContext().getResources(), bitmap);
                    logImage.setBackground(bitmapDrawable);
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

    private static String removeTrailingPipe(String str) {
        if (str.substring(0,str.length()-1).equals("|")) {
            return str.substring(0,str.length()-1);
        }
        return str;
    }
    private void openAndQueryDatabase(String levelIn) {
        int loop = 0;
        results.clear();
        done.clear();
        ids.clear();
        icons.clear();


        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setAdapter(null);
        ranks.clear();
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ranks));
        try {
            DBHelper dbHelper = new DBHelper(this.getApplicationContext());
            newDB = dbHelper.getWritableDatabase();
            Cursor c;
            if (levelIn.toLowerCase().equals("all")) {
                //Log.e("Hunting Log", "SELECT * FROM " + tableName +
                //        " where class='" + myClass + "' ORDER BY _id");
                c = newDB.rawQuery("SELECT * FROM " + tableName +
                        " where class='" + myClass + "' ORDER BY _id", null);
            } else if (levelIn.toLowerCase().contains("★")) {
                //Log.e("Hunting Log", "SELECT * FROM " + tableName +
                //        " where class='" + myClass + "' AND level = '" + levelIn + "' ORDER BY _id");
                c = newDB.rawQuery("SELECT * FROM " + tableName +
                        " where class='" + myClass + "' AND level = '" +levelIn + "' ORDER BY _id", null);
            } else {
                //Log.e("Hunting Log", "SELECT * FROM " + tableName +
                //        " where class='" + myClass + "' AND level BETWEEN " + levelIn.replace("-", " AND ") + " ORDER BY _id");
                c = newDB.rawQuery("SELECT * FROM " + tableName +
                        " where class='" + myClass + "' AND level BETWEEN " + levelIn.replace("-", " AND ") + " ORDER BY _id", null);
            }
            ranks.add("1-10");
            ranks.add("11-20");
            ranks.add("21-30");
            ranks.add("31-40");
            ranks.add("41-50");
            ranks.add("51-60");
            ranks.add("50★");
            ranks.add("50★★");
            ranks.add("50★★★");
            ranks.add("50★★★★");
            ranks.add("60★");
            ranks.add("60★★");
            ranks.add("60★★★");
            ranks.add("60★★★★");
            if (c != null ) {
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex("_id"));
                        String level = c.getString(c.getColumnIndex("level"));
                        String item_name = c.getString(c.getColumnIndex("name"));
                        String ingredients = c.getString(c.getColumnIndex("ingredients"));
                        String requires = c.getString(c.getColumnIndex("requires"));
                        String requiresOut = "";
                        String icon = c.getString(c.getColumnIndex("icon"));
                        if (icon != null && icon.length() > 1){
                            icons.add(icon);
                        } else {
                            icons.add("");
                        }
                        if (requires != null && requires.length() > 0) {
                            requiresOut = "Requires: " + requires + "<br>";
                        }
                        ingredients = ingredients.replace("[", "");
                        ingredients = ingredients.replace("]", "");
                        ingredients = removeTrailingPipe(ingredients);
                        ingredients = ingredients.replace("|", "<br>");
                        ingredients = ingredients.replace(",", "x - ");
                        int isDone = c.getInt(c.getColumnIndex("done"));
                        ids.put(loop, id);
                        results.add(
                                "level: " + level + "<br>" +
                                "Item: " + "<b>"+item_name + "</b><br>" +
                                requiresOut +
                                "Ingredients:<br>" + ingredients
                        );
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
                    CraftingLogViewActivity.tableName +
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
                    CraftingLogViewActivity.tableName +
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
