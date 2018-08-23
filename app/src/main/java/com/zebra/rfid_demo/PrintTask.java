package com.zebra.rfid_demo;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public class PrintTask extends AsyncTask<Void, Void, Void> {

    private String SAMPLE_TEMPLATE_ZPL = "\u0010CT~~CD,~CC^~CT~\r\n" +
            "^XA\r\n" +
            "^DFE:SampleRe.ZPL^FS\r\n" +
            "~TA000^MNN^MTT^POI^PMN,0^JMA^PR6,6^MD15^LRN^CI0\r\n" +
            "^MMT\r\n" +
            "^PW574\r\n" +
            "^LL0508\r\n" +
            "^LS0\r\n" +
            "^FO0,0^GFA,01536,01536,00012,:Z64:\r\n" +
            "eJzd0jtyHCEQBuDBBDhwGZ9g8RF0AGnwUXQEhQpYw5buYcW+xWwpcKgjCJUuwJYTpB0P7m6ezu3ERF9NMf13A9P0jxZ3g/3gOHitEoOV41u1XrqV75aRp/Zv9zu+tj2Cbd0u3VXLxXZ7/VBzZVAvtTcR5Tm0JsV5KYaSqfWQWGo9WJdSGfiLXmwqP1wrb1MZ/kaCS9EovHlffl25Nx+KN/DHWh4sa/k7cKmvH7rVC7iEyZ/gpxwmzuDHHMYT+EeejKHrxOjSp0MfsxeNzgFeofOmING5fiSH1r88xtZ/dWLd1pE19b9ckzFB+c9kTJBhImOCiNmRzr+bbdNXeYia7iBNFnbm+7DOumq9QL1iBROAaWIZIIJniyjQ1B28mQAnQ9PABQc8mTzBFLBamaC+B/x00ax8t+xbcIK2+tOmCfpKg63rLpkloPuPgMF8MBs8jb4ZfDH40/R/L/2a7lfr6amjNxsV2p662cnvZrPSWbNnv9sZQ+bkyyvyMcAec4VnKo5Q06yGfEj30cZbtDxgzXCLl68Y/Gu9rVbW03ub0ftA73D+/vZt3p/e1tqPeQ2Xzb/8DE7PUN/syX9//QZjPS7w:EBC1\r\n" +
            "^FO192,352^GFA,03072,03072,00032,:Z64:\r\n" +
            "eJztlTGrE0EQx2d3fa6YR2LnFSFnbfOeghAl5PIR0gTbgI1lREQLMYf2YvkKQbGS5zew8F33OlvbQxEDggSbhODdOrObbO72blNYSgZym73fzez8Z+YSgL3t7T+yYz+6TJf7XsxUjNe5D/PoJ3Lx2+ufMbzIsQ+LpUD/ZurjrZghbyc+ngP53/ZxNgeBy4PYd3wKLVy++XiIuWGUcw/mfU5cvPSlh3VrIz/0Hw9dLM9rD5exbl2za7bMTRPPhsmWC5fj2TDGZWK2oRv/CcakFFKz7dekT+U7Tsx24qb/1Zz5MLbbMsf7Etd7hsukhpOEH4ZHbvpyLeGz2T5z0yNflM5+mWi5y9smZ2bKG1aGOMBPSjOodyp1OcXGnIXWLb+76cFNI4G6yPnZJxdT60gC6ZaLZa+Od8B0OVTpssLHAORkysAq6VHu2MF1GcTbGs5IM50xWTf/1rDAEzMxPf2obr5SxSoM4ICWp7oMdFL4ZXjwx2KecnKiuovn2Hx+pN5xXuiicRIngHFEAvLxHEpTkAJpbp2QBBk31IxuFnSOBT17GJD8i2y6LtB2CtMjOqvfJfmRWq3vdi2f6JLPAy3fdj+w/AZFFGlAmXRs2tuXNaJUZHwF2KBQNcuN1IjfQfk4AJu7Tftkz1wC3UFrF+w3PbBL5J0it/7yXKeHvDQ61j96keqXpgel0bH+S3p/EbVpiKv5Y+QcGhii2YqLvL3hMYSDKR1Y/t3Y1A8Vt1SOsmVutUfYhNX2UTbSgWzphVJzUf0v2Rz/Qc3eqOprSDa6q9Qiw9m7OnTI9TOlbfXx9LTGMZs+WmSL0au6oAAN9HsfX6qHmH9W+U0pGo/ya7s4y3ZRLNxsNy/3pWrN3Xhv/2p/AQSttgk=:6AA4\r\n" +
            "^FO196,358^GB253,99,8^FS\r\n" +
            "^FT110,36^A0N,31,31^FH\\^FDZebra Technologies Corp.^FS\r\n" +
            "^FT109,70^A0N,17,16^FH\\^FD3 Overlook Point, Lincolnshire, IL, 60029^FS\r\n" +
            "^FT109,91^A0N,17,16^FH\\^FDAppForum - London - 2015^FS\r\n" +
            "^FT109,112^A0N,17,16^FH\\^FDSample Receipt^FS\r\n" +
            "^FT8,164^A0N,23,24^FH\\^FDProduct:^FS\r\n" +
            "^FT197,163^A0N,23,24^FH\\^FDPrice    :^FS\r\n" +
            "^FT290,476^A0N,13,12^FH\\^FDSign Here^FS\r\n" +
            "^FT342,163^A0N,23,24^FH\\^FDQTY:^FS\r\n" +
            "^FO8,260^GB549,0,7^FS\r\n" +
            "^FO8,135^GB550,0,6^FS\r\n" +
            "^FT8,331^A0N,23,24^FH\\^FDTOTAL  :^FS\r\n" +
            "^FT8,245^A0N,23,24^FH\\^FN16\"FProduct2\"^FS\r\n" +
            "^FT197,244^A0N,23,24^FH\\^FN22\"FPrice2\"^FS\r\n" +
            "^FT8,208^A0N,23,24^FH\\^FN13\"FProduct\"^FS\r\n" +
            "^FT351,241^A0N,23,24^FH\\^FN15\"Fqty2\"^FS\r\n" +
            "^FT197,206^A0N,23,24^FH\\^FN19\"FPrice\"^FS\r\n" +
            "^FT350,205^A0N,23,24^FH\\^FN11\"Fqty\"^FS\r\n" +
            "^FT198,331^A0N,23,24^FH\\^FN20\"FTotal\"^FS\r\n" +
            "^FT9,300^A0N,23,24^FH\\^FDTAX     :^FS\r\n" +
            "^BY1,3,23^FT433,240^BCN,,N,N,N,A\r\n" +
            "^FN14\"ProductBarcode2\"^FS\r\n" +
            "^FT198,297^A0N,23,24^FH\\^FN21\"FTax\"^FS\r\n" +
            "^BY2,3,23^FT6,438^BCN,,N,N,N,A\r\n" +
            "^FN24\"\"^FS\r\n" +
            "^BY2,3,23^FT7,398^BCN,,N,N,N,A\r\n" +
            "^FN23\"\"^FS\r\n" +
            "^BY1,3,23^FT470,431^BCN,,N,N,N,A\r\n" +
            "^FN18\"TruckBarcode\"^FS\r\n" +
            "^BY1,3,23^FT470,391^BCN,,N,N,N,A\r\n" +
            "^FN17\"DriverBarcode\"^FS\r\n" +
            "^BY1,3,23^FT432,206^BCN,,N,N,N,A\r\n" +
            "^FN12\"ProductBarcode\"^FS\r\n" +
            "^FT448,127^BQN,2,5\r\n" +
            "^FDMA,www.zebra.com^FS\r\n" +
            "^XZ\r\n";

    private String TEMPLATE_ZPL = "\u0010CT~~CD,~CC^~CT~\r\n" +
            "^XA\r\n" +
            "^DFE:SampleRe.ZPL^FS\r\n" +
            "~TA000^MNN^MTT^POI^PMN,0^JMA^PR6,6^MD15^LRN^CI0\r\n" +
            "^MMT\r\n" +
            "^PW574\r\n" +
            "^LL0508\r\n" +
            "^LS0\r\n" +
            "^FO0,0^GFA,01536,01536,00012,:Z64:\r\n" +
            "eJzd0jtyHCEQBuDBBDhwGZ9g8RF0AGnwUXQEhQpYw5buYcW+xWwpcKgjCJUuwJYTpB0P7m6ezu3ERF9NMf13A9P0jxZ3g/3gOHitEoOV41u1XrqV75aRp/Zv9zu+tj2Cbd0u3VXLxXZ7/VBzZVAvtTcR5Tm0JsV5KYaSqfWQWGo9WJdSGfiLXmwqP1wrb1MZ/kaCS9EovHlffl25Nx+KN/DHWh4sa/k7cKmvH7rVC7iEyZ/gpxwmzuDHHMYT+EeejKHrxOjSp0MfsxeNzgFeofOmING5fiSH1r88xtZ/dWLd1pE19b9ckzFB+c9kTJBhImOCiNmRzr+bbdNXeYia7iBNFnbm+7DOumq9QL1iBROAaWIZIIJniyjQ1B28mQAnQ9PABQc8mTzBFLBamaC+B/x00ax8t+xbcIK2+tOmCfpKg63rLpkloPuPgMF8MBs8jb4ZfDH40/R/L/2a7lfr6amjNxsV2p662cnvZrPSWbNnv9sZQ+bkyyvyMcAec4VnKo5Q06yGfEj30cZbtDxgzXCLl68Y/Gu9rVbW03ub0ftA73D+/vZt3p/e1tqPeQ2Xzb/8DE7PUN/syX9//QZjPS7w:EBC1\r\n" +
            "^FO250,380^XGSignature.GRF,1,1^FS\r\n" +
            "^FO196,358^GB253,99,8^FS\r\n" +
            "^FT110,36^A0N,31,31^FH\\^FDZebra Technologies Corp.^FS\r\n" +
            "^FT109,70^A0N,17,16^FH\\^FD3 Overlook Point, Lincolnshire, IL, 60029^FS\r\n" +
            "^FT109,91^A0N,17,16^FH\\^FDAppForum - London - 2015^FS\r\n" +
            "^FT109,112^A0N,17,16^FH\\^FDSample Receipt^FS\r\n" +
            "^FT8,164^A0N,23,24^FH\\^FDProduct:^FS\r\n" +
            "^FT197,163^A0N,23,24^FH\\^FDPrice    :^FS\r\n" +
            "^FT290,476^A0N,13,12^FH\\^FDSign Here^FS\r\n" +
            "^FT342,163^A0N,23,24^FH\\^FDQTY:^FS\r\n" +
            "^FO8,260^GB549,0,7^FS\r\n" +
            "^FO8,135^GB550,0,6^FS\r\n" +
            "^FT8,331^A0N,23,24^FH\\^FDTOTAL  :^FS\r\n" +
            "^FT8,245^A0N,23,24^FH\\^FN16\"FProduct2\"^FS\r\n" +
            "^FT197,244^A0N,23,24^FH\\^FN22\"FPrice2\"^FS\r\n" +
            "^FT8,208^A0N,23,24^FH\\^FN13\"FProduct\"^FS\r\n" +
            "^FT351,241^A0N,23,24^FH\\^FN15\"Fqty2\"^FS\r\n" +
            "^FT197,206^A0N,23,24^FH\\^FN19\"FPrice\"^FS\r\n" +
            "^FT350,205^A0N,23,24^FH\\^FN11\"Fqty\"^FS\r\n" +
            "^FT198,331^A0N,23,24^FH\\^FN20\"FTotal\"^FS\r\n" +
            "^FT9,300^A0N,23,24^FH\\^FDTAX     :^FS\r\n" +
            "^BY1,3,23^FT433,240^BCN,,N,N,N,A\r\n" +
            "^FN14\"ProductBarcode2\"^FS\r\n" +
            "^FT198,297^A0N,23,24^FH\\^FN21\"FTax\"^FS\r\n" +
            "^BY2,3,23^FT6,438^BCN,,N,N,N,A\r\n" +
            "^FN24\"\"^FS\r\n" +
            "^BY2,3,23^FT7,398^BCN,,N,N,N,A\r\n" +
            "^FN23\"\"^FS\r\n" +
            "^BY1,3,23^FT470,431^BCN,,N,N,N,A\r\n" +
            "^FN18\"TruckBarcode\"^FS\r\n" +
            "^BY1,3,23^FT470,391^BCN,,N,N,N,A\r\n" +
            "^FN17\"DriverBarcode\"^FS\r\n" +
            "^BY1,3,23^FT432,206^BCN,,N,N,N,A\r\n" +
            "^FN12\"ProductBarcode\"^FS\r\n" +
            "^FT448,127^BQN,2,5\r\n" +
            "^FDMA,www.zebra.com^FS\r\n" +
            "^XZ\r\n";

    private MainActivity mainActivity;
    private Bitmap signatureBitmap;

    public PrintTask(MainActivity mainActivity, Bitmap signatureBitmap) {
        this.mainActivity = mainActivity;
        this.signatureBitmap = signatureBitmap;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DiscoveredPrinter discoveredPrinter = SelectedPrinterManager.getSelectedPrinter();

        if (discoveredPrinter != null) {
          Connection connection = new BluetoothConnection(discoveredPrinter.address);


            try {
           connection.open();
            //    Connection connection2 = new BluetoothStatusConnection(discoveredPrinter.address);
            //     connection2.open();
                ZebraPrinter zebraPrinter = ZebraPrinterFactory.getInstance(connection);
                String snackbarMsg = "";

                String printerLanguage = SGD.GET("device.languages",connection);
                mainActivity.showSnackbar(snackbarMsg);
                if (printerLanguage.contains("line_print")) {
                     snackbarMsg = mainActivity.getString(R.string.Language_is_LINE_PRINT);

                    } else if (printerLanguage.contains("cpcl")) {
                            snackbarMsg = mainActivity.getString(R.string.Language_is_CPCL);

                    } else if (printerLanguage.contains("zpl")){
                       snackbarMsg = mainActivity.getString(R.string.Language_is_ZPL);

                    }

               // snackbarMsg = printerLanguage.toString();
                mainActivity.showSnackbar(snackbarMsg);
                SGD.SET("device.languages", "zpl", connection);
                PrinterStatus printerStatus = zebraPrinter.getCurrentStatus();
                byte[] configReceipt = TEMPLATE_ZPL.getBytes();
                if (printerStatus.isReadyToPrint) {
                    snackbarMsg = mainActivity.getString(R.string.print_successful);
                    try {
                     //   zebraPrinter.calibrate();
                        connection.write(configReceipt);
                        zebraPrinter.storeImage("Signature.GRF", new ZebraImageAndroid(signatureBitmap), -1, -1);
                        zebraPrinter.printStoredFormat("E:SampleRe.ZPL", mainActivity.mergeVariables());
                    } catch (ZebraIllegalArgumentException e) {
                        try {
                     //       mainActivity.showSnackbar(mainActivity.getString(R.string.Language_is_LINE_PRINT));
                            SGD.SET("device.languages", "zpl", connection);
                        } catch (ConnectionException ex) {
                            mainActivity.showSnackbar(mainActivity.getString(R.string.connection_error));
                        }
                    }

                } else if (printerStatus.isPaused) {
                    snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.printer_paused);
                } else if (printerStatus.isHeadOpen) {
                    snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.head_open);
                } else if (printerStatus.isPaperOut) {
                    snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.paper_out);
                } else {
                    snackbarMsg = mainActivity.getString(R.string.print_failed) + " " + mainActivity.getString(R.string.cannot_print);
                }

                mainActivity.showSnackbar(snackbarMsg);
            } catch (ZebraPrinterLanguageUnknownException e) {
                try {
                    SGD.SET("device.languages", "zpl", connection);
                } catch (ConnectionException ex) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.connection_error));
                }

            } catch (ConnectionException e) {
                try {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.Language_is_LINE_PRINT));
                    SGD.SET("device.languages", "zpl", connection);
                } catch (ConnectionException ex) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.connection_error));
                }
            } finally {
                try {
                    SGD.SET("device.languages", "zpl", connection);
                    connection.close();
                } catch (ConnectionException e) {

                }
            }
        }

        return null;
    }
}
