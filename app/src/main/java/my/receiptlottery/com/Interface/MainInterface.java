package my.receiptlottery.com.Interface;

import me.dm7.barcodescanner.zbar.ZBarScannerView;

public interface MainInterface extends ZBarScannerView.ResultHandler {
    void initialScannerView(ZBarScannerView scannerView);
}
