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

package com.zebra.android.devdemo.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

public abstract class DiscoveryResultList extends ExpandableListActivity implements DiscoveryHandler {

    protected List<String> discoveredPrinters = null;
    private ZebraExpandableListAdapter mExpListAdapter;

    public DiscoveryResultList() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.discovery_results);

        setProgressBarIndeterminateVisibility(true);

        mExpListAdapter = new ZebraExpandableListAdapter();
        setListAdapter(mExpListAdapter);
    }

    public void foundPrinter(final DiscoveredPrinter printer) {
        runOnUiThread(new Runnable() {
            public void run() {
                mExpListAdapter.addPrinterItem((DiscoveredPrinter) printer);
                mExpListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void discoveryFinished() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(DiscoveryResultList.this, " Discovered " + mExpListAdapter.getGroupCount() + " printers", Toast.LENGTH_SHORT).show();
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    public void discoveryError(String message) {
        new UIHelper(this).showErrorDialogOnGuiThread(message);
    }

    private class ZebraExpandableListAdapter extends BaseExpandableListAdapter {

        private ArrayList<DiscoveredPrinter> printerItems;
        private ArrayList<Map<String, String>> printerSettings;

        public ZebraExpandableListAdapter() {
            printerItems = new ArrayList<DiscoveredPrinter>();
            printerSettings = new ArrayList<Map<String, String>>();
        }

        public void addPrinterItem(DiscoveredPrinter p) {
            printerItems.add(p);
            printerSettings.add(p.getDiscoveryDataMap());
        }

        public Object getChild(int groupPosition, int childPosition) {
            return printerSettings.get(groupPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) DiscoveryResultList.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView itemView = (TextView) (inflater.inflate(android.R.layout.simple_list_item_1, null));
            StringBuilder settingsTextBuilder = new StringBuilder();
            itemView.setMaxLines(printerSettings.get(groupPosition).keySet().size());
            itemView.setTextSize(14.0f);
            for (String key : printerSettings.get(groupPosition).keySet()) {
                settingsTextBuilder.append(key);
                settingsTextBuilder.append(": ");
                settingsTextBuilder.append(printerSettings.get(groupPosition).get(key));
                settingsTextBuilder.append("\n");
            }
            itemView.setText(settingsTextBuilder.toString());
            return itemView;
        }

        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        public Object getGroup(int groupPosition) {
            return printerItems.get(groupPosition);
        }

        public int getGroupCount() {
            return printerItems.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) DiscoveryResultList.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TwoLineListItem itemView = (TwoLineListItem) (inflater.inflate(android.R.layout.simple_expandable_list_item_2, null));
            if (printerItems.get(groupPosition).getDiscoveryDataMap().containsKey("DARKNESS"))
                itemView.setBackgroundColor(0xff4477ff);
            if (printerItems.get(groupPosition) instanceof DiscoveredPrinterNetwork) {
                itemView.getText1().setText(((DiscoveredPrinterNetwork) printerItems.get(groupPosition)).address);
                itemView.getText2().setText(((DiscoveredPrinterNetwork) printerItems.get(groupPosition)).getDiscoveryDataMap().get("DNS_NAME"));
            } else if (printerItems.get(groupPosition) instanceof DiscoveredPrinterBluetooth) {
                itemView.getText1().setText(((DiscoveredPrinterBluetooth) printerItems.get(groupPosition)).address);
                itemView.getText2().setText(((DiscoveredPrinterBluetooth) printerItems.get(groupPosition)).friendlyName);
            }
            return itemView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

}
