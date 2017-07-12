/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.util.Log;

import com.zebra.testconnect.event.StatusIntentCompleteEvent;

import de.greenrobot.event.EventBus;

public class PrinterStatusHelper {

    private static final String LOG_TAG = PrinterStatusHelper.class.getName();
    public static final String RECEIVER_KEY = "com.zebra.printconnect.PrintService.RESULT_RECEIVER";

    public static ResultReceiver buildIPCSafeReceiver(ResultReceiver actualReceiver) {
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    private static void updatePrintConfirmationDialogDisplayedPrinter(Activity activity) {
        AvailableDemosFragment availableDemosFragment = (AvailableDemosFragment) findFragmentByTag(activity, "available_demos_fragment");

        if (availableDemosFragment != null) {
            PrintConfirmationDialog printConfirmationDialog = (PrintConfirmationDialog) activity.getFragmentManager().findFragmentByTag(PrintConfirmationDialog.FRAGMENT_TAG);

            if (printConfirmationDialog != null) {
                printConfirmationDialog.updateDisplayedPrinter(ConnectedPrinterFragment.getStatusMap());
            }
        }
    }

    protected static void updatePrinterSelectedFragment(Activity activity) {
        Intent printerStatusIntent = new Intent();
        printerStatusIntent.setComponent(new ComponentName("com.zebra.printconnect", "com.zebra.printconnect.print.GetPrinterStatusService"));
        printerStatusIntent.putExtra(RECEIVER_KEY, buildIPCSafeReceiver(new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                Log.i(LOG_TAG, "Result Code: " + resultCode);
                EventBus.getDefault().post(new StatusIntentCompleteEvent(resultCode, resultData));
            }
        }));
        activity.startService(printerStatusIntent);
    }

    public static void showPrinterSelectedFragment(Activity activity, Bundle resultData) {
        Fragment updateFragment = findFragmentByTag(activity, "printer_selected_fragment");
        if (updateFragment == null) {
            ConnectedPrinterFragment.setFragmentArguments(resultData);
            ConnectedPrinterFragment connectedPrinterFragment = new ConnectedPrinterFragment();
            activity.getFragmentManager().beginTransaction()
                    .replace(R.id.printerSelectedContainer, connectedPrinterFragment, "printer_selected_fragment")
                    .commit();
        } else {
            updateConnectedPrinterFragment(activity, ((ConnectedPrinterFragment) updateFragment), resultData);
        }

        updatePrintConfirmationDialogDisplayedPrinter(activity);
    }

    public static void showNoPrinterSelectedFragment(Activity activity) {
        removeFragment(activity, "printer_selected_fragment");
        removeFragment(activity, "no_printer_selected_fragment");

        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        NoPrinterSelectedFragment noPrinterSelectedFragment = new NoPrinterSelectedFragment();
        ft.replace(R.id.printerSelectedContainer, noPrinterSelectedFragment, "no_printer_selected_fragment");
        ft.commit();

        ConnectedPrinterFragment.setFragmentArguments(null);
        updatePrintConfirmationDialogDisplayedPrinter(activity);
    }

    public static void removeFragment(Activity activity, String fragmentTag) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment fragment = fm.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            ft.remove(fragment);
            ft.commit();
        }
    }

    public static Fragment findFragmentByTag(Activity activity, String tagName) {
        FragmentManager fm = activity.getFragmentManager();
        if (fm != null) {
            return fm.findFragmentByTag(tagName);
        }
        return null;
    }

    public static void updateConnectedPrinterFragment(Activity activity, ConnectedPrinterFragment fragment, Bundle resultData) {
        ConnectedPrinterFragment.setFragmentArguments(resultData);
        fragment.updateStatusInfo();
        fragment.stopRefreshIcon();

        updatePrintConfirmationDialogDisplayedPrinter(activity);
    }
}
