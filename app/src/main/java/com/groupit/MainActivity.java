package com.groupit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstButton(findViewById(R.id.button));
    }

    public void firstButton(View v) {

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.editText);

                if (text.getText().toString().length() < 5) {
                    Toast.makeText(MainActivity.this, "Display names need to be longer than 5 characters.", Toast.LENGTH_LONG).show();
                    return;
                }

                MessageActivity.display = text.getText().toString();
                startActivity(new Intent(MainActivity.this, MessageActivity.class));
            }
        });
    }
}