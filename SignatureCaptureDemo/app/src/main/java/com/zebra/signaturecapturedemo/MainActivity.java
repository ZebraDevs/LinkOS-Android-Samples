package com.zebra.signaturecapturedemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

public class MainActivity extends ConnectionScreen {

    private ProgressDialog myDialog;
    private UIHelper helper = new UIHelper(this);
    private Connection connection;
    private SignatureArea signatureArea;
    private AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * This is an abstract method from connection screen where we are implementing the calls.
     */
    @Override
    public void performTest() {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doPerformTest();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    /**
     * This method is used to create a new alert dialog to sign and print and implements best practices to check the status of the printer.
     */
    public void doPerformTest() {
        if (isBluetoothSelected() == false) {
            try {
                int port = Integer.parseInt(getTcpPortNumber());
                connection = new TcpConnection(getTcpAddress(), port);
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        } else {
            connection = new BluetoothConnection(getMacAddressFieldText());
        }
        try {
            connection.open();
            final ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
            PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
            getPrinterStatus();
            if (printerStatus.isReadyToPrint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this, "Printer Ready", Toast.LENGTH_LONG).show();

                    }
                });

                        View view = View.inflate(MainActivity.this, R.layout.signature_print_dialog, null);
                        signatureArea = (SignatureArea) view.findViewById(R.id.signatureArea);
                        builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        builder.setView(view);
                        builder.setPositiveButton(getString(R.string.print), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    connection.open();
                                    Bitmap signatureBitmap = Bitmap.createScaledBitmap(signatureArea.getBitmap(), 300, 200, false);
                                    printer.printImage(new ZebraImageAndroid(signatureBitmap), 0, 0, signatureBitmap.getWidth(), signatureBitmap.getHeight(), false);
                                    dialog.dismiss();
                                    connection.close();

                                } catch (ConnectionException e) {
                                    helper.showErrorDialogOnGuiThread(e.getMessage());
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create();
                        builder.show();


            } else if (printerStatus.isHeadOpen) {
                helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print");
            } else if (printerStatus.isPaused) {
                helper.showErrorMessage("Error: Printer Paused");
            } else if (printerStatus.isPaperOut) {
                helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print");
            } else {
                helper.showErrorMessage("Error: Please check the Connection of the Printer");
            }

            connection.close();
            getAndSaveSettings();
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } finally {
            helper.dismissLoadingDialog();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void createCancelProgressDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                myDialog = new ProgressDialog(MainActivity.this, R.style.ErrorButtonAppearance);
                myDialog.setMessage(message);
                myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Use either finish() or return() to either close the activity or just the dialog
                        return;
                    }
                });
                myDialog.show();
                TextView tv1 = (TextView) myDialog.findViewById(android.R.id.message);
                tv1.setTextAppearance(MainActivity.this, R.style.ErrorButtonAppearance);
                Button b = myDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                b.setTextColor(getResources().getColor(R.color.light_gray));
                b.setBackgroundColor(getResources().getColor(R.color.zebra_blue));
            }
        });

    }

    /**
     * This method implements the best practices to check the language of the printer and set the language of the printer to ZPL.
     *
     * @throws ConnectionException
     */
    private void getPrinterStatus() throws ConnectionException{


        final String printerLanguage = SGD.GET("device.languages", connection); //This command is used to get the language of the printer.

        final String displayPrinterLanguage = "Printer Language is " + printerLanguage;

        SGD.SET("device.languages", "zpl", connection); //This command set the language of the printer to ZPL

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, displayPrinterLanguage + "\n" + "Language set to ZPL", Toast.LENGTH_LONG).show();

            }
        });

    }

    /**
     * This method saves the entered address of the printer.
     */

    private void getAndSaveSettings() {
        SettingsHelper.saveBluetoothAddress(MainActivity.this, getMacAddressFieldText());
        SettingsHelper.saveIp(MainActivity.this, getTcpAddress());
        SettingsHelper.savePort(MainActivity.this, getTcpPortNumber());
    }


}
