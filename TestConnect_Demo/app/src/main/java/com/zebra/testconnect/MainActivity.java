/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.zebra.testconnect.event.StatusIntentCompleteEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener, PrintConfirmationDialog.PrintConfirmationListener {

    private final String LOG_TAG = MainActivity.class.getName();
    private boolean isPaused;

    private List<StatusIntentCompleteEvent> statusIntentCompleteEvents = new ArrayList<>();

    public void onEventMainThread(StatusIntentCompleteEvent event) {
        if (isPaused) {
            statusIntentCompleteEvents.add(event);
        } else {
            switch (event.getResultCode()) {
                case 0:
                    PrinterStatusHelper.showPrinterSelectedFragment(this, event.getResultData());
                    break;
                case 1:
                    PrinterStatusHelper.showNoPrinterSelectedFragment(this);
                    break;
                case 2:
                    Log.e(LOG_TAG, "Connection Exception Occurred");
                    ConnectedPrinterFragment fragment = (ConnectedPrinterFragment) PrinterStatusHelper.findFragmentByTag(this, "printer_selected_fragment");
                    if (fragment != null) {
                        PrinterStatusHelper.updateConnectedPrinterFragment(this, fragment, event.getResultData());
                    } else {
                        PrinterStatusHelper.showNoPrinterSelectedFragment(this);
                    }
                    break;
                default:
                    Log.e(LOG_TAG, "Received unknown result code: " + event.getResultCode());
                    break;
            }

            if (event.getResultData().getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE") != null) {
                Log.e(LOG_TAG, "error message: " + event.getResultData().getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE"));
            }
        }
    }

    protected void updateDashboard() {
        if (isAppInstalled("com.zebra.printconnect")) {
            showPrintConnectInstalledFragment();

            if (!ConnectedPrinterFragment.isRefreshIconSpinning()) {
                PrinterStatusHelper.updatePrinterSelectedFragment(this);
            }
        } else {
            PrinterStatusHelper.showNoPrinterSelectedFragment(this);
            showPrintConnectNotInstalledFragment();
        }

        showAvailableDemosFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        if (TabletConfigurationHelper.isTabletConfiguration(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();

        while (statusIntentCompleteEvents.size() > 0) {
            StatusIntentCompleteEvent event = statusIntentCompleteEvents.get(0);
            PrinterStatusHelper.showPrinterSelectedFragment(this, event.getResultData());
            statusIntentCompleteEvents.remove(0);
        }

        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_about:
                showAboutDialog();
                return true;
            case android.R.id.home:
                restoreMainLayout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrintConfirm() {
        Log.i(LOG_TAG, "Print button was pressed");


        PrintDemoFragment printDemoFragment = (PrintDemoFragment) getFragmentManager().findFragmentByTag(PrintDemoFragment.FRAGMENT_TAG);

        if(printDemoFragment != null) {
            printDemoFragment.startPrintIntent();
        }
    }

    @Override
    public void onPrintCancel() {
        Log.i(LOG_TAG, "Cancel button was pressed");
        //TODO Do 'Cancel' handling here if required
    }

    private void showAboutDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AboutDialogFragment aboutDialog = (AboutDialogFragment) getFragmentManager().findFragmentByTag("about_dialog");

        if (aboutDialog != null) {
            ft.remove(aboutDialog);
        }
        aboutDialog = new AboutDialogFragment();
        aboutDialog.show(ft, "about_dialog");
    }

    public void restoreMainLayout() {
        PrinterStatusHelper.removeFragment(this, "print_demo_fragment");

        findViewById(R.id.fragmentScrollView).setVisibility(View.VISIBLE);
        findViewById(R.id.zebraFooterLogo).setVisibility(View.VISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        setTitle(R.string.app_name);
    }

    private void showAvailableDemosFragment() {
        FragmentManager fm = getFragmentManager();
        AvailableDemosFragment availableDemosFragment = (AvailableDemosFragment) fm.findFragmentByTag(AvailableDemosFragment.FRAGMENT_TAG);

        if (availableDemosFragment == null) {
            availableDemosFragment = new AvailableDemosFragment();
            fm.beginTransaction()
                    .replace(R.id.demosContainer, availableDemosFragment, AvailableDemosFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    private void showPrintConnectInstalledFragment() {
        PrinterStatusHelper.removeFragment(this, "print_connect_not_installed_fragment");
        PrinterStatusHelper.removeFragment(this, "print_connect_installed_fragment");

        PrintConnectInstalledFragment printConnectInstalledFragment = new PrintConnectInstalledFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.statusContainer, printConnectInstalledFragment, "print_connect_installed_fragment")
                .commit();
    }

    private void showPrintConnectNotInstalledFragment() {
        PrinterStatusHelper.removeFragment(this, "print_connect_not_installed_fragment");

        PrintConnectNotInstalledFragment printConnectNotInstalledFragment = new PrintConnectNotInstalledFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.printConnectNotInstalledContainer, printConnectNotInstalledFragment, "print_connect_not_installed_fragment")
                .commit();
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }

        return installed;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        FragmentManager fm = getFragmentManager();
        PrintDemoFragment printDemoFragment = (PrintDemoFragment) fm.findFragmentByTag("print_demo_fragment");
        if (printDemoFragment != null) {
            printDemoFragment.getView().findViewById(R.id.printButton).setEnabled(true);
        }
    }
}
