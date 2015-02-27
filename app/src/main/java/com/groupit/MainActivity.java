package com.groupit;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.sql.SQLException;


public class MainActivity extends ActionBarActivity {

    public static File dir = null;
    boolean doneSetup = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            if (MySQL.con == null || MySQL.con.isClosed()) {
                MySQL.startUp();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        MySQL.saveValues("TESTING");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dir = this.getFilesDir();

        DNManager fm = new DNManager("Settings.yml");

        doneSetup = fm.containsName();

        if (doneSetup == false) {
            startActivity(new Intent(MainActivity.this, DisplayActivity.class));
            return;
        }

        TextView et = (TextView) findViewById(R.id.textView3);
        et.setText("Display: " + fm.getName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
