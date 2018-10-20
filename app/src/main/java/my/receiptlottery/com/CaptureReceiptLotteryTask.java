package my.receiptlottery.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import my.receiptlottery.com.util.Utility;

/**
 * 利用 Jsoup 解析財政部統一發票對獎網頁並取得中獎號碼
 */
public class CaptureReceiptLotteryTask extends AsyncTask <Context,Void,String> {
    private final String TAG = CaptureReceiptLotteryTask.class.getCanonicalName();
    private Context context;// for error handle

    @Override
    protected String doInBackground(Context[] objects) {

        String returnStirng = "";
        this.context = objects[0];
        if (this.context == null) {
            return "Error: does not contain context";
        }
        try {
            String urlString = "http://invoice.etax.nat.gov.tw/";

            Document doc = Jsoup.parse(new URL(urlString), 3000);

            Elements prizes = doc.select("div#area1>table>tbody>tr>td>span[class=t18Red]");// 本期中獎號碼
            //Elements last_prizes = doc.select("div#area2>table>tbody>tr>td>span[class=t18Red]");// 上一期中獎號碼

            SharedPreferences sharedPreferences = context.getSharedPreferences(Utility.SHAREDPREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (prizes.isEmpty())
                return "Error: 無法取得對獎號碼";


            for(Element prize:prizes) {
                Log.d(TAG, prize.text());
            }

            editor.putString("特別獎", prizes.get(0).text());
            editor.putString("特獎", prizes.get(1).text());

            String[] splits = prizes.get(2).text().split("、");
            editor.putString("頭獎1",splits[0]);
            editor.putString("頭獎2",splits[1]);
            editor.putString("頭獎3",splits[2]);

            splits = prizes.get(3).text().split("、");
            editor.putString("增開六獎1",splits[0]);
            editor.putString("增開六獎2",splits[1]);

            editor.commit();

            return "lottery number updated";

        } catch (MalformedURLException e){
            // new URL failed
            returnStirng = "MalformedURLException";
            e.printStackTrace();
        } catch (IOException e){
            // Jsoup parse failed
            returnStirng = "IOException";
            e.printStackTrace();
        } catch (Exception e){
            returnStirng = e.getMessage();
            e.printStackTrace();
        }
        return "Error: " + returnStirng;
    }

    @Override
    protected void onPostExecute(String s) {
        Intent intent = new Intent(Utility.CAPTURE_TASK_COMPLETED);
        if (s.contains("Error")){
            intent.putExtra("message", "更新對獎號碼失敗");
        }
        else {
            intent.putExtra("message", "對獎號碼已更新");
        }
        context.sendBroadcast(intent);
    }
}

