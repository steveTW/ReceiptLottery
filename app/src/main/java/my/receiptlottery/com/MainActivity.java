package my.receiptlottery.com;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler, DialogInterface.OnClickListener{

    private ZBarScannerView scannerView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Utility.CAPTURE_TASK_COMPLETED)) {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    showDialog(message);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View content_main = findViewById(R.id.fragment);
        scannerView = (ZBarScannerView) content_main.findViewById(R.id.main_zbarscan);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CaptureReceiptLotteryTask task = new CaptureReceiptLotteryTask();
                //task.execute(MainActivity.this);
            }
        });

        // 檢察程式權限
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        if (Build.VERSION.SDK_INT >= 23) { // check if need to show requestPermission
            if (!checkRequestPermission(permissions)) {
                requestPermissions(permissions, 3);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, new IntentFilter(Utility.CAPTURE_TASK_COMPLETED));

        // 初始設定 QRCode Scanner
        if (scannerView != null) {
            scannerView.setResultHandler(this);
            scannerView.setLaserEnabled(false);

            scannerView.setAutoFocus(true);
            ArrayList<BarcodeFormat> formats = new ArrayList<>();
            formats.add(BarcodeFormat.QRCODE);
            scannerView.setFormats(formats);
            //scannerView.setFlash(true);
        }
        scannerView.startCamera(-1);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);

        scannerView.stopCameraPreview();
        scannerView.stopCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_update) {
            Log.d("tag", "action_update");
            CaptureReceiptLotteryTask task = new CaptureReceiptLotteryTask();
            task.execute(MainActivity.this);
        }

        if (id == R.id.action_etax) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkRequestPermission(String[] permissions){
        for (String permission : permissions){
            if(ContextCompat.checkSelfPermission(MainActivity.this,permission)!=PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    // implement ZBar ResultHandler
    @Override
    public void handleResult(Result result) {
        //String barcodeFaormat = result.getBarcodeFormat().getName();

        // play notification sound
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {}

        Map<String,String> resultMap = parseResultContent(result.getContents());
        if (resultMap==null){
            showDialog("讀取不到發票號碼，如果可能請再試一次");
        }
        else {
            Validator validator = new Validator(MainActivity.this);
            String validateResult = validator.validatePrize(resultMap.get("receipt_number"));

            showDialog("發票號碼: " + resultMap.get("receipt_number") + "\n" + validateResult);
        }

    }

    private void showDialog(String message){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton("確定",this)
                .create();
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        scannerView.resumeCameraPreview(MainActivity.this);
        dialog.dismiss();
    }

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
}
