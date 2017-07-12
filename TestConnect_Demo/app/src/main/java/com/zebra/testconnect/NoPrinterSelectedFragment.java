/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NoPrinterSelectedFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.no_printer_selected_fragment, container, false);

        TextView learnMoreButton = (TextView) rootView.findViewById(R.id.learnMore);
        learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.print_touch_uri)));
                startActivity(browserIntent);
            }
        });

        return rootView;
    }
}
