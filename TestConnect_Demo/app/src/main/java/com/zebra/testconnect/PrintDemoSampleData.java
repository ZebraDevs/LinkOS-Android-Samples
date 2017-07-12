/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import java.io.Serializable;
import java.util.List;

public class PrintDemoSampleData implements Serializable {
    private List<PrintDemoItem> itemList;

    public PrintDemoSampleData() {
    }

    public List<PrintDemoItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<PrintDemoItem> itemList) {
        this.itemList = itemList;
    }
}
