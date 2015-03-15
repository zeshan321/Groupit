package com.groupit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity  extends ActionBarActivity {

    public static ListView groupsList;
    public static GroupArrayAdapter myAdapter;
    public static List<String> groups = new ArrayList<String>();
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    Context con;
    ClientMessage cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_group);
        con = this;

        setup();

        groupsList = (ListView) findViewById(R.id.list_group);
        myAdapter = new GroupArrayAdapter(getApplicationContext(), R.layout.groups_layout);

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

                TextView textView = (TextView) arg1.findViewById(R.id.secondLine);
                TextView textView1 = (TextView) arg1.findViewById(R.id.firstLine);

                String group = textView.getText().toString().replace("Code: ", "");
                String name = textView1.getText().toString();

                MessageActivity.groupName = name;
                Intent intent = new Intent(GroupActivity.this, MessageActivity.class);
                startActivity(intent);
                MessageActivity.currentGroup = group;

            }
        });

        Button b = (Button) findViewById(R.id.Create);
        b.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View v = inflater.inflate(R.layout.dialog_creategroup, null);

                builder.setView(v)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText e1 = (EditText) v.findViewById(R.id.CreateName);
                                EditText e2 = (EditText) v.findViewById(R.id.CreateCode);

                                String es1 = e1.getText().toString();
                                String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {
                                    addGroup(es1, es2);
                                    addMessage(es1, "Code: " + es2);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.create();
                builder.show();
            }
        });

        Button b1 = (Button) findViewById(R.id.Join);
        b1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View v = inflater.inflate(R.layout.dialog_joingroup, null);

                builder.setView(v)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText e1 = (EditText) v.findViewById(R.id.JoinName);
                                EditText e2 = (EditText) v.findViewById(R.id.JoinCode);

                                String es1 = e1.getText().toString();
                                String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {
                                    addGroup(es1, es2);
                                    addMessage(es1, "Code: " + es2);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.create();
                builder.show();
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
        if (ClientMessage.cm == null) {
            ClientMessage.cm.sendData(JSONUtils.getJSONList());
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
                addMessage(display, "Code: " + id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean addMessage(String text, String name) {
        myAdapter.add(new GroupMessage(text, name));

        groupsList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        groupsList.setAdapter(myAdapter);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            finish();
            return;
        }
        else {
            Toast.makeText(getBaseContext(), "Press again to quit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }
}
