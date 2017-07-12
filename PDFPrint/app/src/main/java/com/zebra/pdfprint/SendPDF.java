package com.zebra.pdfprint;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public class SendPDF extends AsyncTask<Void, Boolean, Boolean> {
    private static final String TAG = "SEND_PDF_DIALOG";

    protected String MacAddress;

    protected MainActivity mainActivity;
    protected String filePath;

    public SendPDF(MainActivity mainActivity, String filePath) {
        this.mainActivity = mainActivity;
        this.filePath = filePath;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String snackbarMsg = mainActivity.getString(R.string.sent_to_printer);
        mainActivity.showSnackbar(snackbarMsg);
        DiscoveredPrinter selectedPrinter = SelectedPrinterManager.getSelectedPrinter();

        if(selectedPrinter == null) {
            MacAddress = mainActivity.MacAddress();
        }else{
            MacAddress = selectedPrinter.address;
        }

        if (MacAddress != null) {
            sendPrint(MacAddress);
        } else {
            snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.no_printer_selected);
            mainActivity.showSnackbar(snackbarMsg);
        }

        return null;
    }

    // Sees if the printer is ready to print
    private Boolean checkPrinterStatus(ZebraPrinter printer) {
        Log.i(TAG, "checkPrinterStatus()");
        try {
            PrinterStatus printerStatus = printer.getCurrentStatus();
            if (printerStatus.isReadyToPrint && filePath != null) {
                return true;
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
        return false;
    }

    //If there is an issue with the printer, this IDs the most common issues and tells the user
    private void showPrinterStatus(ZebraPrinter printer) {
        Log.i(TAG, "showPrinterStatus()");
        String snackbarMsg = "";
        try {
            PrinterStatus printerStatus = printer.getCurrentStatus();
            if (printerStatus.isReadyToPrint) {
                snackbarMsg = mainActivity.getString(R.string.ready_to_print);
            } else if (printerStatus.isPaused) {
                snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.printer_paused);
            } else if (printerStatus.isHeadOpen) {
                snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.head_open);
            } else if (printerStatus.isPaperOut) {
                snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.paper_out);
            } else {
                snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.cannot_print);
            }

            mainActivity.showSnackbar(snackbarMsg);

        } catch (ConnectionException e) {
            e.printStackTrace();
            snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.no_printer);
            mainActivity.showSnackbar(snackbarMsg);
        }
    }

    // Sets the scaling on the printer and then sends the pdf file to the printer
    private void sendPrint(String MacAddress) {
        Log.i(TAG, "sendPrint()");
        Connection connection = new BluetoothConnection(MacAddress);
        String snackbarMsg = "";

        try {
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            boolean isReady = checkPrinterStatus(printer);
            String scale = scalePrint(connection);

            SGD.SET("apl.settings",scale,connection);

            if (isReady) {
                if (filePath != null) {
                    printer.sendFileContents(filePath);
                } else {
                    snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.no_pdf_selected);
                    mainActivity.showSnackbar(snackbarMsg);
                }
            } else {
                showPrinterStatus(printer);
            }

        } catch (ConnectionException e) {
            e.printStackTrace();
            snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.no_printer);
            mainActivity.showSnackbar(snackbarMsg);
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    // Takes the size of the pdf and the printer's maximum size and scales the file down
    private String scalePrint (Connection connection) throws ConnectionException {
        int fileWidth = mainActivity.FileWidth();
        String scale = "dither scale-to-fit";

        if (fileWidth != 0) {
            String printerModel = SGD.GET("device.host_identification",connection).substring(0,5);
            double scaleFactor;

            if (printerModel.equals("iMZ22")||printerModel.equals("QLn22")||printerModel.equals("ZD410")) {
                scaleFactor = 2.0/fileWidth*100;
            } else if (printerModel.equals("iMZ32")||printerModel.equals("QLn32")||printerModel.equals("ZQ510")) {
                scaleFactor = 3.0/fileWidth*100;
            } else if (printerModel.equals("QLn42")||printerModel.equals("ZQ520")||
                    printerModel.equals("ZD420")||printerModel.equals("ZD500")||
                    printerModel.equals("ZT220")||printerModel.equals("ZT230")||
                    printerModel.equals("ZT410")) {
                scaleFactor = 4.0/fileWidth*100;
            } else if (printerModel.equals("ZT420")) {
                scaleFactor = 6.5/fileWidth*100;
            } else {
                scaleFactor = 100;
            }

            scale = "dither scale=" + (int) scaleFactor + "x" + (int) scaleFactor;
        }

        return scale;
    }

}