package com.groupit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MessageActivity extends ActionBarActivity {

    public static String SocketAddress = "104.236.60.137";
    public static int SocketServerPORT = 8080;
    public static ListView chatMsg;
    public static ArrayAdapter myAdapter;
    public static ArrayAdapter myAdapter1;

    EditText editTextSay;
    ImageButton buttonSend;
    ScrollView scroll;

    public static String display;
    public static ClientMessage cm = null;
    public static String message;
    public static Context con;
    public static int currentGroup = 0;
    public static List<String> msgLog = new ArrayList<String>();
    public static boolean isLooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        isLooking = true;
        con = this;
        myAdapter = new ArrayAdapter(this, R.layout.list_item_message_left, R.id.txtMsg);
        //chatClientThread = new ChatClientThread(display, "10.23.202.132", SocketServerPORT);
        //chatClientThread.start();

        Thread thread = new Thread(new Runnable(){
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
        chatMsg = (ListView) findViewById(R.id.list_view_messages);

        MessageHandler mh = new MessageHandler(currentGroup, null, con);
        mh.loadMessages();
        scrollDown();

        editTextSay = (EditText)findViewById(R.id.say);
        buttonSend = (ImageButton)findViewById(R.id.send);

        buttonSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                String msg = editTextSay.getText().toString();
                if (msg.equals("")) {
                    return;
                }

                message = display + ": " + msg;
                cm.sendData(JSONUtils.getJSONMessage(display, message));
                editTextSay.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ClientMessage.closeSocket();
    }
    @Override
    protected void onResume() {
        super.onResume();

        isLooking = true;
        ClientMessage.closeSocket();
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    cm = new ClientMessage(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        isLooking = false;

        super.onPause();
    }


    public static void scrollDown() {
        chatMsg.post(new Runnable() {
            @Override
            public void run() {
                chatMsg.setSelection(myAdapter.getCount() - 1);
            }
        });
    }

    public static void sendToast(String message) {
        Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
        toast.show();
    }
}