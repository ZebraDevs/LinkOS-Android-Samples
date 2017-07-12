/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AvailableDemosFragment extends Fragment {

    public static final String FRAGMENT_TAG = "available_demos_fragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.available_demos_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (!TabletConfigurationHelper.isTabletConfiguration(view.getContext()) || TabletConfigurationHelper.isPortraitConfiguration(view.getContext())) {
            view.findViewById(R.id.priceTagFileInfo).setVisibility(View.GONE);
            view.findViewById(R.id.receiptFileInfo).setVisibility(View.GONE);
        }

        LinearLayout priceTagLayout = (LinearLayout) view.findViewById(R.id.printPriceTagLayout);
        if (priceTagLayout != null) {
            priceTagLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadPrintDemoFragment(new PrintDemoShelfLabel(getActivity().getApplicationContext()));
                }
            });
        }

        LinearLayout receiptLayout = (LinearLayout) view.findViewById(R.id.printReceiptLayout);
        if (receiptLayout != null) {
            receiptLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadPrintDemoFragment(new PrintDemoReceipt(getActivity().getApplicationContext()));
                }
            });
        }
    }

    private void loadPrintDemoFragment(PrintDemo printDemo) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(PrintDemoFragment.PRINT_DEMO_KEY, printDemo);

        PrintDemoFragment demoFragment = new PrintDemoFragment();
        demoFragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.fragmentScrollViewLayout, demoFragment, PrintDemoFragment.FRAGMENT_TAG).commit();
    }

}
