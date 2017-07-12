/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PrintResultExpandableListAdapter extends BaseExpandableListAdapter {

    private final Activity activity;

    private final List<PrintIntentDemoItem> intentDemoItems;
    private final List<PrintIntentDemoItemDetails> intentDemoItemDetails;

    public PrintResultExpandableListAdapter(Activity activity, List<PrintIntentDemoItem> intentDemoItems, List<PrintIntentDemoItemDetails> intentDemoItemDetails) {
        this.activity = activity;
        this.intentDemoItems = intentDemoItems;
        this.intentDemoItemDetails = intentDemoItemDetails;
    }

    @Override
    public int getGroupCount() {
        return intentDemoItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public List<PrintIntentDemoItem> getGroup(int groupPosition) {
        return intentDemoItems;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return intentDemoItemDetails.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.print_result_item, parent, false);
        }

        PrintIntentDemoItem demoItem = intentDemoItems.get(groupPosition);
        PrintIntentDemoItem.IntentState state = demoItem.getState();

        String statusMessage = getIntentStatusMessage(state);
        TextView statusText = (TextView) convertView.findViewById(R.id.statusText);
        statusText.setText(String.format("%s %s", statusMessage, demoItem.getTemplateName()));

        ImageView statusIcon = (ImageView) convertView.findViewById(R.id.printStatusIcon);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.printResultChevron);

        int progressSpinnerVisibility = View.GONE;
        if (state.equals(PrintIntentDemoItem.IntentState.Sending)) {
            chevron.setImageResource(R.drawable.icon_chevron_up_gray);
            progressSpinnerVisibility = View.VISIBLE;
            statusIcon.setVisibility(View.GONE);
        } else {
            chevron.setImageResource(demoItem.getChevronState());

            int resourceId = R.drawable.icon_checkmark_green;
            if (state.equals(PrintIntentDemoItem.IntentState.Error)) {
                resourceId = R.drawable.icon_error_dash_red;
            }

            statusIcon.setImageResource(resourceId);
            statusIcon.setTag(resourceId);
            statusIcon.setVisibility(View.VISIBLE);
        }

        convertView.findViewById(R.id.progressSpinner).setVisibility(progressSpinnerVisibility);
        return convertView;
    }

    private String getIntentStatusMessage(PrintIntentDemoItem.IntentState state) {
        String statusMessage = "";
        switch (state) {
            case Error:
                statusMessage = activity.getString(R.string.error_sending_intent);
                break;

            case Sending:
                statusMessage = activity.getString(R.string.sending_intent);
                break;

            case Success:
                statusMessage = activity.getString(R.string.sent_intent);
                break;
        }
        return statusMessage;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.print_result_details, parent, false);
        }

        PrintIntentDemoItem intentDemoItem = intentDemoItems.get(groupPosition);
        PrintIntentDemoItemDetails itemDetails = intentDemoItemDetails.get(groupPosition);

        TextView sentDetails = (TextView) convertView.findViewById(R.id.intentSentDetails);
        sentDetails.setText(Html.fromHtml("<b>" + activity.getString(R.string.intent_name) + "</b>" + itemDetails.getSentMessage()));

        TextView intentVarData = (TextView) convertView.findViewById(R.id.intentSentVarData);
        intentVarData.setText(Html.fromHtml("<b>" + activity.getString(R.string.intent_var_data) + "</b>" + intentDemoItem.getVariableDataMap().toString()));

        TextView resultCode = (TextView) convertView.findViewById(R.id.intentResultCode);
        resultCode.setText(Integer.toString(itemDetails.getResultCode()));

        TextView resultCodeDescription = (TextView) convertView.findViewById(R.id.intentResultCodeDescription);
        resultCodeDescription.setText("(" + itemDetails.getResultCodeDescription(parent.getContext()) + ")");

        TextView responseDetails = (TextView) convertView.findViewById(R.id.intentResultMessage);
        responseDetails.setText(Html.fromHtml("<b>" + activity.getString(R.string.intent_result_error_message) + "</b>" + itemDetails.getErrorMessage()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
