package com.zebra.rfid_demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Integer.valueOf;



/**
 * The class handles all of the field configuration. It is launched when the Configure Custom Data
 * Entry button is pressed in the SettingsActivity or when the custom data entry switch is turned
 * on.
 *
 * This activity allows the user to change the number of fields that will appear on new lead entry
 * (minimum 1, maximum 10). As this number is changed, fields will appear and disappear accordingly.
 *
 * Previously saved fields will automatically appear as fields when this activity is launched. If
 * there are no previously saved fields found, one blank field will appear when the activity starts.
 *
 * This activity ends when the hard back button or the soft back button on the action bar is
 * pressed. When the activity ends, all changes are saved. This returns to the SettingsActivity.
 *
 * If there are any fields left blank, they will be ignored and not saved upon exit.
 */
public class CustomFieldConfigActivity extends DatabaseActivity {
    private static final String CUSTOM_CONFIG_TAG = "custom_field_config";

    private int maxNumberFields = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_field_config);

        getCustomEntryFields();

        /*
        If custom data entry is turned off, display bright red warning about it.
         */
        if (!customDataEntry) {
            final TextView enabledError = (TextView) findViewById(R.id.enabledError);
            enabledError.setVisibility(View.VISIBLE);
        }

        final LinearLayout inputLayout = (LinearLayout) findViewById(R.id.inputLayout);
        final TextView numFields = (TextView) findViewById(R.id.numFields);
        final Button minusButton = (Button) findViewById(R.id.minusButton);
        final Button plusButton = (Button) findViewById(R.id.plusButton);

        /*
        Sets the fields to decrease with the - button.
        Disables and enables the - and + buttons according to the max number of fields.
         */
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = valueOf(numFields.getText().toString());

                if ((i-1)<1) {
                    minusButton.setEnabled(false);
                } else if ((i-1)==1) {
                    minusButton.setEnabled(false);
                    numFields.setText("" + (i-1));
                    Log.i(CUSTOM_CONFIG_TAG, "Minus one field: " + (i-1));

                    inputLayout.removeView(inputLayout.getChildAt(i-1));

                } else {
                    minusButton.setEnabled(true);
                    numFields.setText("" + (i-1));
                    Log.i(CUSTOM_CONFIG_TAG, "Minus one field: " + (i-1));

                    inputLayout.removeView(inputLayout.getChildAt(i-1));
                }

                if ((i-1)<maxNumberFields) {
                    plusButton.setEnabled(true);
                }
            }
        });

        /*
        Sets the fields to increase with the + button.
        Disables and enables the - and + buttons according to the max number of fields.
         */
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = valueOf(numFields.getText().toString());

                if ((i+1)>maxNumberFields) {
                    plusButton.setEnabled(false);
                } else if ((i+1)==maxNumberFields) {
                    plusButton.setEnabled(false);
                    numFields.setText("" + (i+1));
                    Log.i(CUSTOM_CONFIG_TAG, "Plus one field: " + (i+1));

                    addNewField("");
                } else {
                    plusButton.setEnabled(true);
                    numFields.setText("" + (i+1));
                    Log.i(CUSTOM_CONFIG_TAG, "Plus one field: " + (i+1));

                    addNewField("");
                }

                if ((i+1)>1) {
                    minusButton.setEnabled(true);
                }
            }
        });


        for (int i=0; i<customEntryFields.size(); i++) {
            addNewField(customEntryFields.get(i));
        }

        if (customEntryFields.size()<1) {   //If nothing has been saved previously
            addNewField("");
            minusButton.setEnabled(false);
        } else {
            numFields.setText("" + customEntryFields.size());
        }

        /*
        Displays Zebra marketing team alert in dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomFieldConfigActivity.this);
        builder .setMessage(R.string.country_and_email)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(CUSTOM_CONFIG_TAG,"In onStop() for CustomFieldConfigActivity");
        customEntryFields.clear();
        final LinearLayout inputLayout = (LinearLayout) findViewById(R.id.inputLayout);
        final TextView numFields = (TextView) findViewById(R.id.numFields);
        boolean firstIssue = true;

        /*
        Goes through all the fields. If any are left blank, a Toast is displayed telling the user
        they will be ignored. Adds data from fields to customEntryFields.
         */
        for (int i=0; i<valueOf(numFields.getText().toString()); i++) {
            View v = inputLayout.getChildAt(i);
            final EditText edit = (EditText) v.findViewById(R.id.fieldEdit);
            if (edit.getText().toString().length()<1 && firstIssue) {
                Log.d(CUSTOM_CONFIG_TAG,"One or more field titles left blank");

                Toast toast = Toast.makeText(getApplicationContext(),"One or more field titles " +
                        "were left blank. They will be disregarded.",Toast.LENGTH_SHORT);
                toast.show();
                firstIssue = false;
            }
            if (edit.getText().toString().length()>0) {
                customEntryFields.add(edit.getText().toString());
                Log.d(CUSTOM_CONFIG_TAG, "Adding " + edit.getText().toString() + " to customEntryFields");
            }
        }

        updateCustomEntryFields();
    }

    /**
     * Adds a new field to the activity.
     *
     * @param text      Text to be displayed in the EditText, if any.
     */
    private void addNewField(String text) {
        final LinearLayout inputLayout = (LinearLayout) findViewById(R.id.inputLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View fieldView = inflater.inflate(R.layout.list_item_field_config, null);

        final EditText fieldEdit = (EditText) fieldView.findViewById(R.id.fieldEdit);
        fieldEdit.setHint("Field Title");
        fieldEdit.setText(text);

        inputLayout.addView(fieldView, inputLayout.getChildCount());
    }
}