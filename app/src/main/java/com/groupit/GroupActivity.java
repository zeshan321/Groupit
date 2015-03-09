package com.groupit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity  extends ActionBarActivity {

    public static ListView groupsList;
    public static ArrayAdapter<String> myAdapter;
    public static List<String> groups = new ArrayList<String>();
    Context con;
    ClientMessage cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_group);
        con = this;

        setup();

        groupsList = (ListView) findViewById(R.id.list_group);
        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        loadGroups();

        if (MessageActivity.finishedSetup == false) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cm = new ClientMessage(con);
                        MessageActivity.cm = cm;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }

        groupsList.setClickable(true);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Object o = groupsList.getItemAtPosition(position);

                String[] s = o.toString().split(": ");
                int group = Integer.parseInt(s[1]);

                Intent intent = new Intent(GroupActivity.this, MessageActivity.class);
                startActivity(intent);
                MessageActivity.currentGroup = group;

            }
        });
    }

    public void setup() {
        File file = new File(this.getFilesDir(), "groups");

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            addGroup("GroupIt", "0");
        }
    }

    public void addGroup(String display, String group) {
        File file = new File(this.getFilesDir(), "groups");

        BufferedWriter stream = null;
        try {
            stream = new BufferedWriter(new FileWriter(file, true));
            stream.write(JSONUtils.getJSOnGroup(display, group) + "\n");
            stream.close();
            groups.add(group);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGroups() {
        File file = new File(this.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String display = JSONUtils.getGroupDisplay(line);
                String id = JSONUtils.getGroupID(line);

                groups.add(id);
                myAdapter.add(display + " : " + id);
            }

            groupsList.setAdapter(myAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}