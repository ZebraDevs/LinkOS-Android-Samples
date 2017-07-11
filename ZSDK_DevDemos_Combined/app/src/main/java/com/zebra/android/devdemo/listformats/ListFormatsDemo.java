/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.android.devdemo.listformats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zebra.android.devdemo.ConnectionScreen;
import com.zebra.android.devdemo.R;

public class ListFormatsDemo extends ConnectionScreen {
    private boolean retrieveFormats = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testButton.setText("Retrieve Formats");
        testButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                retrieveFormats = true;
                performTest();
            }
        });

        Button b = new Button(this);
        b.setText("Retrieve Files");
        b.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                retrieveFormats = false;
                performTest();
            }
        });

        LinearLayout connection_screen_layout = (LinearLayout) findViewById(R.id.connection_screen_layout);
        connection_screen_layout.addView(b, connection_screen_layout.indexOfChild(testButton) + 1);
    }

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
