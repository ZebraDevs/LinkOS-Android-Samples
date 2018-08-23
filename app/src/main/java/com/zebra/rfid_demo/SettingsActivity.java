package com.zebra.rfid_demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * This class handles the main settings menu.
 *
 * It is launched when the wrench icon is pressed on the main menu and the correct password entered.
 *
 * Activity ends and returns to MainActivity when the hard back button or the soft back button on
 * the action bar is pressed. Before returning, all settings are saved.
 */
public class SettingsActivity extends DatabaseActivity {
    private static final String SETTINGS_TAG = "settings_menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setupSavedSettings();

        final Switch parseStringsCheck = (Switch) findViewById(R.id.parseDataStrings);
        final RelativeLayout suggestedDelimiterLayout = (RelativeLayout) findViewById(R.id.suggestedDelimiter);

        /**
         * Where the suggested delimiter is flipped on and off with the flipping of the parse data
         * string switch
         */
        parseStringsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (parseStringsCheck.isChecked()) {
                    suggestedDelimiterLayout.setVisibility(View.VISIBLE);
                } else {
                    suggestedDelimiterLayout.setVisibility(View.GONE);
                }
            }
        });

        /**
         * Starts a CustomFieldConfigActivity when the custom data entry switch is turned on
         */
        final Switch customDataEntry = (Switch) findViewById(R.id.customDataEntry);
        customDataEntry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (customDataEntry.isChecked()) {
                    updateSettings();
                    Intent intent = new Intent(SettingsActivity.this, CustomFieldConfigActivity.class);
                    startActivity(intent);
                }
            }
        });

        final TextView configCustomEntryButton = (TextView) findViewById(R.id.configCustomEntryButton);
        configCustomEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(SETTINGS_TAG, "Configure Custom Entry button clicked");

                updateSettings();
                Intent intent = new Intent(SettingsActivity.this, CustomFieldConfigActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (updateSettings()) { //Doesn't allow the Activity to end unless the folder name is valid
            super.onBackPressed();
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        getSharedVariables();
        setupSavedSettings();
    }

    @Override
    public void onBackPressed(){
        if (updateSettings()) { //Doesn't allow the Activity to end unless the folder name is valid
            super.onBackPressed();
        }
    }

    /**
     * Uses the global settings variables to update all the objects on screen to their saved states.
     *
     * If parseScannedData is true, it will make the suggested delimeter field visible.
     */
    private void setupSavedSettings() {
        final TextView pathText = (TextView) findViewById(R.id.pathText);
        pathText.setText(getExternalStorageDirectory() + "/");
        final EditText folderText = (EditText) findViewById(R.id.folderText);
        folderText.setText(folderName);
        final Switch timestampCheck = (Switch) findViewById(R.id.timestampNewFiles);
        timestampCheck.setChecked(timestampFileNames);
        final Switch customDataEntryCheck = (Switch) findViewById(R.id.customDataEntry);
        customDataEntryCheck.setChecked(customDataEntry);
        final Switch parseStringsCheck = (Switch) findViewById(R.id.parseDataStrings);
        parseStringsCheck.setChecked(parseScannedData);
        final RelativeLayout suggestedDelimiterLayout = (RelativeLayout) findViewById(R.id.suggestedDelimiter);
        if (parseScannedData) {
            suggestedDelimiterLayout.setVisibility(View.VISIBLE);
            final EditText suggestedDelimiterInput = (EditText) findViewById(R.id.suggestedDelimiterInput);
            suggestedDelimiterInput.setText(suggestedDelimiter);
        }
    }

    /**
     * Checks the name of the folder in the EditText to make sure it's legal. If not, it makes an
     * error message visible and returns false.
     *
     * If the folder name is valid, it updates the global settings variables, updates the
     * SharedPreferences, and returns true.
     *
     * @return      Returns true if settings (more specifically the name of the folder) are valid
     */
    private boolean updateSettings() {
        final Switch timestampCheck = (Switch) findViewById(R.id.timestampNewFiles);
        final Switch customDataEntryCheck = (Switch) findViewById(R.id.customDataEntry);
        final Switch parseStringsCheck = (Switch) findViewById(R.id.parseDataStrings);
        final EditText folderText = (EditText) findViewById(R.id.folderText);
        final EditText suggestedDelimiterInput = (EditText) findViewById(R.id.suggestedDelimiterInput);
        String newFolderName = folderText.getText().toString();

        //Do all the folder name checks:
        if (!newFolderName.matches(".*[~\"{}:?*@#%&|].*")
                && !newFolderName.contains("..")
                && !newFolderName.contains("[")
                && !newFolderName.contains("]")
                && !newFolderName.contains(">")
                && !newFolderName.contains("<")
                && newFolderName.toCharArray()[0]!='.'
                && newFolderName.toCharArray()[0]!=' '
                && newFolderName.toCharArray()[newFolderName.length()-1]!='.'
                && newFolderName.toCharArray()[newFolderName.length()-1]!=' '
                && newFolderName.length()>0) {

            //Update all the global settings variables
            folderName = folderText.getText().toString();
            timestampFileNames = timestampCheck.isChecked();
            customDataEntry = customDataEntryCheck.isChecked();
            parseScannedData = parseStringsCheck.isChecked();
            if (parseScannedData) {
                suggestedDelimiter = suggestedDelimiterInput.getText().toString();
            }
            else { suggestedDelimiter = ""; }

            updateSharedVariables();
            return true;
        } else {
            Log.d(SETTINGS_TAG, "Invalid folder name");
            final TextView errorText = (TextView) findViewById(R.id.errorText);
            errorText.setVisibility(View.VISIBLE);

            return false;
        }
    }
}
