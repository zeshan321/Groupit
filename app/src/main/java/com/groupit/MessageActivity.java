package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageActivity extends ActionBarActivity {

    public static String SocketAddress = "104.236.60.137";
    public static int SocketServerPORT = 47687;
    public static ListView chatMsg;
    public static ArrayAdapter myAdapter;
    public static Boolean allowReConnect = false;
    public static String display;
    public static ClientMessage cm = null;
    public static Context con;
    public static String currentGroup = "0";
    public static boolean isLooking = false;
    public static boolean finishedSetup = false;
    public static String groupName = null;

    public BroadcastReceiver networkStateReceiver = null;

    EditText editTextSay;
    ImageButton buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_display);

        setTitle(groupName);

        isLooking = true;
        con = this;
        myAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.list_item_message_left);

        chatMsg = (ListView) findViewById(R.id.list_view_messages);

        MessageHandler mh = new MessageHandler(currentGroup, null, con);
        mh.loadMessages();

        editTextSay = (EditText)findViewById(R.id.say);
        buttonSend = (ImageButton)findViewById(R.id.send);

        ImageButton b = (ImageButton)findViewById(R.id.send);
        b.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                final String msg = editTextSay.getText().toString();
                String json = null;
                if (msg.equals("")) {
                    return;
                }

                try {
                    json = JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display);
                    MessageActivity.addMessage(true, JSONUtils.getMessage(json), JSONUtils.getName(json), currentGroup);
                    cm.sendData(JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display));
                    editTextSay.setText("");
                } catch (NullPointerException e) {
                    ClientMessage.closeSocket();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cm = new ClientMessage(con);
                                cm.sendData(JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display));
                                editTextSay.setText("");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });

        networkStateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (allowReConnect == false) {
                                    allowReConnect = true;
                                    ClientMessage.closeSocket();
                                    cm = new ClientMessage(con);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
        };

        try {
            con.unregisterReceiver(networkStateReceiver);
        } catch (IllegalArgumentException e) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateReceiver, filter);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        isLooking = true;
        if (ClientMessage.socket == null || ClientMessage.socket.isConnected() == false) {
            ClientMessage.closeSocket();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cm = new ClientMessage(con);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    protected void onPause() {
        isLooking = false;

        super.onPause();
    }

    public static void sendToast(String message, Context con) {
        Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public static boolean addMessage(boolean right, String text, String name, String group) {
        try {
            myAdapter.add(new ChatMessage(right, text, name));

            chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            chatMsg.setAdapter(myAdapter);
        } catch (NullPointerException e) {
            MessageHandler mh = new MessageHandler(group, null, ClientMessage.con);
            mh.saveMessage();
        }
        return true;
    }

    public void showSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_locker, null);

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                }
        });
        builder.create();
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isLooking = false;
                Intent intent = new Intent(this, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);

                if (networkStateReceiver != null) {
                    unregisterReceiver(networkStateReceiver);
                }
                return true;
            case 0:
                showSettings();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (GroupActivity.owns.contains(currentGroup)) {
            menu.add(0, 0, 0, "Settings");
        }

        return super.onCreateOptionsMenu(menu);
    }
}