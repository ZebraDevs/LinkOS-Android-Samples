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

package com.zebra.android.devdemo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class UIHelper {
    ProgressDialog loadingDialog;
    Activity activity;

    public UIHelper(Activity activity) {
        this.activity = activity;
    }

    public void showLoadingDialog(final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    loadingDialog = ProgressDialog.show(activity, "", message, true, false);
                }
            });
        }
    }

    public void updateLoadingDialog(final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    loadingDialog.setMessage(message);
                }
            });
        }
    }

    public boolean isDialogActive() {
        if (loadingDialog != null) {
            return loadingDialog.isShowing();
        } else {
            return false;
        }
    }

    public void dismissLoadingDialog() {
        if (activity != null && loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(activity).setMessage(errorMessage).setTitle("Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void showErrorDialogOnGuiThread(final String errorMessage) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(activity).setMessage(errorMessage).setTitle("Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            dismissLoadingDialog();
                        }
                    }).create().show();
                }
            });
        }
    }

}
