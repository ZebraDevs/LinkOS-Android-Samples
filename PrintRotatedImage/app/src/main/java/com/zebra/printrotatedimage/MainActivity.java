/**
 * ********************************************
 * CONFIDENTIAL AND PROPRIETARY
 * <p/>
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * <p/>
 * Copyright ZIH Corp. 2015
 * <p/>
 * ALL RIGHTS RESERVED
 * *********************************************
 */


package com.zebra.printrotatedimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Connection connection;
    private Spinner imageSelectionSpinner;
    private CheckBox cb;
    private Spinner angleSpinner;
    private ProgressDialog myDialog;

    private RadioButton btRadioButton;
    private EditText macAddressEditText;
    private EditText ipAddressEditText;
    private EditText portNumberEditText;
    private EditText printStoragePath;
    private static final String bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS";
    private static final String tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS";
    private static final String tcpPortKey = "ZEBRA_DEMO_TCP_PORT";
    private static final String PREFS_NAME = "OurSavedAddress";
    private UIHelper helper = new UIHelper(this);


    private static int TAKE_PICTURE = 1;
    private static int PICTURE_FROM_GALLERY = 2;
    private static File file = null;
    private int rotationAngle = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        ipAddressEditText = (EditText) this.findViewById(R.id.ipAddressInput);
        String ip = settings.getString(tcpAddressKey, "");
        ipAddressEditText.setText(ip);

        portNumberEditText = (EditText) this.findViewById(R.id.portInput);
        String port = settings.getString(tcpPortKey, "");
        portNumberEditText.setText(port);

        macAddressEditText = (EditText) this.findViewById(R.id.macInput);
        String mac = settings.getString(bluetoothAddressKey, "");
        macAddressEditText.setText(mac);

        TextView t2 = (TextView) findViewById(R.id.launchpad_link);
        t2.setMovementMethod(LinkMovementMethod.getInstance());


        printStoragePath = (EditText) findViewById(R.id.printerStorePath);

        cb = (CheckBox) findViewById(R.id.checkBox);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    printStoragePath.setVisibility(View.VISIBLE);
                } else {
                    printStoragePath.setVisibility(View.INVISIBLE);
                }
            }
        });


        btRadioButton = (RadioButton) this.findViewById(R.id.bluetoothRadio);


        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.bluetoothRadio) {
                    toggleEditField(macAddressEditText, true);
                    toggleEditField(portNumberEditText, false);
                    toggleEditField(ipAddressEditText, false);
                } else {
                    toggleEditField(portNumberEditText, true);
                    toggleEditField(ipAddressEditText, true);
                    toggleEditField(macAddressEditText, false);
                }
            }
        });

        angleSpinner = (Spinner) findViewById(R.id.rotationSpinner);
        ArrayAdapter<CharSequence> angleAdapter = ArrayAdapter.createFromResource(this, R.array.rotation_array, android.R.layout.simple_spinner_item);
        angleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        angleSpinner.setAdapter(angleAdapter);
        angleSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());


        imageSelectionSpinner = (Spinner) findViewById(R.id.imageSelection);
        ArrayAdapter<CharSequence> imageAdapter = ArrayAdapter.createFromResource(this, R.array.image_selection, android.R.layout.simple_spinner_item);
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageSelectionSpinner.setAdapter(imageAdapter);
        imageSelectionSpinner.setOnItemSelectedListener(new OnItemsSelectedListener());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PICTURE) {
                printRotatedPhotoFromExternal(BitmapFactory.decodeFile(file.getAbsolutePath()), rotationAngle);
            }
            if (requestCode == PICTURE_FROM_GALLERY) {
                Uri imgPath = data.getData();
                Bitmap myBitmap = null;
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgPath);
                } catch (FileNotFoundException e) {
                    helper.showErrorDialog(e.getMessage());
                } catch (IOException e) {
                    helper.showErrorDialog(e.getMessage());
                }
                printRotatedPhotoFromExternal(myBitmap, rotationAngle);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * OnItemSelectedListener for angleSpinner
     */


    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            rotationAngle = Integer.parseInt(parent.getItemAtPosition(pos).toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    /**
     * OnItemSelectedListener for imageSelectionSpinner
     */

    public class OnItemsSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            imageSelectionSpinner.setSelection(0);
            if (position == 1) {
                getPhotoFromCamera();
            } else if (position == 2) {
                getPhotosFromGallery();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    /**
     * Intents to make a call when image is captured using camera
     */

    private void getPhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "tempPic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, TAKE_PICTURE);
    }

    /**
     * Intents to make a call when photos are selected from gallery
     */

    private void getPhotosFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICTURE_FROM_GALLERY);
    }

    /**
     * @param bitmap
     * @param rotationAngle
     * @return Rotates the bitmap image according to the specified angle and returns a new Bitmap instance.
     */
    private Bitmap rotateBitmap(final Bitmap bitmap, int rotationAngle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }

    /**
     * This method calls the rotated image and send it to the final method.
     *
     * @param bitmap
     * @param rotationAngle
     */

    private void printRotatedPhotoFromExternal(final Bitmap bitmap, int rotationAngle) {
        Bitmap rotatedBitmap = rotateBitmap(bitmap, rotationAngle);
        printPhotoFromExternal(rotatedBitmap);
    }


    /**
     * This method makes the call to the printer and send the images to the printer to print and implements best practices to check the status of the printer.
     *
     * @param bitmap
     */
    private void printPhotoFromExternal(final Bitmap bitmap) {

        new Thread(new Runnable() {
            public void run() {

                try {
                    getAndSaveSettings();
                    Looper.prepare();
                    connection = getZebraPrinterConn();
                    connection.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                    getPrinterStatus();

                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

                    PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

                    /**
                     * check if the printer is ready or not and then send the image to print
                     */

                    if (printerStatus.isReadyToPrint) {
                        try {
                            if (((CheckBox) findViewById(R.id.checkBox)).isChecked()) {
                                printer.storeImage(printStoragePath.getText().toString(), new ZebraImageAndroid(bitmap), 550, 412);
                            } else {
                                helper.showLoadingDialog("Printer Ready \nProcessing to Print.");
                                printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 550, 412, false);

                            }
                        } catch (ZebraIllegalArgumentException e) {
                            helper.showErrorDialogOnGuiThread(e.getMessage());
                        } catch (ConnectionException e) {
                            helper.showErrorDialogOnGuiThread(e.getMessage());
                        }
                    } else if (printerStatus.isHeadOpen) {
                        helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print.");
                    } else if (printerStatus.isPaused) {
                        helper.showErrorMessage("Error: Printer Paused.");
                    } else if (printerStatus.isPaperOut) {
                        helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print.");
                    } else {
                        helper.showErrorMessage("Error: Please check the Connection of the Printer.");
                    }

                    connection.close();

                    if (file != null) {
                        file.delete();
                        file = null;
                    }
                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } finally {
                    bitmap.recycle();
                    helper.dismissLoadingDialog();
                    Looper.myLooper().quit();

                }
            }

        }).start();

    }


    /**
     * This method implements the best practices i.e., Checks the language of the printer and set the language of the printer to ZPL.
     *
     * @throws ConnectionException
     */
    private void getPrinterStatus() throws ConnectionException {


        final String printerLanguage = SGD.GET("device.languages", connection);

        final String displayPrinterLanguage = "Printer Language is " + printerLanguage;

        SGD.SET("device.languages", "zpl", connection);

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, displayPrinterLanguage + "\n" + "Language set to ZPL", Toast.LENGTH_LONG).show();

            }
        });

    }

    private void toggleEditField(EditText editText, boolean set) {
        /*
         * Note: Disabled EditText fields may still get focus by some other means, and allow text input.
         *       See http://code.google.com/p/android/issues/detail?id=2771
         */
        editText.setEnabled(set);
        editText.setFocusable(set);
        editText.setFocusableInTouchMode(set);
    }

    private boolean isBluetoothSelected() {
        return btRadioButton.isChecked();
    }

    private String getMacAddressFieldText() {
        return macAddressEditText.getText().toString();
    }

    private String getTcpAddress() {
        return ipAddressEditText.getText().toString();
    }

    private String getTcpPortNumber() {
        return portNumberEditText.getText().toString();
    }

    /**
     * This method checks the mode of connection.
     *
     * @return
     */
    private Connection getZebraPrinterConn() {
        int portNumber;
        try {
            portNumber = Integer.parseInt(getTcpPortNumber());
        } catch (NumberFormatException e) {
            portNumber = 0;
        }
        return isBluetoothSelected() ? new BluetoothConnection(getMacAddressFieldText()) : new TcpConnection(getTcpAddress(), portNumber);
    }

    /**
     * This method saves the entered address for the printer.
     */

    private void getAndSaveSettings() {
        SettingsHelper.saveBluetoothAddress(MainActivity.this, getMacAddressFieldText());
        SettingsHelper.saveIp(MainActivity.this, getTcpAddress());
        SettingsHelper.savePort(MainActivity.this, getTcpPortNumber());
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


}
