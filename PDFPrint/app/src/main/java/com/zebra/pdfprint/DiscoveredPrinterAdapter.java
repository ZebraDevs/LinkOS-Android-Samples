package com.zebra.pdfprint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.List;

public class DiscoveredPrinterAdapter extends ArrayAdapter<DiscoveredPrinter> {
    public DiscoveredPrinterAdapter(Context context, int resource, List<DiscoveredPrinter> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.list_item_discovered_printer, null);
        }

        TextView printerName = (TextView) convertView.findViewById(R.id.printerName);
        printerName.setText(getItem(position).toString());

        return convertView;
    }
}
