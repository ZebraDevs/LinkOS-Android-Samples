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

package com.zebra.android.devdemo.discovery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zebra.android.devdemo.R;

public class DirectedBroadcastParameters extends Activity {

    public static final String DIRECTED_BROADCAST_IP = "DIRECTED_BROADCAST_IP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direct_broadcast_discovery_parameters);

        Button discoverButton = (Button) this.findViewById(R.id.do_directed_broadcast);

        discoverButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(DirectedBroadcastParameters.this, DirectedBroadcastResultList.class);
                Bundle directedBroadcastBundle = new Bundle();
                EditText directedBroadcastIp = (EditText) findViewById(R.id.directed_broadcast_ip);
                String ip = directedBroadcastIp.getText().toString();
                directedBroadcastBundle.putString(DIRECTED_BROADCAST_IP, ip);
                intent.putExtras(directedBroadcastBundle);
                startActivity(intent);
            }
        });

    }

}
