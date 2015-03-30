package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class GroupActivity  extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback {

    public static ListView groupsList;
    public static GroupArrayAdapter myAdapter;
    public static List<String> groups = new ArrayList<String>();
    private static final int TIME_INTERVAL = 2000;
    public static List<String> owns = new ArrayList<String>();
    public static String ID = null;
    public static HashMap<String, String> settings = new HashMap<String, String>();
    public static boolean finishedSetup = false;

    private long mBackPressed;

    Context con;
    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] mIntentFilters;
    String[][] mNFCTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_group);
        con = this;

        new GroupHandler(con).setup();

        groupsList = (ListView) findViewById(R.id.list_group);
        myAdapter = new GroupArrayAdapter(getApplicationContext(), R.layout.groups_layout);

        new GroupHandler(con).loadGroups();


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndefIntent.addDataType("*/*");
                mIntentFilters = new IntentFilter[] { ndefIntent };
            } catch (Exception e) {
                e.printStackTrace();
            }

            mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };
        }

        if (getIntent().hasExtra("transferName") && getIntent().hasExtra("transferCode")) {
            MessageActivity.groupName = getIntent().getExtras().getString("transferName");
            Intent intent = new Intent(GroupActivity.this, MessageActivity.class);
            startActivity(intent);
            overridePendingTransition(R.transition.activity_to_1, R.transition.activity_to_2);

            MessageActivity.currentGroup = getIntent().getExtras().getString("transferCode");
        }

        if (finishedSetup == false) {

            finishedSetup = true;

            // Open socket
            ClientMessage.con = this;
            startService(new Intent(this, ClientMessage.class));

            ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");

            query.whereEqualTo("owner", ID);

            query.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                    if (parseObjects == null) {
                        return;
                    }

                    for (ParseObject groups : parseObjects) {
                        if (groups != null) {
                            String post = groups.getString("groupID");
                            String post1 = groups.getObjectId();
                            boolean post2 = groups.getBoolean("locked");
                            String pass = groups.getString("pass");

                            owns.add(post);
                            settings.put(post, post1 + " , " + post2 + " , " + pass);
                        }
                    }
                    if (MessageActivity.con != null) {
                        ActivityCompat.invalidateOptionsMenu((Activity) MessageActivity.con);
                    }
                }
            });
        }

        groupsList.setClickable(true);
        groupsList.setLongClickable(true);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                TextView textView = (TextView) arg1.findViewById(R.id.chatListItemHints);
                TextView textView1 = (TextView) arg1.findViewById(R.id.chatListItemName);

                String group = textView.getText().toString().replace("Code: ", "");
                String name = textView1.getText().toString();

                MessageActivity.groupName = name;
                Intent intent = new Intent(GroupActivity.this, MessageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.transition.activity_to_1, R.transition.activity_to_2);

                MessageActivity.currentGroup = group;

            }
        });

        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.chatListItemHints);
                TextView textView1 = (TextView) view.findViewById(R.id.chatListItemName);

                final String group = textView.getText().toString().replace("Code: ", "");
                final String name = textView1.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View v = inflater.inflate(R.layout.dialog_delete, null);

                builder.setView(v)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                myAdapter.removeChat(position);
                                groupsList.setAdapter(myAdapter);

                                new GroupHandler(con).removeGroup(new JSONUtils().getJSOnGroup(name, group));

                                groups.remove(group);

                                ClientMessage.sendData(new JSONUtils().getJSONList());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create();
                builder.show();
                return true;
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

                                final String es1 = e1.getText().toString();
                                final String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {

                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");
                                    query.whereEqualTo("groupID", es2);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                            if (e == null) {
                                                if (parseObjects.size() > 0) {
                                                    Toast toast = Toast.makeText(con, "Group already exists!", Toast.LENGTH_LONG);
                                                    toast.show();
                                                } else {
                                                    new GroupHandler(con).addGroup(es1, es2, true);
                                                    addMessage(es1, "Code: " + es2);

                                                    ParseObject addGroup = new ParseObject("groups");
                                                    addGroup.put("groupID", es2);
                                                    addGroup.put("owner", ID);
                                                    addGroup.put("locked", false);
                                                    addGroup.put("pass", "null");
                                                    addGroup.saveInBackground();

                                                    owns.add(es2);
                                                    settings.put(es2, addGroup.getObjectId() + " , " + "false" + " , " + "null");
                                                }
                                            } else {
                                                Toast toast = Toast.makeText(con, "Oops! Something went wrong.", Toast.LENGTH_LONG);
                                                toast.show();
                                            }
                                        }
                                    });
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
                        .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText e1 = (EditText) v.findViewById(R.id.JoinName);
                                EditText e2 = (EditText) v.findViewById(R.id.JoinCode);

                                final String es1 = e1.getText().toString();
                                final String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {

                                    if (new GroupHandler(con).groupCodeExists(es2)) {
                                        Toast toast = Toast.makeText(con, "Group already exists!", Toast.LENGTH_LONG);
                                        toast.show();
                                        return;
                                    }

                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");
                                    query.whereEqualTo("groupID", es2);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                            if (e == null) {
                                                if (parseObjects.size() > 0) {
                                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");

                                                    query.whereEqualTo("groupID", es2);

                                                    query.findInBackground(new FindCallback<ParseObject>() {

                                                        @Override
                                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                                            for (ParseObject groups : parseObjects) {
                                                                if (groups != null) {
                                                                    boolean post = groups.getBoolean("locked");
                                                                    String password = groups.getString("pass");

                                                                    if (post == false) {
                                                                        new GroupHandler(con).addGroup(es1, es2, true);
                                                                        addMessage(es1, "Code: " + es2);
                                                                    } else {
                                                                        hasPassword(password, es1, es2);
                                                                    }
                                                                }
                                                            }
                                                            if (MessageActivity.con != null) {
                                                                ActivityCompat.invalidateOptionsMenu((Activity) MessageActivity.con);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast toast = Toast.makeText(con, "Group not found!", Toast.LENGTH_LONG);
                                                    toast.show();
                                                }
                                            } else {
                                                Toast toast = Toast.makeText(con, "Oops! Something went wrong.", Toast.LENGTH_LONG);
                                                toast.show();
                                            }
                                        }
                                    });
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
            moveTaskToBack(true);
            return;
        }
        else {
            Toast.makeText(getBaseContext(), "Press again to quit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                changeName();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Display name");
        return super.onCreateOptionsMenu(menu);
    }

    public void changeName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View v = inflater.inflate(R.layout.dialog_changename, null);

        EditText et = (EditText) v.findViewById(R.id.ChangeName);
        et.setText(MessageActivity.display);
        builder.setView(v)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText text = (EditText) v.findViewById(R.id.ChangeName);

                        if (text.getText().toString().length() < 5 && text.getText().toString().startsWith(" ") == false) {
                            Toast.makeText(con, "Display names need to be longer than 5 characters.", Toast.LENGTH_LONG).show();
                            changeName();
                            return;
                        }

                        MessageActivity.display = text.getText().toString().replaceAll("\\s+$", "");

                        NameHandler nh = new NameHandler(text.getText().toString().replaceAll("\\s+$", ""), con);
                        nh.saveName();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    public void hasPassword(final String password, final String es1, final String es2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View v = inflater.inflate(R.layout.dialog_password, null);

        final EditText et = (EditText) v.findViewById(R.id.GroupPassCode);

        builder.setView(v)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String text = et.getText().toString().replaceAll("\\s+$", "");

                        if (text.equals(password)) {
                            new GroupHandler(con).addGroup(es1, es2, true);
                            addMessage(es1, "Code: " + es2);
                        } else {
                            Toast.makeText(con, "Incorrect password!", Toast.LENGTH_LONG).show();
                            hasPassword(password, es1, es2);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return null;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0];
            String s = new String(message.getRecords()[0].getPayload());

            if (!new GroupHandler(con).groupCodeExists(new JSONUtils().nfcGetID(s))) {
                new GroupHandler(con).addGroup(new JSONUtils().nfcGetDisplay(s), new JSONUtils().nfcGetID(s), true);
                addMessage(new JSONUtils().nfcGetDisplay(s), "Code: " + new JSONUtils().nfcGetID(s));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);

    }
}