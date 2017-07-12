/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect.event;

import android.os.Bundle;

public class StatusIntentCompleteEvent {

    private final int resultCode;
    private final Bundle resultData;

    public StatusIntentCompleteEvent(int resultCode, Bundle resultData) {
        this.resultCode = resultCode;
        this.resultData = resultData;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Bundle getResultData() {
        return resultData;
    }
}
