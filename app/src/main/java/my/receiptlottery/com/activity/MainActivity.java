package my.receiptlottery.com.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import me.dm7.barcodescanner.zbar.ZBarScannerView;
import my.receiptlottery.com.CaptureReceiptLotteryTask;
import my.receiptlottery.com.R;
import my.receiptlottery.com.controller.MainController;
import my.receiptlottery.com.util.Utility;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    private ZBarScannerView scannerView;
    private MainController mainController;

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Utility.CAPTURE_TASK_COMPLETED)) {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    showDialog(message);
                }
            }
            else if (intent.getAction().equals(Utility.SHOW_DIALOG)) {
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

        mainController = new MainController(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, new IntentFilter(Utility.CAPTURE_TASK_COMPLETED));
        registerReceiver(mReceiver, new IntentFilter(Utility.SHOW_DIALOG));

        this.mainController.initialScannerView(scannerView);
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
            Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
            intent.putExtra("url", "http://invoice.etax.nat.gov.tw/");
            startActivity(intent);
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

    private void showDialog(String message){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton("確定",this)
                .create();
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        scannerView.resumeCameraPreview(this.mainController);
        dialog.dismiss();
    }
}
