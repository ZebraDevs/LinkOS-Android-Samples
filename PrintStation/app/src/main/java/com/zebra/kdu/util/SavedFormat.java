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
package com.zebra.kdu.util;

public class SavedFormat {
	public static final String TABLE_NAME = "saved_formats_table";
	
	public static final String _ID = "id";
	public static final String FORMAT_DRIVE = "format_drive";
	public static final String FORMAT_NAME = "format_name";
	public static final String FORMAT_EXTENSION = "format_extension";
	public static final String SOURCE_PRINTER_NAME = "source_printer_name";
	public static final String TIMESTAMP = "timestamp";
	public static final String FORMAT_TEXT = "format_text";

	public Long id;
	public String formatDrive;
	public String formatName;
	public String formatExtension;
	public String sourcePrinterName;
	public Long timestamp;
	public String formatText;
	
	public SavedFormat(Long id, String formatDrive, String formatName, String formatExtension, String sourcePrinterName, Long timestamp, String formatText) {
		this.id = id;
		this.formatDrive = formatDrive;
		this.formatName = formatName;
		this.formatExtension = formatExtension;
		this.sourcePrinterName = sourcePrinterName;
		this.timestamp = timestamp;
		this.formatText = formatText;
	}
}
