/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * Copyright ZIH Corp. 2015
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class PrintConfirmationDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = "print_confirmation_dialog";
    public static final String SELECTED_FILE_NAME_KEY = "selectedFileName";
    private AlertDialog printConfirmationDialog;

    interface PrintConfirmationListener {
        void onPrintConfirm();
        void onPrintCancel();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PrintConfirmationListener)) {
            throw new ClassCastException(activity.toString() + " must implement PrintConfirmationListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("unchecked")
        List<String> templateFileNamesList = (List<String>) getArguments().getSerializable(SELECTED_FILE_NAME_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View printDialogView = View.inflate(getActivity(), R.layout.confirm_print_dialog, null);

        TextView templateFileHeaderText = (TextView) printDialogView.findViewById(R.id.templateFileHeader);
        TextView templateFileValue = (TextView) printDialogView.findViewById(R.id.templateFileValue);
        TextView selectedPrinterValue = (TextView) printDialogView.findViewById(R.id.selectedPrinterValue);

        builder.setTitle(R.string.print_job)
                .setView(printDialogView)
                .setPositiveButton(R.string.print, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((PrintConfirmationListener) getActivity()).onPrintConfirm();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((PrintConfirmationListener) getActivity()).onPrintCancel();
                        dialog.dismiss();
                    }
                });

        if (templateFileNamesList != null && !templateFileNamesList.isEmpty()) {
            if (templateFileNamesList.size() > 1) {
                templateFileHeaderText.setText(getString(R.string.template_files));
            }

            String templateFileNames = "";
            for (String templateName : templateFileNamesList) {
                templateFileNames += templateName + ", ";
            }
            templateFileNames = templateFileNames.substring(0, templateFileNames.length() - 2);
            templateFileValue.setText(templateFileNames);
        }

        setPrintDialogSelectedPrinterText(ConnectedPrinterFragment.getStatusMap(), selectedPrinterValue);

        printConfirmationDialog = builder.create();

        return printConfirmationDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (printConfirmationDialog != null) {
            setPrintDialogPositiveButtonEnabled(printConfirmationDialog, ConnectedPrinterFragment.getStatusMap());
        }
    }

    private void setPrintDialogSelectedPrinterText(@Nullable Map<String, String> statusMap, TextView selectedPrinterValue) {
        String selectedPrinterName = null;

        if (statusMap != null) {
            selectedPrinterName = statusMap.get("friendlyName");
        }

        if (selectedPrinterName != null) {
            selectedPrinterValue.setText(selectedPrinterName);
        } else {
            selectedPrinterValue.setText(R.string.no_printer_selected);
        }
    }

    private void setPrintDialogPositiveButtonEnabled(AlertDialog printDialog, @Nullable Map<String, String> statusMap) {
        if (statusMap != null && statusMap.get("isReadyToPrint").equals("true")) {
            printDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            printDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public void updateDisplayedPrinter(@Nullable Map<String, String> statusMap) {
        if (printConfirmationDialog != null) {
            setPrintDialogSelectedPrinterText(statusMap, (TextView) printConfirmationDialog.findViewById(R.id.selectedPrinterValue));
            setPrintDialogPositiveButtonEnabled(printConfirmationDialog, statusMap);
        }
    }
}
