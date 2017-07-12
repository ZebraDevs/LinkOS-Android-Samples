package com.zebra.pdfprint;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_PRINTER_CONNECT_TAG = "printer_connect";
    private static final String DIALOG_PDF_PICK_TAG = "pdf_pick";
    private static final String DIALOG_PRINT_TAG = "print";

    protected TextView printerSelectionStatus;
    protected TextView printerInfoTableName;
    protected TextView printerInfoTableAddress;
    protected TableLayout printerInfoTable;

    protected TextView pdfSelectionStatus;
    protected TextView pdfInfoTableName;
    protected TextView pdfInfoTablePath;
    protected TableLayout pdfInfoTable;

    protected TextView printButton;

    protected DiscoveredPrinter chosenPrinter;
    protected String MacAddress;
    protected String filePath = null;
    protected Integer fileWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printerSelectionStatus = (TextView) findViewById(R.id.printerSelectionStatus);
        printerInfoTableName = (TextView) findViewById(R.id.printerInfoTableName);
        printerInfoTableAddress = (TextView) findViewById(R.id.printerInfoTableAddress);
        printerInfoTable = (TableLayout) findViewById(R.id.printerInfoTable);

        pdfSelectionStatus = (TextView) findViewById(R.id.pdfSelectionStatus);
        pdfInfoTableName = (TextView) findViewById(R.id.pdfInfoTableName);
        pdfInfoTablePath = (TextView) findViewById(R.id.pdfInfoTablePath);
        pdfInfoTable = (TableLayout) findViewById(R.id.pdfInfoTable);


        TextView selectPrinterButton = (TextView) findViewById(R.id.selectPrinterButton);
        selectPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PRINTER_CONNECT_TAG,"Select Printer button clicked");

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                com.zebra.pdfprint.PrinterConnectionDialog printerConnectionDialog = (com.zebra.pdfprint.PrinterConnectionDialog) getFragmentManager().findFragmentByTag(DIALOG_PRINTER_CONNECT_TAG);

                if (printerConnectionDialog == null) {
                    printerConnectionDialog = new com.zebra.pdfprint.PrinterConnectionDialog();
                    printerConnectionDialog.show(ft, DIALOG_PRINTER_CONNECT_TAG);
                }
            }
        });

        TextView selectPDFButton = (TextView) findViewById(R.id.selectPDFButton);
        selectPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PDF_PICK_TAG, "Select PDF button clicked");

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PDFPick pdfPick = new com.zebra.pdfprint.PDFPick();


                pdfPick.show(ft, DIALOG_PDF_PICK_TAG);
            }
        });

        printButton = (TextView) findViewById(R.id.printNowButton);
        printButton.setOnClickListener(null);
    }

    public String MacAddress(){
        return MacAddress;
    }

    public String FilePath(){
        return filePath;
    }

    public Integer FileWidth(){
        return fileWidth;
    }

    public void displayConnectingStatus() {
        printerSelectionStatus.setText(getString(R.string.connecting_to_printer));
    }

    public void updatePrinterInfoTable(DiscoveredPrinter discoveredPrinter) {
        printerInfoTableName.setText(discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
        printerInfoTableAddress.setText(discoveredPrinter.getDiscoveryDataMap().get("HARDWARE_ADDRESS"));
        printerInfoTable.setVisibility(View.VISIBLE);
        printerSelectionStatus.setText(getString(R.string.connected_to_printer));
        chosenPrinter=discoveredPrinter;

        MacAddress=discoveredPrinter.toString();
        updatePrintButton();
    }

    public void updatePDFInfoTable(String pdfName, String pdfPath) {
        pdfInfoTableName.setText(pdfName);
        pdfInfoTablePath.setText(pdfPath);
        pdfInfoTable.setVisibility(View.VISIBLE);
        pdfSelectionStatus.setText(getString(R.string.selected_a_pdf));

        filePath = pdfPath;
        updatePrintButton();
    }

    public void resetConnectingStatus() {
        printerInfoTable.setVisibility(View.GONE);
        printerSelectionStatus.setText(getString(R.string.no_printer_selected));
    }

    public void showSnackbar(String snackbarText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.near_black));
        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void updatePrintButton() {
        if ((filePath != null) && (MacAddress != null)) {
            printButtonEnable();
        }else {
            printButtonDisable();
        }
    }

    public void printButtonEnable() {
        printButton = (TextView) findViewById(R.id.printNowButton);

        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.zebra_red));
        printButton.setText(R.string.print);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PrintDialog printDialog = new com.zebra.pdfprint.PrintDialog();
                printDialog.show(ft, DIALOG_PRINT_TAG);
            }
        });
    }

    public void printButtonDisable() {
        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        printButton.setText(R.string.unable_to_print);
        printButton.setOnClickListener(null);
    }
}
