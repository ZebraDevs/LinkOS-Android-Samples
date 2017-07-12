/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import java.io.Serializable;

public class PrintDemoItem implements Serializable {

    private String description;
    private String price;
    private String upc;

    public PrintDemoItem() {
    }

    public PrintDemoItem(String price, String description, String upc) {
        this.description = description;
        this.price = price;
        this.upc = upc;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getUpc() {
        return upc;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }
}
