/**********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2010 - 2022
 *
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.setgetdodemo;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends ConnectionScreen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testButton.setText("Connect");
    }

    @Override
    public void performTest() {
        Intent intent;
        intent = new Intent(this, SetGetDoDemoScreen.class);
        intent.putExtra("bluetooth selected", isBluetoothSelected());
        intent.putExtra("mac address", getMacAddressFieldText());
        intent.putExtra("tcp address", getTcpAddress());
        intent.putExtra("tcp port", getTcpPortNumber());
        startActivity(intent);
    }


}
