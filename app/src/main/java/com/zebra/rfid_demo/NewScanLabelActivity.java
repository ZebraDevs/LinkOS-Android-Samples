package com.zebra.rfid_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewScanLabelActivity extends ScanningHelper{

    private static final String SCANNED_INTENT_TAG = "scan_recieved";
    private static final String NEW_ENTRY_TAG = "new_entry";
    private static final int PRINT_RFID_LABEL = 0;
    private static final int PRINT_LABEL = 1;
    private String barcodeDataString = "";  //Data String from the barcode scan

    MainActivity myactivity ;
    Context myContext;
    private static EditText uPC, productName, provider, price, qty,discount, color, style, size_product;

    final String[] BarcodeData = new String[5];
    final String[] BarcodeData_db = new String[9];

    IntentFilter filter = new IntentFilter("com.zebra.rfid_demo.RECVR");
    BroadcastReceiver receiver = new BroadcastReceiver() { //When an intent is caught, handles it
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(NEW_ENTRY_TAG,"Scan received");
            handleScan(intent);
            myContext= context;
        }
    };

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.context;
        setContentView(R.layout.activity_new_label_entry);
        registerReceiver(receiver,filter);

        getSharedVariables();
        getCustomEntryFields();
        final LinearLayout fieldsLayout = (LinearLayout) findViewById(R.id.fieldsLayout);
        barcodeDataString = "";


        uPC = (EditText) findViewById(R.id.UPC_Input);

        productName = (EditText) findViewById(R.id.productName_Input);

        provider = (EditText)findViewById(R.id.provider_Input);

        price = (EditText)findViewById(R.id.priceItem_Input);

        qty = (EditText)findViewById(R.id.quantity_input);

        discount = (EditText)findViewById(R.id.discount_input);

        color = (EditText)findViewById(R.id.color_input);

        style = (EditText)findViewById(R.id.style_input);

        size_product = (EditText)findViewById(R.id.size_input);

        uPC.setText("100000000001");
        productName.setText("New Item");
        provider.setText("New Item");
        price.setText("none");
        qty.setText("1");
        discount.setText("na");
        color.setText("na");
        style.setText("na");
        size_product.setText("na");


        final TextView printLabelButton = (TextView) findViewById(R.id.buttonLabel);
        printLabelButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View parent) {


                BarcodeData_db[0] = uPC.getText().toString();
                BarcodeData_db[1] = productName.getText().toString();
                BarcodeData_db[2] = provider.getText().toString();
                BarcodeData_db[3] = price.getText().toString();
                BarcodeData_db[4] = qty.getText().toString();
                BarcodeData_db[5] = discount.getText().toString();
                BarcodeData_db[6] = color.getText().toString();
                BarcodeData_db[7] = style.getText().toString();
                BarcodeData_db[8] = size_product.getText().toString();

                Log.d(SCANNED_INTENT_TAG,"Scan detected _Print: " +  BarcodeData_db[0]+"," +BarcodeData_db[1]+","+BarcodeData_db[2]+","+BarcodeData_db[3]+","
                                                                        + BarcodeData_db[4]+"," +BarcodeData_db[5]+","+BarcodeData_db[6]+","+BarcodeData_db[7]+",");

                if(!BarcodeData_db[0].isEmpty()) {
                    addNewEntry(BarcodeData_db);
                    new PrintTaskRfid(parent, this, context, BarcodeData_db, PRINT_LABEL, fieldsLayout).execute((Void[]) null);
                }
                 finish();
                 Intent intent1;
                 intent1 = new Intent(NewScanLabelActivity.this , MainActivity.class);
                 startActivity(intent1);

            }
        });


        final TextView RfidButton = (TextView) findViewById(R.id.buttonRfid);
        RfidButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View parent) {


                BarcodeData_db[0] = uPC.getText().toString();
                BarcodeData_db[1] = productName.getText().toString();
                BarcodeData_db[2] = provider.getText().toString();
                BarcodeData_db[3] = price.getText().toString();
                BarcodeData_db[4] = qty.getText().toString();
                BarcodeData_db[5] = discount.getText().toString();
                BarcodeData_db[6] = color.getText().toString();
                BarcodeData_db[7] = style.getText().toString();
                BarcodeData_db[8] = size_product.getText().toString();

                Log.d(SCANNED_INTENT_TAG,"Scan detected: " +  BarcodeData_db[0]+"," +BarcodeData_db[1]+","+BarcodeData_db[2]+","+BarcodeData_db[3]+","
                                                                + BarcodeData_db[4]+"," +BarcodeData_db[5]+","+BarcodeData_db[6]+","+BarcodeData_db[7]+",");

                if(!BarcodeData_db[0].isEmpty()) {
                    addNewEntry(BarcodeData_db);
                    new PrintTaskRfid(parent, this, context, BarcodeData_db, PRINT_RFID_LABEL, fieldsLayout).execute((Void[]) null);
                }
                finish();
                Intent intent;
                intent = new Intent(NewScanLabelActivity.this , MainActivity.class);
                startActivity(intent);


            }
        });

             /*
        Catches the intent that started the activity to see if it was started by a hard scan.
         */
        Intent i = getIntent();
        if (i.getIntExtra("soft_scan",-1) == 1) {
            Log.d(SCANNED_INTENT_TAG,"Hard scan found");
            handleScan(i);
        }

    }



    private void handleScan(Intent intent) {
        barcodeDataString = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");
        if (barcodeDataString == null) {
            barcodeDataString = intent.getStringExtra("barcode_data");
        }
        if (barcodeDataString == null) { return; }

        Log.d(SCANNED_INTENT_TAG,"Scan detected: " + barcodeDataString);

        if(barcodeDataString.length()==12) {

            BarcodeData[0] = barcodeDataString.substring(0, 1);

            Log.d(SCANNED_INTENT_TAG, "parsed substring 1: " + BarcodeData[0]);
            BarcodeData[1] = barcodeDataString.substring(1, 6);
            Log.d(SCANNED_INTENT_TAG, "parsed substring 2: " + BarcodeData[1]);

            BarcodeData[2] = barcodeDataString.substring(6, 11);
            Log.d(SCANNED_INTENT_TAG, "parsed substring 3: " + BarcodeData[2]);

            BarcodeData[3] = barcodeDataString.substring(11, 12);
            Log.d(SCANNED_INTENT_TAG, "parsed substring 4: " + BarcodeData[3]);

            BarcodeData[4] = barcodeDataString;
            Log.d(SCANNED_INTENT_TAG, "parsed substring 5: " + BarcodeData[4]);

            setFieldData(BarcodeData);
        }else{
            Snackbar.make(findViewById(R.id.fieldsLayout), "This is not a standard UPC barcode", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed()
    {
        // Add your code here
        finish();
        Intent intent;
        intent = new Intent(NewScanLabelActivity.this , MainActivity.class);
        startActivity(intent);

        // Then call the parent constructor to have the default button functionality
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
    }
    private void setFieldData(String [] splitBarcode) {
        final LinearLayout fieldsLayout = (LinearLayout) findViewById(R.id.fieldsLayout);

            String [] BarcodeRetrieveDb = new String[9];
            try {
                BarcodeRetrieveDb = retrieveDataUPC(splitBarcode[4]);

                Log.d(SCANNED_INTENT_TAG, "Data_retrieved: " + BarcodeRetrieveDb[0] + "," + BarcodeRetrieveDb[1] + "," + BarcodeRetrieveDb[2] + "," + BarcodeRetrieveDb[3]
                        + "," + BarcodeRetrieveDb[4] + "," + BarcodeRetrieveDb[5] + "," + BarcodeRetrieveDb[6] + "," + BarcodeRetrieveDb[7] + ",");

                uPC.setText("100000000001");
                productName.setText("New Item");
                provider.setText("New Item");
                price.setText("none");
                qty.setText("1");
                discount.setText("na");
                color.setText("na");
                style.setText("na");
                size_product.setText("na");

                if (BarcodeRetrieveDb[0] != null) {
                    uPC.setText(BarcodeRetrieveDb[0]);
                    productName.setText(BarcodeRetrieveDb[1]);
                    provider.setText(BarcodeRetrieveDb[2]);
                    price.setText(BarcodeRetrieveDb[3]);

                    int qty_int = Integer.parseInt(BarcodeRetrieveDb[4]);
                    qty_int++;
                    String new_qty = String.valueOf(qty_int);
                    qty.setText(new_qty);
                    discount.setText(BarcodeRetrieveDb[5]);
                    color.setText(BarcodeRetrieveDb[6]);
                    style.setText(BarcodeRetrieveDb[7]);
                    size_product.setText(BarcodeRetrieveDb[8]);
                } else {
                    uPC.setText(splitBarcode[4]);
                    productName.setText(splitBarcode[2]);
                    provider.setText(splitBarcode[1]);
                }

            }catch(NullPointerException e){
                uPC.setText(splitBarcode[4]);
                productName.setText(splitBarcode[2]);
                provider.setText(splitBarcode[1]);
            }

    }




        }



