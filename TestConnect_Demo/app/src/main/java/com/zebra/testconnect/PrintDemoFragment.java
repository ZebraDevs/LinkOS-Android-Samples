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
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.testconnect.event.PrintIntentCompleteEvent;
import com.zebra.testconnect.event.SendPrintIntentEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class PrintDemoFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {
    private static final String LOG_TAG = PrintDemoFragment.class.getName();
    public static final String PRINT_DEMO_KEY = "PRINT_DEMO_KEY";
    public static final String FRAGMENT_TAG = "print_demo_fragment";

    private static PrintDemo printDemo;
    private static List<PrintDemoItem> printDemoItems;
    private PrintDemoListAdapter demoListAdapter;

    private static PrintResultDialogFragment printStatusDialog;

    private boolean isPaused = false;
    private List<SendPrintIntentEvent> sendPrintIntentEvents = new ArrayList<>();
    private List<PrintIntentCompleteEvent> printIntentCompleteEvents = new ArrayList<>();

    private static final String templatePrintIntentServiceName = "\"com.zebra.printconnect.print.TemplatePrintService\"";

    private int printIntentResultIndex = 0;

    private static void updateIntentWithResult(PrintIntentCompleteEvent event) {
        PrintIntentDemoItem.IntentState state;
        int printIntentListIndex = event.getPrintIntentListIndex();

        PrintIntentDemoItemDetails itemDetails = new PrintIntentDemoItemDetails();
        itemDetails.setSentMessage(templatePrintIntentServiceName);

        int resultCode = event.getResultCode();
        itemDetails.setResultCode(resultCode);

        switch (resultCode) {
            case 0:
                state = PrintIntentDemoItem.IntentState.Success;
                break;

            default:
                state = PrintIntentDemoItem.IntentState.Error;

                String errorMessage = event.getResultData().getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE");
                itemDetails.setErrorMessage(errorMessage != null ? errorMessage : "");
                break;
        }

        String templateName = printStatusDialog.getPrintIntentDemoItem(printIntentListIndex).getTemplateName();
        PrintIntentDemoItem demoItem = new PrintIntentDemoItem(state, templateName);
        updateIntentStatus(printIntentListIndex, demoItem, itemDetails);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        printDemo = (PrintDemo) arguments.getSerializable(PRINT_DEMO_KEY);
        if(null != printDemo) {
            printDemoItems = printDemo.getPrintDemoItems();
        }

        EventBus.getDefault().register(this);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_print_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View headerView = View.inflate(getActivity(), R.layout.demo_header_layout, null);

        Activity activity = getActivity();
        activity.findViewById(R.id.fragmentScrollView).setVisibility(View.GONE);
        activity.findViewById(R.id.zebraFooterLogo).setVisibility(View.GONE);

        demoListAdapter = new PrintDemoListAdapter(getActivity(), R.layout.print_demo_list_item, printDemoItems);
        ListView printDemoListView = (ListView) view.findViewById(R.id.printDemoList);
        printDemoListView.addHeaderView(headerView, null, false);
        printDemoListView.setAdapter(demoListAdapter);

        initializeViewData(view, activity);

        ((TextView) view.findViewById(R.id.fileSize)).setText(printDemo.getHumanReadableTemplateSize());

        Button printButton = (Button) view.findViewById(R.id.printButton);
        printButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            printButton.setEnabled(savedInstanceState.getBoolean("isPrintButtonEnabled", true));
        }

        //Click listener for Back button
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(this);

        view.findViewById(R.id.refreshButton).setOnClickListener(this);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                ((MainActivity) getActivity()).restoreMainLayout();
                return true;
            }
        }
        return false;
    }

    private void initializeViewData(View view, Activity activity) {
        activity.setTitle(printDemo.getTitle());

        if (printDemo instanceof PrintDemoReceipt) {
            updatePrintDemoInfo(view);
        }

        updatePrintDemoSampleData(printDemo.getSampleDataList());
    }

    private void updatePrintDemoSampleData(List<PrintDemoSampleData> printDemoSampleData) {
        printDemoItems.clear();

        int currentSampleDataIndex = printDemo.getCurrentSampleDataIndex();
        List<PrintDemoItem> demoSampleData = printDemoSampleData.get(currentSampleDataIndex).getItemList();

        for (int i = 0; i < demoSampleData.size(); i++) {
            printDemoItems.add(new PrintDemoItem(demoSampleData.get(i).getPrice(), demoSampleData.get(i).getDescription(), demoSampleData.get(i).getUpc()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        printStatusDialog = (PrintResultDialogFragment) getFragmentManager().findFragmentByTag("print_status_dialog");

        while (printIntentCompleteEvents.size() > 0) {
            updateIntentWithResult(printIntentCompleteEvents.get(0));
            printIntentCompleteEvents.remove(0);
        }

        while (sendPrintIntentEvents.size() > 0) {
            sendIntent(sendPrintIntentEvents.get(0));
            sendPrintIntentEvents.remove(0);
        }

        isPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isPrintButtonEnabled", getActivity().findViewById(R.id.printButton).isEnabled());
        super.onSaveInstanceState(outState);
    }

    private void updatePrintDemoInfo(View view) {
        ((TextView) view.findViewById(R.id.fileSize)).setText(R.string.receipt_file_size);
        ((TextView) view.findViewById(R.id.printDemoHeader)).setText(R.string.receipt_demo_header);
        ((TextView) view.findViewById(R.id.dimensions)).setText(R.string.receipt_dimensions);
        ((ImageView) view.findViewById(R.id.demoPrintIcon)).setImageResource(R.drawable.icon_file);
        ((TextView) view.findViewById(R.id.demoDescription)).setText(R.string.demo_receipt_description);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.printButton:
                showPrintConfirmationDialog(printDemo.getTemplateFileNames());
                break;

            case R.id.refreshButton:
                int currentSampleDataIndex = printDemo.getCurrentSampleDataIndex() + 1;
                if (currentSampleDataIndex >= printDemo.getSampleDataList().size()) {
                    printDemo.setCurrentSampleDataIndex(0);
                } else {
                    printDemo.setCurrentSampleDataIndex(currentSampleDataIndex);
                }

                updatePrintDemoSampleData(printDemo.getSampleDataList());
                demoListAdapter.notifyDataSetChanged();

                ((TextView) getActivity().findViewById(R.id.fileSize)).setText(printDemo.getHumanReadableTemplateSize());
                break;

            default:
                Log.e(LOG_TAG, "Unknown button click");
                break;
        }
    }

    public void startPrintIntent() {
        View view = getView();
        if(null != view) {
            view.findViewById(R.id.printButton).setEnabled(false);
        }
        printIntentResultIndex = 0;
        new PrintDemoIntentTask(printDemo.buildPrintDemoTemplateData()).execute();
    }

    private void showPrintConfirmationDialog(ArrayList<String> templateFileNames) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PrintConfirmationDialog.SELECTED_FILE_NAME_KEY, templateFileNames);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        PrintConfirmationDialog confirmPrintDialog = (PrintConfirmationDialog) getFragmentManager().findFragmentByTag(PrintConfirmationDialog.FRAGMENT_TAG);

        if (confirmPrintDialog != null) {
            ft.remove(confirmPrintDialog);
        }
        confirmPrintDialog = new PrintConfirmationDialog();
        confirmPrintDialog.setArguments(bundle);
        confirmPrintDialog.setCancelable(false);
        confirmPrintDialog.show(ft, PrintConfirmationDialog.FRAGMENT_TAG);
    }

    public void onEventMainThread(SendPrintIntentEvent event) {
        if (isPaused) {
            sendPrintIntentEvents.add(event);
        } else {
            sendIntent(event);
        }
    }

    public void onEventMainThread(PrintIntentCompleteEvent event) {
        if (isPaused) {
            printIntentCompleteEvents.add(event);
        } else {
            updateIntentWithResult(event);
        }
    }

    private static void updateIntentStatus(int index, PrintIntentDemoItem demoItem, PrintIntentDemoItemDetails demoItemDetails) {
        printStatusDialog.updateIntentDemoItem(demoItem, index);
        printStatusDialog.addIntentDemoDetails(demoItemDetails, index);
    }

    private void sendIntent(final SendPrintIntentEvent event) {
        Intent templatePrintIntent = new Intent();
        templatePrintIntent.setComponent(new ComponentName("com.zebra.printconnect", "com.zebra.printconnect.print.TemplatePrintService"));
        templatePrintIntent.putExtra("com.zebra.printconnect.PrintService.TEMPLATE_FILE_NAME", event.getTemplateName());
        templatePrintIntent.putExtra("com.zebra.printconnect.PrintService.VARIABLE_DATA", (HashMap) event.getVariableDataMap());

        templatePrintIntent.putExtra(PrinterStatusHelper.RECEIVER_KEY, PrinterStatusHelper.buildIPCSafeReceiver(new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                EventBus.getDefault().post(new PrintIntentCompleteEvent(resultCode, resultData, printIntentResultIndex++));
            }
        }));
        getActivity().startService(templatePrintIntent);
    }

    private class PrintDemoIntentTask extends AsyncTask<Void, String, Void> {

        private List<PrintDemoTemplateData> templateData;
        private final Handler handler = new Handler();

        public PrintDemoIntentTask(List<PrintDemoTemplateData> templateData) {
            this.templateData = templateData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showPrintStatusDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int itemNumber = 0;

            for (PrintDemoTemplateData demoTemplateData : templateData) {
                for (Map<String, String> varsMap : demoTemplateData.getVariableData()) {
                    publishProgress(demoTemplateData.getTemplateName());
                    waitForProgressUpdate();

                    printStatusDialog.addIntentVariableDataMap(varsMap, itemNumber);
                    EventBus.getDefault().post(new SendPrintIntentEvent(demoTemplateData.getTemplateName(), varsMap));

                    itemNumber++;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String templateName = values[0];
            printStatusDialog.addIntentDemoItem(templateName);

            synchronized (handler) {
                handler.notify();
            }
        }

        private void waitForProgressUpdate() {
            synchronized (handler) {
                try {
                    handler.wait();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                }
            }
        }

        private void showPrintStatusDialog() {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            printStatusDialog = (PrintResultDialogFragment) getFragmentManager().findFragmentByTag("print_status_dialog");

            if (printStatusDialog != null) {
                ft.remove(printStatusDialog);
            }

            printStatusDialog = PrintResultDialogFragment.newInstance();
            printStatusDialog.show(ft, "print_status_dialog");
        }
    }
}
