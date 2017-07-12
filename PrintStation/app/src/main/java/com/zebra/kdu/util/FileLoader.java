/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *  
 * The information contained herein is the confidential and the exclusive property of
 * ZIH Corp. This document, and the information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose without the express
 * written consent of ZIH Corp. 
 * 
 * Copyright ZIH Corp. 2012 
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.kdu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FileLoader {

    public static byte[] toByteArray(String fullPath, int maxSize) {

        if (fullPath == null)
            return null;

        File f = new File(fullPath);
        long fileSize = f.length();
        int intSize = 0;

        if (fileSize < maxSize)
            intSize = (int) fileSize;
        else
            return null;

        byte[] data = new byte[intSize];

        try {
            FileInputStream is = new FileInputStream(f);
            if (intSize != is.read(data)) {
                is.close();
                return null;
            }
            is.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return data;
    }

    public static String toUtf8String(String fullPath) {
        return (toUtf8String(fullPath, Integer.MAX_VALUE));
    }
    
    public static String toUtf8String(String fullPath, int maxSize) {
        byte[] data = toByteArray(fullPath, maxSize);
        if (data == null)
            return "";
        try {
            return new String(data, "utf8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
