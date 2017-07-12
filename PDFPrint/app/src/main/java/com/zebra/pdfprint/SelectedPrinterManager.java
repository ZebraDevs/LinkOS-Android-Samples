package com.zebra.pdfprint;

import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectedPrinterManager {
    private static DiscoveredPrinter currentlySelectedPrinter = null;

    public static DiscoveredPrinter getSelectedPrinter() {
        return currentlySelectedPrinter;
    }

    public static void setSelectedPrinter(DiscoveredPrinter selectedPrinter) {
        SelectedPrinterManager.currentlySelectedPrinter = selectedPrinter;
    }

    public static String getModelName() {
        String modelName = "";
        if (currentlySelectedPrinter != null) {
            modelName = parseProductName(currentlySelectedPrinter.getDiscoveryDataMap().get("PRODUCT_NAME"));
        }
        return modelName;
    }

    public static String getFriendlyName() {
        String friendlyName = "";
        if (currentlySelectedPrinter != null) {
            friendlyName = currentlySelectedPrinter.getDiscoveryDataMap().get("SYSTEM_NAME");
        }
        return friendlyName;
    }

    public static String getMacAddress() {
        String macAddress = "";
        if (currentlySelectedPrinter != null) {
            macAddress = currentlySelectedPrinter.getDiscoveryDataMap().get("HARDWARE_ADDRESS");
        }
        return macAddress;
    }

    public static String getFirmwareVersion() {
        String firmwareVersion = "";
        if (currentlySelectedPrinter != null) {
            firmwareVersion = currentlySelectedPrinter.getDiscoveryDataMap().get("FIRMWARE_VER");
        }
        return firmwareVersion;
    }

    public static String getSerialNumber() {
        String serialNumber = "";
        if (currentlySelectedPrinter != null) {
            serialNumber = currentlySelectedPrinter.getDiscoveryDataMap().get("SERIAL_NUMBER");
        }
        return serialNumber;
    }

    public static String getIpAddress() {
        String ipAddress = "";
        if (currentlySelectedPrinter != null) {
            ipAddress = getSelectedPrinter().address;
        }
        return ipAddress;
    }

    private static String parseProductName(String productName) {
        Pattern pattern = Pattern.compile("\\s*ztc\\s+(.+?)[\\-|\\s].+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(productName);

        if (matcher.find()) {
            productName = matcher.group(1).trim();
        }

        return productName;
    }
}
