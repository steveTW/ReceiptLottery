package my.receiptlottery.com.controller;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import my.receiptlottery.com.Interface.LotteryGetInterface;
import my.receiptlottery.com.Interface.MainInterface;
import my.receiptlottery.com.util.Utility;
import my.receiptlottery.com.util.Validator;

public class MainController implements LotteryGetInterface, MainInterface {

    private Context context;

    public MainController(Context context){
        this.context = context;
    }


    @Override
    public void handleResult(Result result) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this.context, notification);
            r.play();
        } catch (Exception e) {}

        Map<String,String> resultMap = parseResultContent(result.getContents());
        if (resultMap==null){
            sendDialogMessage("讀取不到發票號碼，如果可能請再試一次");
        }
        else {
            Validator validator = new Validator(this.context);
            String validateResult = validator.validatePrize(resultMap.get("receipt_number"));

            sendDialogMessage("發票號碼: " + resultMap.get("receipt_number") + "\n" + validateResult);
        }
    }

    // 分析掃描到的 QRCode 內容
    private Map<String,String> parseResultContent(String resultContent){
        if (resultContent!=null) {
            Log.d("parseResultContent", "resultcontent: " + resultContent);
            if (resultContent.contains("http")){
            }
            else if (resultContent.startsWith("**")){
                // right side
            }
            else if (!resultContent.equals("")) {
                String[] splits = resultContent.split(":");
                if (splits[0].length()==77) {
                    try {
                        int charset = Integer.parseInt(splits[4]);
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                        //return null;
                    }
                    Log.d("parseResultContent", "could be receipt");
                    Map<String, String> parseResult = new HashMap<>();
                    parseResult.put("receipt_number", resultContent.substring(2, 10));
                    parseResult.put("date", resultContent.substring(10, 17));
                    parseResult.put("machine_code", resultContent.substring(17, 21));
                    parseResult.put("pre_tax", resultContent.substring(21, 29));
                    parseResult.put("with_tax", resultContent.substring(29, 37));
                    parseResult.put("buyer_tax_id", resultContent.substring(37, 45));
                    parseResult.put("seller_tax_id", resultContent.substring(45, 53));
                    parseResult.put("validate_code", resultContent.substring(53, 77));
                    Log.d("parseResultContent", "parse result: " + parseResult.toString());
                    return parseResult;
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

    private void sendDialogMessage(String message){
        Intent intent = new Intent(Utility.SHOW_DIALOG);
        intent.putExtra(Utility.SHOW_DIALOG_EXTRA, message);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    @Override
    public void initialScannerView(ZBarScannerView scannerView) {
        if (scannerView != null) {
            scannerView.setResultHandler(this);
            scannerView.setLaserEnabled(false);

            scannerView.setAutoFocus(true);
            ArrayList<BarcodeFormat> formats = new ArrayList<>();
            formats.add(BarcodeFormat.QRCODE);
            scannerView.setFormats(formats);
            //scannerView.setFlash(true);
            scannerView.startCamera(-1);
        }
        else {
            sendDialogMessage("zbar scanner view initialize failed");
        }
    }
}
