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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

public class ConnectedPrinterFragment extends Fragment {

    private static boolean refreshIconSpinning;
    private static Bundle resultData;
    private static String friendlyName = "";

    public static boolean isRefreshIconSpinning() {
        return refreshIconSpinning;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.connected_printer, container, false);

        final ImageView refreshButton = (ImageView) rootView.findViewById(R.id.refreshButton);

        if (refreshButton != null) {
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrinterStatusHelper.updatePrinterSelectedFragment(getActivity());
                    animateRefreshIcon(refreshButton);
                }
            });
        }

        if (refreshIconSpinning) {
            animateRefreshIcon(refreshButton);
        }

        updateStatusInfo(rootView);
        return rootView;
    }

    public void updateStatusInfo() {
        updateStatusInfo(getView());
    }

    private void updateStatusInfo(View rootView) {
        if (resultData != null) {
            int iconId = R.drawable.icon_error_dash_red;
            @SuppressWarnings("unchecked")
            Map<String, String> statusMap = (Map<String, String>) resultData.getSerializable("PrinterStatusMap");

            if (statusMap != null) {
                friendlyName = statusMap.get("friendlyName");
                if (statusMap.get("isReadyToPrint").equals("true")) {
                    iconId = R.drawable.icon_checkmark_green;
                }
            }

            ((TextView) rootView.findViewById(R.id.dashboardFriendlyName)).setText(friendlyName);
            ((ImageView) rootView.findViewById(R.id.dashboardPrinterStatus)).setImageResource(iconId);
        }
    }

    public static Map<String, String> getStatusMap() {
        Map<String, String> statusMap = null;

        if (resultData != null) {
            statusMap = (Map<String, String>) resultData.getSerializable("PrinterStatusMap");
        }

        return statusMap;
    }

    public static void setFragmentArguments(Bundle resultData) {
        ConnectedPrinterFragment.resultData = resultData;
    }

    public void stopRefreshIcon() {
        refreshIconSpinning = false;
    }

    private void animateRefreshIcon(final ImageView refreshButton) {
        if (refreshButton != null) {
            final Animation rotation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.anim_rotate);
            rotation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (!refreshIconSpinning) {
                        refreshButton.clearAnimation();
                        refreshButton.setEnabled(true);
                    }
                }
            });

            refreshButton.setEnabled(false);
            refreshIconSpinning = true;
            refreshButton.startAnimation(rotation);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRefreshIcon();
    }
}