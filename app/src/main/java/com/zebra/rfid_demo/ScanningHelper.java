package com.zebra.rfid_demo;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class provides helper functions to make the scan processing easier.
 *
 * It is extended by NewCustomEntryActivity and NewScanEntryActivity.
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

    /**
     * Checks the data String for any occurrences of the suggested delimiter. If there is no
     * suggested delimiter or no occurrences of that delimeter are found in the data String,
     * it checks the data String for occurrences of common delimiters.
     *
     * It then splits the data String on the suggested delimiter or the delimiter that occurs the
     * most and returns an ArrayList of the data string's pieces.
     *
     * @param data      The String that is to be parsed
     * @return          ArrayList of strings
     */
    protected ArrayList<String> attemptToParse(String data) {
        ArrayList<String> fields;
        String[] delimiters = { ";" , "," , "#" , "\t" , "\r" , Character.toString((char) 31) ,
                Character.toString((char) 29) , Character.toString((char) 28) };
        String best_delimiter = "";
        int best_delimiter_occurrences = 0;

        if (!suggestedDelimiter.equals("")) {
            Log.d(SCANNED_DATA_TAG,"Trying to split on suggested delimiter" + suggestedDelimiter);
            String[] other = data.split(suggestedDelimiter);
            fields = new ArrayList<>(Arrays.asList(other));
            if (fields.size() > 0) { return fields; }
            Log.d(SCANNED_DATA_TAG,"Suggested delimiter " + suggestedDelimiter + " failed");
        }

        for (String d : delimiters) {
            int curr_delimiter_occurrences = 0;
            int i = 0;
            while (i != -1) {
                i = data.indexOf(d,i);

                if(i != -1){
                    curr_delimiter_occurrences++;
                    i += d.length();
                }
            }
            if (curr_delimiter_occurrences > best_delimiter_occurrences) {
                best_delimiter = String.valueOf(d);
                best_delimiter_occurrences = curr_delimiter_occurrences;
            };
        }

        if (best_delimiter_occurrences > 1) {
            Log.d(SCANNED_DATA_TAG,"Found potential delimiter for data: " + best_delimiter);
            String[] other = data.split(best_delimiter);
            fields = new ArrayList<>(Arrays.asList(other));
            return fields;
        }
        return null;
    }
}
