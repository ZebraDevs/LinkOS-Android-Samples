package com.zebra.pdfprint;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.io.IOException;
import java.util.Map;

public class SelectedPrinterTask extends AsyncTask<Void, Boolean, Boolean> {

    private static final String TAG = "SELECTED_PNTR_TASK";

    private MainActivity mainActivity;
    private DiscoveredPrinter selectedPrinter = null;

    public SelectedPrinterTask(MainActivity mainActivity, DiscoveredPrinter selectedPrinter) {
        this.mainActivity = mainActivity;
        this.selectedPrinter = selectedPrinter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        com.zebra.pdfprint.SelectedPrinterManager.setSelectedPrinter(null);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        class BluetoothConnectionQuickClose extends BluetoothConnection {

            public BluetoothConnectionQuickClose(String address) {
                super(address);
            }

            @Override
            public void close() throws ConnectionException {
                this.friendlyName = "";
                if (this.isConnected) {
                    this.isConnected = false;

                    try {
                        this.inputStream.close();
                        this.outputStream.close();
                        this.commLink.close();
                    } catch (IOException e) {
                        throw new ConnectionException("Could not disconnect from device: " + e.getMessage());
                    }
                }
            }
        }

        boolean result;

        try {
            Connection connection = new BluetoothConnectionQuickClose(selectedPrinter.address);
            connection.open();
            try {
                ZebraPrinter zebraPrinter = ZebraPrinterFactory.getInstance(connection);
                boolean isPdfPrinter = isPDFEnabled(connection);
                if (!isPdfPrinter) {
                    String snackbarmsg = mainActivity.getString(R.string.wrong_firmware);
                    mainActivity.showSnackbar(snackbarmsg);

                    connection.close();
                    return false;
                }
            }catch (ZebraPrinterLanguageUnknownException e)
            {
                Log.e(TAG, "Open connection error", e);
            };

            Map<String, String> discoveryMap = selectedPrinter.getDiscoveryDataMap();
            discoveryMap.put("LINK_OS_MAJOR_VER", SGD.GET("appl.link_os_version", connection));
            discoveryMap.put("PRODUCT_NAME", SGD.GET("device.product_name", connection));
            discoveryMap.put("SYSTEM_NAME", SGD.GET("bluetooth.friendly_name", connection));
            discoveryMap.put("HARDWARE_ADDRESS", SGD.GET("bluetooth.address", connection));
            discoveryMap.put("FIRMWARE_VER", SGD.GET("appl.name", connection));
            discoveryMap.put("SERIAL_NUMBER", SGD.GET("device.unique_id", connection));

            result = true;
            com.zebra.pdfprint.SelectedPrinterManager.setSelectedPrinter(selectedPrinter);
            connection.close();

        } catch (ConnectionException e) {
            Log.e(TAG, "Open connection error", e);
            result = false;
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean populateBluetoothDiscoDataSuccessful) {
        super.onPostExecute(populateBluetoothDiscoDataSuccessful);

        if (populateBluetoothDiscoDataSuccessful) {
            mainActivity.updatePrinterInfoTable(selectedPrinter);
        } else {
            mainActivity.resetConnectingStatus();
        }
    }

    // Checks the selected printer to see if it has the pdf virtual device installed.
    private boolean isPDFEnabled(Connection connection) {
        try {
            String printerInfo = SGD.GET("apl.enable", connection);
            if (printerInfo.equals("pdf")) {
                return true;
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        }

        return false;
    }
}
