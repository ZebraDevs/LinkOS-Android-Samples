/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2012
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.android.devdemo.receipt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zebra.android.devdemo.ConnectionScreen;
import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.DemoSleeper;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class ReceiptDemo extends ConnectionScreen {

    private UIHelper helper = new UIHelper(this);
    private boolean sendData = true;
    Connection printerConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testButton.setText("Print Using One Job");

        Button printAsManyJobsButton = new Button(this);
        printAsManyJobsButton.setText("Print Using Multiple Jobs");

        LinearLayout connection_screen_layout = (LinearLayout) findViewById(R.id.connection_screen_layout);
        int index = connection_screen_layout.indexOfChild(testButton);

        connection_screen_layout.addView(printAsManyJobsButton, index + 1);

        printAsManyJobsButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sendData = false;
                performTestWithManyJobs();
            }
        });

    }

    @Override
    public void performTest() {
        executeTest(false);
    }

    public void performTestWithManyJobs() {
        executeTest(true);
    }

    public void executeTest(final boolean withManyJobs) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                connectAndSendLabel(withManyJobs);
                Looper.loop();
                Looper.myLooper().quit();
                sendData = true;
            }
        }).start();

    }

    private void connectAndSendLabel(final boolean withManyJobs) {
        if (isBluetoothSelected() == false) {
            try {
                printerConnection = new TcpConnection(getTcpAddress(), Integer.valueOf(getTcpPortNumber()));
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        } else {
            printerConnection = new BluetoothConnection(getMacAddressFieldText());
        }
        try {
            helper.showLoadingDialog("Connecting...");
            printerConnection.open();

            ZebraPrinter printer = null;

            if (printerConnection.isConnected()) {
                printer = ZebraPrinterFactory.getInstance(printerConnection);

                if (printer != null) {
                    PrinterLanguage pl = printer.getPrinterControlLanguage();
                    if (pl == PrinterLanguage.CPCL) {
                        helper.showErrorDialogOnGuiThread("This demo will not work for CPCL printers!");
                    } else {
                        // [self.connectivityViewController setStatus:@"Building receipt in ZPL..." withColor:[UIColor
                        // cyanColor]];
                        if (withManyJobs) {
                            sendTestLabelWithManyJobs(printerConnection);
                        } else {
                            sendTestLabel();
                        }
                    }
                    printerConnection.close();
                    saveSettings();
                }
            }
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread("Could not detect printer language");
        } finally {
            helper.dismissLoadingDialog();
        }
    }

    private void sendTestLabel() {
        try {
            byte[] configLabel = createZplReceipt().getBytes();
            printerConnection.write(configLabel);
            DemoSleeper.sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
        }
    }

    private void sendTestLabelWithManyJobs(Connection printerConnection) {
        try {
            sendZplReceipt(printerConnection);
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        }

    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(ReceiptDemo.this, getMacAddressFieldText());
        SettingsHelper.saveIp(ReceiptDemo.this, getTcpAddress());
        SettingsHelper.savePort(ReceiptDemo.this, getTcpPortNumber());
    }

    private Map<String, String> createListOfItems() {
        String[] names = { "Microwave Oven", "Sneakers (Size 7)", "XL T-Shirt", "Socks (3-pack)", "Blender", "DVD Movie" };
        String[] prices = { "79.99", "69.99", "39.99", "12.99", "34.99", "16.99" };
        Map<String, String> retVal = new HashMap<String, String>();

        for (int ix = 0; ix < names.length; ix++) {
            retVal.put(names[ix], prices[ix]);
        }
        return retVal;
    }

    private String createZplReceipt() {
        /*
         This routine is provided to you as an example of how to create a variable length label with user specified data.
         The basic flow of the example is as follows

            Header of the label with some variable data
            Body of the label
                Loops thru user content and creates small line items of printed material
            Footer of the label
         
         As you can see, there are some variables that the user provides in the header, body and footer, and this routine uses that to build up a proper ZPL string for printing.
         Using this same concept, you can create one label for your receipt header, one for the body and one for the footer. The body receipt will be duplicated as many items as there are in your variable data
         
         */

        String tmpHeader =
        /*
         Some basics of ZPL. Find more information here : http://www.zebra.com/content/dam/zebra/manuals/en-us/printer/zplii-pm-vol2-en.pdf
                  
         ^XA indicates the beginning of a label
         ^PW sets the width of the label (in dots)
         ^MNN sets the printer in continuous mode (variable length receipts only make sense with variably sized labels)
         ^LL sets the length of the label (we calculate this value at the end of the routine)
         ^LH sets the reference axis for printing. 
            You will notice we change this positioning of the 'Y' axis (length) as we build up the label. Once the positioning is changed, all new fields drawn on the label are rendered as if '0' is the new home position
         ^FO sets the origin of the field relative to Label Home ^LH
         ^A sets font information 
         ^FD is a field description
         ^GB is graphic boxes (or lines)
         ^B sets barcode information
         ^XZ indicates the end of a label
         */

        "^XA" +

        "^PON^PW400^MNN^LL%d^LH0,0" + "\r\n" +

        "^FO50,50" + "\r\n" + "^A0,N,70,70" + "\r\n" + "^FD Shipping^FS" + "\r\n" +

        "^FO50,130" + "\r\n" + "^A0,N,35,35" + "\r\n" + "^FDPurchase Confirmation^FS" + "\r\n" +

        "^FO50,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDCustomer:^FS" + "\r\n" +

        "^FO225,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDAcme Industries^FS" + "\r\n" +

        "^FO50,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDDelivery Date:^FS" + "\r\n" +

        "^FO225,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD%s^FS" + "\r\n" +

        "^FO50,273" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDItem^FS" + "\r\n" +

        "^FO280,273" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDPrice^FS" + "\r\n" +

        "^FO50,300" + "\r\n" + "^GB350,5,5,B,0^FS";

        int headerHeight = 325;
        String body = String.format("^LH0,%d", headerHeight);

        int heightOfOneLine = 40;

        float totalPrice = 0;

        Map<String, String> itemsToPrint = createListOfItems();

        int i = 0;
        for (String productName : itemsToPrint.keySet()) {
            String price = itemsToPrint.get(productName);

            String lineItem = "^FO50,%d" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD%s^FS" + "\r\n" + "^FO280,%d" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD$%s^FS";
            totalPrice += Float.parseFloat(price);
            int totalHeight = i++ * heightOfOneLine;
            body += String.format(lineItem, totalHeight, productName, totalHeight, price);

        }

        long totalBodyHeight = (itemsToPrint.size() + 1) * heightOfOneLine;

        long footerStartPosition = headerHeight + totalBodyHeight;

        String footer = String.format("^LH0,%d" + "\r\n" +

        "^FO50,1" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +

        "^FO50,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FDTotal^FS" + "\r\n" +

        "^FO175,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FD$%.2f^FS" + "\r\n" +

        "^FO50,130" + "\r\n" + "^A0,N,45,45" + "\r\n" + "^FDPlease Sign Below^FS" + "\r\n" +

        "^FO50,190" + "\r\n" + "^GB350,200,2,B^FS" + "\r\n" +

        "^FO50,400" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +

        "^FO50,420" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDThanks for choosing us!^FS" + "\r\n" +

        "^FO50,470" + "\r\n" + "^B3N,N,45,Y,N" + "\r\n" + "^FD0123456^FS" + "\r\n" + "^XZ", footerStartPosition, totalPrice);

        long footerHeight = 600;
        long labelLength = headerHeight + totalBodyHeight + footerHeight;

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = sdf.format(date);

        String header = String.format(tmpHeader, labelLength, dateString);

        String wholeZplLabel = String.format("%s%s%s", header, body, footer);

        return wholeZplLabel;
    }

    private void sendZplReceipt(Connection printerConnection) throws ConnectionException {
        /*
         This routine is provided to you as an example of how to create a variable length label with user specified data.
         The basic flow of the example is as follows

            Header of the label with some variable data
            Body of the label
                Loops thru user content and creates small line items of printed material
            Footer of the label
         
         As you can see, there are some variables that the user provides in the header, body and footer, and this routine uses that to build up a proper ZPL string for printing.
         Using this same concept, you can create one label for your receipt header, one for the body and one for the footer. The body receipt will be duplicated as many items as there are in your variable data
         
         */

        String tmpHeader =
        /*
         Some basics of ZPL. Find more information here : http://www.zebra.com
                  
         ^XA indicates the beginning of a label
         ^PW sets the width of the label (in dots)
         ^MNN sets the printer in continuous mode (variable length receipts only make sense with variably sized labels)
         ^LL sets the length of the label (we calculate this value at the end of the routine)
         ^LH sets the reference axis for printing. 
            You will notice we change this positioning of the 'Y' axis (length) as we build up the label. Once the positioning is changed, all new fields drawn on the label are rendered as if '0' is the new home position
         ^FO sets the origin of the field relative to Label Home ^LH
         ^A sets font information 
         ^FD is a field description
         ^GB is graphic boxes (or lines)
         ^B sets barcode information
         ^XZ indicates the end of a label
         */

        "^XA" +

        "^POI^PW400^MNN^LL325^LH0,0" + "\r\n" +

        "^FO50,50" + "\r\n" + "^A0,N,70,70" + "\r\n" + "^FD Shipping^FS" + "\r\n" +

        "^FO50,130" + "\r\n" + "^A0,N,35,35" + "\r\n" + "^FDPurchase Confirmation^FS" + "\r\n" +

        "^FO50,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDCustomer:^FS" + "\r\n" +

        "^FO225,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDAcme Industries^FS" + "\r\n" +

        "^FO50,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDDelivery Date:^FS" + "\r\n" +

        "^FO225,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD%s^FS" + "\r\n" +

        "^FO50,273" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDItem^FS" + "\r\n" +

        "^FO280,273" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDPrice^FS" + "\r\n" +

        "^FO50,300" + "\r\n" + "^GB350,5,5,B,0^FS" + "^XZ";

        int headerHeight = 325;

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = sdf.format(date);

        String header = String.format(tmpHeader, dateString);

        printerConnection.write(header.getBytes());

        int heightOfOneLine = 40;

        float totalPrice = 0;

        Map<String, String> itemsToPrint = createListOfItems();

        int i = 0;
        for (String productName : itemsToPrint.keySet()) {
            String price = itemsToPrint.get(productName);

            String lineItem = "^XA^POI^LL40" + "^FO50,10" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD%s^FS" + "\r\n" + "^FO280,10" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD$%s^FS" + "^XZ";
            totalPrice += Float.parseFloat(price);
            String oneLineLabel = String.format(lineItem, productName, price);

            printerConnection.write(oneLineLabel.getBytes());

        }

        long totalBodyHeight = (itemsToPrint.size() + 1) * heightOfOneLine;

        long footerStartPosition = headerHeight + totalBodyHeight;

        String footer = String.format("^XA^POI^LL600" + "\r\n" +

        "^FO50,1" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +

        "^FO50,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FDTotal^FS" + "\r\n" +

        "^FO175,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FD$%.2f^FS" + "\r\n" +

        "^FO50,130" + "\r\n" + "^A0,N,45,45" + "\r\n" + "^FDPlease Sign Below^FS" + "\r\n" +

        "^FO50,190" + "\r\n" + "^GB350,200,2,B^FS" + "\r\n" +

        "^FO50,400" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +

        "^FO50,420" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDThanks for choosing us!^FS" + "\r\n" +

        "^FO50,470" + "\r\n" + "^B3N,N,45,Y,N" + "\r\n" + "^FD0123456^FS" + "\r\n" + "^XZ", totalPrice);

        printerConnection.write(footer.getBytes());

    }
}
