package com.zebra.listformatsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ConnectionScreen {
    private boolean retrieveFormats = true;
    private Button retrieveFilesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        testButton.setText("Retrieve Formats");
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                retrieveFormats = true;
                performTest();
            }
        });

        retrieveFilesButton = (Button)findViewById(R.id.listsButton);
        retrieveFilesButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                retrieveFormats = false;
                performTest();
            }
        });

        TextView t2 = (TextView) findViewById(R.id.launchpad_link);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     * Implements the abstract method in Connection Screen to make a call.
     */
    @Override
    public void performTest() {
        Intent intent;
        intent = new Intent(this, ListFormatsScreen.class);
        intent.putExtra("bluetooth selected", isBluetoothSelected());
        intent.putExtra("mac address", getMacAddressFieldText());
        intent.putExtra("tcp address", getTcpAddress());
        intent.putExtra("tcp port", getTcpPortNumber());
        intent.putExtra("retrieveFormats", retrieveFormats);
        startActivity(intent);
    }
}


