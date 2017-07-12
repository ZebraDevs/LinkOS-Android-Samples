/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PrintResultDialogFragment extends DialogFragment implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {

    private static final ArrayList<PrintIntentDemoItem> intentDemoItems = new ArrayList<>();
    private static final List<PrintIntentDemoItemDetails> intentDemoDetails = new ArrayList<>();

    private static ExpandableListView expandableListView;
    private static PrintResultExpandableListAdapter resultListAdapter;

    private static boolean printDemoComplete = false;

    public static PrintResultDialogFragment newInstance() {
        printDemoComplete = false;

        intentDemoItems.clear();
        intentDemoDetails.clear();

        PrintResultDialogFragment printResultDialogFragment = new PrintResultDialogFragment();
        printResultDialogFragment.setCancelable(false);

        return printResultDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (resultListAdapter == null) {
            resultListAdapter = new PrintResultExpandableListAdapter(getActivity(), intentDemoItems, intentDemoDetails);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View intentResultView = View.inflate(getActivity(), R.layout.print_result_fragment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(intentResultView)
                .setTitle(R.string.printing_template)
                .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        expandableListView = (ExpandableListView) intentResultView.findViewById(R.id.printResultList);
        expandableListView.setAdapter(resultListAdapter);
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnGroupCollapseListener(this);
        expandableListView.setOnGroupExpandListener(this);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            setDialogNegativeButtonEnabled();
        }

        notifyDataSetChanged();
    }

    private void setDialogNegativeButtonEnabled() {
        if (getDialog() != null) {
            if (printDemoComplete) {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            } else {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
        }
    }

    public void addIntentDemoItem(String templateName) {
        intentDemoItems.add(new PrintIntentDemoItem(PrintIntentDemoItem.IntentState.Sending, templateName));
        intentDemoDetails.add(new PrintIntentDemoItemDetails());
        notifyDataSetChanged();
    }

    public void addIntentVariableDataMap(Map<String, String> variableDataMap, int printIntentListIndex) {
        intentDemoItems.get(printIntentListIndex).setVariableDataMap(variableDataMap);
        notifyDataSetChanged();
    }

    public void updateIntentDemoItem(PrintIntentDemoItem printIntentDemoItem, int printIntentListIndex) {
        PrintIntentDemoItem currentDemoItem = intentDemoItems.get(printIntentListIndex);
        currentDemoItem.setState(printIntentDemoItem.getState());
        notifyDataSetChanged();

        if (printIntentListIndex == intentDemoItems.size() - 1) {
            printDemoComplete = true;
            setDialogNegativeButtonEnabled();
        } else {
            printDemoComplete = false;
        }
    }

    public void addIntentDemoDetails(PrintIntentDemoItemDetails itemDetails, int printIntentListIndex) {
        intentDemoDetails.set(printIntentListIndex, itemDetails);
        notifyDataSetChanged();
    }

    public PrintIntentDemoItem getPrintIntentDemoItem(int index) {
        return intentDemoItems.get(index);
    }

    private void notifyDataSetChanged() {
        Activity activity = getActivity();
        if (activity != null && resultListAdapter != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (isDemoItemComplete(groupPosition)) {
            if (expandableListView.isGroupExpanded(groupPosition)) {
                expandableListView.collapseGroup(groupPosition);
            } else {
                expandableListView.expandGroup(groupPosition);
            }
        }
        return true;
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        setChevronState(groupPosition, PrintIntentDemoItem.CHEVRON_STATE_DOWN);
        notifyDataSetChanged();
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        setChevronState(groupPosition, PrintIntentDemoItem.CHEVRON_STATE_UP);
        notifyDataSetChanged();
    }

    private void setChevronState(int groupPosition, int chevronState) {
        intentDemoItems.get(groupPosition).setChevronState(chevronState);
    }

    private boolean isDemoItemComplete(int groupPosition) {
        return !intentDemoItems.get(groupPosition).getState().equals(PrintIntentDemoItem.IntentState.Sending);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
