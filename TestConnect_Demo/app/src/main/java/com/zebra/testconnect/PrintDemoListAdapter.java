/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class PrintDemoListAdapter extends ArrayAdapter<PrintDemoItem> {

    private final List<PrintDemoItem> demoItemList;

    public PrintDemoListAdapter(Context context, int resource, List<PrintDemoItem> objects) {
        super(context, resource, objects);
        demoItemList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.print_demo_list_item, parent, false);
        }

        PrintDemoItem demoItem = demoItemList.get(position);

        TextView itemNumber = (TextView) convertView.findViewById(R.id.itemNumber);

        String itemId;
        if (demoItem.getUpc() == null) {
            itemId = String.format(getContext().getString(R.string.item_number) + " %d", position + 1);
        } else {
            itemId = getContext().getString(R.string.item_number);
        }
        itemNumber.setText(itemId);

        TextView itemSummary = (TextView) convertView.findViewById(R.id.itemSummary);
        itemSummary.setText(String.format(getContext().getString(R.string.item_summary), demoItem.getDescription(), demoItem.getPrice()));

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
