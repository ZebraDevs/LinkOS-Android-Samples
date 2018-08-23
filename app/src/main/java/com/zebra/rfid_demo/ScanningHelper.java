package com.zebra.rfid_demo;

import android.content.Intent;
import android.util.Log;

/**
 * This class provides helper functions to make the scan processing easier.
 *
 * It is extended by NewScanLabelActivity.
 */
public class ScanningHelper extends DatabaseActivity {
    private final String SCANNED_DATA_TAG = "scanned_data_helper";

    /*
    Sends an Intent to DataWedge to start a soft scan
    */
    protected void startSoftScan() {
        Log.d(SCANNED_DATA_TAG,"Sending intent for soft scan");

        Intent i = new Intent();
        i.setAction(softScanTrigger);
        i.putExtra(extraData, "START_SCANNING");
        this.sendBroadcast(i);
    }

    /*
    Sends an Intent to DataWedge to end a soft scan
    */
    protected void endSoftScan() {
        Log.d(SCANNED_DATA_TAG,"Sending intent for to stop soft scan");

        Intent i = new Intent();
        i.setAction(softScanTrigger);
        i.putExtra(extraData, "STOP_SCANNING");
        this.sendBroadcast(i);
    }


}
