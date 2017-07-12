package com.zebra.pdfprint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.ArrayList;

public class PrinterConnectionDialog extends DialogFragment {
    private static final String TAG = "PRINTER_CNNCTN_DIALOG";

    private com.zebra.pdfprint.MainActivity mainActivity;
    private TextView emptyView;

    private com.zebra.pdfprint.DiscoveredPrinterAdapter adapter;
    private ArrayList<DiscoveredPrinter> discoveredPrinters;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");

        mainActivity = (MainActivity) getActivity();
        View view = View.inflate(getActivity(), R.layout.dialog_printer_connect, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(view);

        discoveredPrinters = new ArrayList<>();
        adapter = new com.zebra.pdfprint.DiscoveredPrinterAdapter(getActivity(), R.layout.list_item_discovered_printer, discoveredPrinters);

        emptyView = (TextView) view.findViewById(R.id.discoveredPrintersEmptyView);

        ListView discoveredPrintersListView = (ListView) view.findViewById(R.id.discoveredPrintersListView);
        discoveredPrintersListView.setEmptyView(emptyView);
        discoveredPrintersListView.setAdapter(adapter);

        final AlertDialog dialog = builder.create();

        discoveredPrintersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.displayConnectingStatus();

                new com.zebra.pdfprint.SelectedPrinterTask((MainActivity) getActivity(), adapter.getItem(position)).execute();
                dialog.dismiss();
            }
        });

        try {
            BluetoothDiscoverer.findPrinters(getActivity(), new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    discoveredPrinters.add(discoveredPrinter);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "Discovered a printer");
                }

                @Override
                public void discoveryFinished() {
                    Log.i(TAG, "Discovery finished");
                }

                @Override
                public void discoveryError(String s) {
                    Log.i(TAG, "Discovery error");
                }
            });
        } catch (ConnectionException e) {
            Log.i(TAG, "Printer connection error");
        }
        return dialog;
    }


}
