package com.groupit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DisplayActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        firstButton(findViewById(R.id.button));
        onEdit(findViewById(R.id.editText));
    }

    public void firstButton(View v) {

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText)DisplayActivity.this.findViewById(R.id.editText);

                String s = input.getText().toString();

                DNManager fm = new DNManager("Settings.yml");

                fm.setName(s);
                startActivity(new Intent(DisplayActivity.this, MainActivity.class));
            }
        });
    }

    public void onEdit(View v) {
       final EditText text = (EditText) findViewById(R.id.editText);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");
            }
        });
    }
}
