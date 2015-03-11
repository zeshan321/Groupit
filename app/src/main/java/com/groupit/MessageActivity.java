package com.groupit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageActivity extends ActionBarActivity {

    public static String SocketAddress = "104.236.60.137";
    public static int SocketServerPORT = 8080;
    public static ListView chatMsg;
    public static ArrayAdapter myAdapter;

    EditText editTextSay;
    ImageButton buttonSend;

    public static String display;
    public static ClientMessage cm = null;
    public static Context con;
    public static int currentGroup = 0;
    public static boolean isLooking = false;
    public static boolean finishedSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_display);

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
                String msg = editTextSay.getText().toString();
                if (msg.equals("")) {
                    return;
                }

                if (ClientMessage.socket == null || ClientMessage.socket.isConnected() == false || ClientMessage.socket.isClosed()) {
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

                cm.sendData(JSONUtils.getJSONMessage(getID(), currentGroup, msg, display));
                editTextSay.setText("");
            }
        });

        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ClientMessage.closeSocket();
                                cm = new ClientMessage(con);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
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

    public static void sendToast(String message) {
        Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public static boolean addMessage(boolean right, String text, String name) {
        myAdapter.add(new ChatMessage(right, text, name));

        chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatMsg.setAdapter(myAdapter);
        return true;
    }

    public static String getID() {
        TelephonyManager telephonyManager = (TelephonyManager)con.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isLooking = false;
                Intent intent = new Intent(this, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}