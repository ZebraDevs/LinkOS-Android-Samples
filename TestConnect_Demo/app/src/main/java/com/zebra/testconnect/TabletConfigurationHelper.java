/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.content.Context;
import android.content.res.Configuration;

public class TabletConfigurationHelper {
    public static boolean isTabletConfiguration(Context context) {
        Configuration config = context.getResources().getConfiguration();

        if (config.smallestScreenWidthDp >= 600) { // Make sure this integer always equals the number in the layout-sw###dp directory name
            return true;
        }

        return false;
    }

    public static boolean isPortraitConfiguration(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        return false;
    }
}
