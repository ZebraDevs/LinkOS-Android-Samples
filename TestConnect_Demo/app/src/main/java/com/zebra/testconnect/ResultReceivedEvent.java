/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.os.Bundle;

public class ResultReceivedEvent {

    private Bundle resultData;

    public ResultReceivedEvent(Bundle resultData) {
        this.resultData = resultData;
    }

    public Bundle getResultData() {
        return resultData;
    }

    public void setResultData(Bundle resultData) {
        this.resultData = resultData;
    }
}
