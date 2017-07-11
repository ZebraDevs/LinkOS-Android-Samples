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

import android.os.Bundle;

import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveryException;
import com.zebra.sdk.printer.discovery.NetworkDiscoverer;

public class SubnetSearchResultList extends DiscoveryResultList {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String subnetRange = getIntent().getExtras().getString(SubnetSearchParameters.SUBNET_SEARCH_RANGE);

        try {
            NetworkDiscoverer.subnetSearch(this, subnetRange);
        } catch (DiscoveryException e) {
            new UIHelper(this).showErrorDialog(e.getMessage());
        }
    }
}
