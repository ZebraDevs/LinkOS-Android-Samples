/**
 * Created by BWai on 7/7/2015.
 */

package com.zebra.isv.tapbluetoothwifi;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

//Implements the Bluetooth Device Array List View

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {
    private final Context context;
    private final ArrayList<BluetoothDevice> values;

    public BluetoothDeviceArrayAdapter(Context context, ArrayList<BluetoothDevice> values) {
        super(context,R.layout.row_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout,parent, false);
        TextView tvDeviceName = (TextView) rowView.findViewById(R.id.tvDeviceName);
        tvDeviceName.setText(values.get(position).getName());
        TextView tvDeviceAddress = (TextView) rowView.findViewById(R.id.tvDeviceAddress);
        tvDeviceAddress.setText(values.get(position).getAddress());
        return rowView;
    }



}
