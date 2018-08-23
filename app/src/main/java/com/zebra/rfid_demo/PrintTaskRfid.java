package com.zebra.rfid_demo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.HashMap;

public class PrintTaskRfid extends AsyncTask<Void, Void, Void> {
    static final String TAG_Print_Task="PRINT TASK";
   Context mcontext;
    byte[] configReceipt = null;
    byte[] templateLabel = null;
    View.OnClickListener myView;
   View myMainView;
   String [] lastRecordDB;


    private static int  myLabel_type=0;
    private String ZPL_PRICE_LABEL =  "\u0010CT~~CD,~CC^~CT~\r\n" +
            "^XA\r\n"+
            "^DFE:LabelSt.ZPL^FS\r\n" +
            "~TA000^MNM^MTT^POI^PMN,0^JMA^PR6,6^MD15^LRN^CI0\r\n" +
            "~JSB\r\n"+
            "^MMT\r\n"+
            "^PW479\r\n"+
            "^LL0406\r\n"+
            "^LS0\r\n"+
            "^FO0,0^GFA,01536,01536,00016,:Z64:\r\n"+
            "eJzt0jEOwjAMBdCEDNnqG7QcIRJLkApF4kKMHYMYuvlMRQxco1EuEKaWDUb/iI21f3tKbEdWlFrzf/QI3OgJTKWvaHtHnyM6vtD5je4PAdx2eF5ThucRLdLGMlw3N96Cn5WT1jMfYeDMu1G64/2Edhc5v2Ev6w2xk/VmYC/rzYOdtI7sk2igM7skGyyVT7KgrVySG6hJNXIDRAFshwAbM/fRwoMiWmc5/Zv+hG6Rqi5MhW0oXPQv/ptSpYv5a9b8zgfMPj/n:3663\r\n"+
            "^BY3,2,65^FT87,277^BUN,,Y,N\r\n"+
            "^FN10\"UPC\"^FS\r\n"+
            "^FT56,152^A0N,28,28^FH\\^FDSTYLE^FS\r\n"+
            "^FT312,152^A0N,28,28^FH\\^FDCOLOR^FS\r\n"+
            "^FT57,192^A0N,23,24^FH\\^FN18\"style\"^FS\r\n"+
            "^FT314,192^A0N,23,24^FH\\^FN17\"color\"^FS\r\n"+
            "^FT193,152^A0N,28,28^FH\\^FDSIZE^FS\r\n"+
            "^FT193,192^A0N,23,24^FH\\^FN19\"size\"^FS\r\n"+
            "^FT27,347^A0N,28,28^FH\\^FDSUGGESTED^FS\r\n"+
            "^FT27,381^A0N,28,28^FH\\^FDRETAIL PRICE^FS\r\n"+
            "^FT268,362^A0N,28,28^FH\\^FN13\"price\"^FS\r\n"+
            "^FT103,34^A0N,28,28^FH\\^FDZEBRA RETAIL STORE^FS\r\n"+
            "^FT126,60^A0N,17,16^FH\\^FDISV ELECTRONICS DEPARTMENT^FS\r\n"+
            "^FT245,96^A0N,20,19^FH\\^FDPROVIDER^FS\r\n"+
            "^FT332,96^A0N,20,19^FH\\^FN12\"provider\"^FS\r\n"+
            "^FT51,96^A0N,20,19^FH\\^FDPRODUCT ^FS\r\n"+
            "^FT136,96^A0N,20,19^FH\\^FN11\"product\"^FS\r\n"+
            "^PQ1,0,1,Y^XZ";


    private String ZPL_RFID_LABEL = null;
    private String ZPL_RETAIL_LABEL = null;


    private String TEMPLATE_ZPL = "\u0010CT~~CD,~CC^~CT~\r\n" +
            "^XA\r\n" +
            "^DFE:SampleRe.ZPL^FS\r\n" +
            "~TA000^MNW^MTT^POI^PMN,0^JMA^PR6,6^MD15^LRN^CI0\r\n" +
            "~JSA\r\n"+
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

    //private MainActivity mainActivity;


    public PrintTaskRfid(View v, View.OnClickListener view2, Context context, int type_label ) {


    mcontext = context;
    myMainView = v;
    myView = view2;

    myLabel_type = type_label;

    }

    public PrintTaskRfid(Context context, String [] lastRecord, int type_label, final LinearLayout layoutScan) {


        mcontext = context;

        lastRecordDB = lastRecord;
        myLabel_type = type_label;
        final LinearLayout myLayout = layoutScan;

       ZPL_RFID_LABEL = "\u0010^XA\r\n"+
               "~TA000^MNN^MTT^POI^PMN,0^JMA^PR6,6^MD15^LRN^CI0\r\n" +
                "^RU\r\n"+
                "^MMT\r\n"+
                "^PW639\r\n"+
                "^LL0190\r\n" +
                "^FO448,0^GFA,04480,04480,00020,:Z64:\r\n"+
                "eJztlsFt3TAMhuUYqG/VBlWRCTpB1FE6QgcILD/00GNGqoKiyDErOBvIyCE+GGZJyYlISUAvvQQwE+ABn//nn6Qo6Sl1xhnvPdYG2xsMWmwqwBdkvmC3Nes2ZLNkPRpA+Dfr8GUlo5dVzIWa2bVik9lK1nm9l6yfB6hY6GEq6hhWTLBgGh3mogfo4ELB0MGuRU/RwWzFeqCD3jfJ0GFotKCHss8WEyyF1AKQg/CVEgRp8g0TdCCT+Y4JOpDDgQ7ERCHoMLlO2qLD5JSQ4ZhZ1HnJJmRK1gGedLIOmEkn63CBdLIOu5JO1mE20oFYEb1HnePOA5Bud9y5h1+kM9wZX0U6LVyIqX0Q3XJR14tu2ai74rLJJB1nXkddz7+KUxB1vA4aU9LxOuIaoY7XEdcIdbyOuEao4x2Ma6QKNpla53Wlc/OQdEN2saFPuiErzdodutxB3EeHLneQHidd7iAdG4furQ1Y7qsutwEfHyy3AVf7YDxB5S6R8QSV8+TFGCZIwyIYJohflwwTpGHp+abDBPHrNDes/eCpPsloU9Mjvolt6KhNjm92dCCd5SONDiM94tNGp076yIEOn+hTTvSkVRngP1QME6yYLY+mlGAVujzWysxyglV01XWkGteRijdDFf89wcYd2rXu0NZd6xrspsFMg31ssKHBrhqsa7Az3l1cXePfZ3Wt6P8IA49wBxdIB0UKi+QOnmAVTEe2sWF1kS2w813ntNe/lyDYiOzHEkZ+m21JNw4FQ90NY92K7IF0j54xyiWMOrM+ELuHvcE2/XN+ZcOc6lhNZnqOvhBMPieReP1neZlNPk8juywvwWZmJmT3yzNndorve15tPp9TT5dlG7s3dvQv1Owp7Lf5xwkWRXWEcVONU+aMM85g8Rck9ADb:729E\r\n"+
                "^FT37,58^A0N,26,26^FDZebra ZQ520 RFID^FS\r\n"+
                "^FT37,87^A0N,26,26^FN1^FS\r\n"+
                "^BY2,2,66^FT75,168^BUN,,Y,N^FD"+lastRecordDB[0]+"^FS \r\n"+
                "^RB96,8,3,3,24,20,38\r\n"+
                "^RFW,E^FD48,1,5,"+ lastRecordDB[0].substring(1, 6)+","+lastRecordDB[0].substring(6,11)+",#S^FS\r\n"+
                "^FN1^RFR,E\r\n"+
                "^PH\r\n"+
                "^XZ\r\n";

       ZPL_RETAIL_LABEL  = "^XA\r\n"+
                "^XFE:LabelSt.ZPL\r\n"+
                "^FN10^FD"+lastRecordDB[0]+"^FS\r\n"+
                "^FN11^FD"+lastRecordDB[1]+"^FS\r\n"+
                "^FN12^FD"+lastRecordDB[2]+"^FS\r\n"+
                "^FN13^FD$"+lastRecordDB[3]+"^FS\r\n"+
                "^FN17^FD"+lastRecordDB[6]+"BLUE^FS\r\n"+
                "^FN18^FD"+lastRecordDB[7]+"Indoor^FS\r\n"+
                "^FN19^FD"+lastRecordDB[8]+"^FS\r\n"+
                "^XZ";


    }

    public HashMap<Integer, String> mergeVariables_rfid() {


        HashMap<Integer, String> variableMap = new HashMap<>();
        variableMap.put(10,lastRecordDB[0]);
        variableMap.put(11,lastRecordDB[1]);
        variableMap.put(12,lastRecordDB[2]);
        variableMap.put(13,lastRecordDB[3]);
        variableMap.put(17,lastRecordDB[6]);
        variableMap.put(18,lastRecordDB[7]);
        variableMap.put(19,lastRecordDB[8]);
        return variableMap;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DiscoveredPrinter discoveredPrinter = SelectedPrinterManager.getSelectedPrinter();
        Log.d(TAG_Print_Task,"PRINT RFID LABEL: " +  lastRecordDB[0]+"," +lastRecordDB[1]+","+lastRecordDB[3]);
        if (discoveredPrinter != null) {
         Connection connection = new BluetoothConnection(discoveredPrinter.address);
            try {
                connection.open();
                ZebraPrinter zebraPrinter = ZebraPrinterFactory.getInstance(connection);
                SGD.SET("device.languages", "zpl", connection);
                PrinterStatus printerStatus = zebraPrinter.getCurrentStatus();

                if(myLabel_type==0) {
                    configReceipt = ZPL_RFID_LABEL.getBytes();
                }else if(myLabel_type==1){
                    configReceipt = ZPL_PRICE_LABEL.getBytes();
                    templateLabel = ZPL_RETAIL_LABEL.getBytes();
                }else if(myLabel_type==2){
                    configReceipt=ZPL_RFID_LABEL.getBytes();
                }
                if (printerStatus.isReadyToPrint) {
                try {
                        connection.write(configReceipt);
                 if(myLabel_type==1) {
                            zebraPrinter.printStoredFormat("E:LabelSt.ZPL", mergeVariables_rfid());
                        }
                        connection.close();
                    } catch (Exception e) {
                        try {
                          SGD.SET("device.languages", "zpl", connection);
                        } catch (ConnectionException ex) {

                        }
                    }

                } else if (printerStatus.isPaused) {

                } else if (printerStatus.isHeadOpen) {

                } else if (printerStatus.isPaperOut) {

                } else {

                }


            } catch (ZebraPrinterLanguageUnknownException e) {
                try {
                    SGD.SET("device.languages", "zpl", connection);
                } catch (ConnectionException ex) {

                }

            } catch (ConnectionException e) {
                try {

                    SGD.SET("device.languages", "zpl", connection);
                } catch (ConnectionException ex) {

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
