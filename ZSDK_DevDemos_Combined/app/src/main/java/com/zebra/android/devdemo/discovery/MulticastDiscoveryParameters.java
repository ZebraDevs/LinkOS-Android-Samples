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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zebra.android.devdemo.R;

public class MulticastDiscoveryParameters extends Activity {

    public static final String MULTICAST_HOPS = "MULTICAST_HOPS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multicast_discovery_parameters);

        Button discoverButton = (Button) this.findViewById(R.id.do_multicast_discovery);

        discoverButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MulticastDiscoveryParameters.this, MulticastDiscoveryResultList.class);
                Bundle multicastBundle = new Bundle();
                EditText hopsText = (EditText) findViewById(R.id.multicast_hops);
                try {
                    Integer multicastHops = Integer.valueOf(hopsText.getText().toString());
                    if (multicastHops < 0 || multicastHops > 255) {
                        showAlert("Invalid hop count");

                    } else {
                        multicastBundle.putInt(MULTICAST_HOPS, multicastHops);
                        intent.putExtras(multicastBundle);
                        startActivity(intent);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid hop count");
                }
            }
        });

    }

    public void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
