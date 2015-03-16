package com.groupit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

public class MainActivity extends ActionBarActivity {
    Context con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        con = this;
        GroupActivity.ID = getID();

        NameHandler nh = new NameHandler(null, con);

        if (nh.getName() != null) {
            MessageActivity.display = nh.getName();
            startActivity(new Intent(MainActivity.this, GroupActivity.class));
            return;
        }

        firstButton(findViewById(R.id.button));

    }

    public void firstButton(View v) {

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.editText);

                if (text.getText().toString().length() < 5 && text.getText().toString().startsWith(" ") == false) {
                    Toast.makeText(MainActivity.this, "Display names need to be longer than 5 characters.", Toast.LENGTH_LONG).show();
                    return;
                }

                MessageActivity.display = text.getText().toString().replaceAll("\\s+$", "");

                NameHandler nh = new NameHandler(text.getText().toString().replaceAll("\\s+$", ""), con);
                nh.saveName();

                startActivity(new Intent(MainActivity.this, GroupActivity.class));

            }
        });
    }

    public String getID(){
        String myAndroidDeviceId = "";
        TelephonyManager mTelephony = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null){
            myAndroidDeviceId = mTelephony.getDeviceId();
        }else{
            myAndroidDeviceId = Settings.Secure.getString(con.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}