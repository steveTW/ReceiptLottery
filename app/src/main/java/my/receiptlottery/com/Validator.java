package my.receiptlottery.com;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *  特別獎  同期統一發票收執聯8位數號碼與特別獎號碼相同者獎金1,000萬元
 *  特獎  同期統一發票收執聯8位數號碼與特獎號碼相同者獎金200萬元
 *  頭獎  同期統一發票收執聯8位數號碼與頭獎號碼相同者獎金20萬元 (3組)
 *  二獎  同期統一發票收執聯末7 位數號碼與頭獎中獎號碼末7 位相同者各得獎金4萬元
 *  三獎  同期統一發票收執聯末6 位數號碼與頭獎中獎號碼末6 位相同者各得獎金1萬元
 *  四獎  同期統一發票收執聯末5 位數號碼與頭獎中獎號碼末5 位相同者各得獎金4千元
 *  五獎  同期統一發票收執聯末4 位數號碼與頭獎中獎號碼末4 位相同者各得獎金1千元
 *  六獎  同期統一發票收執聯末3 位數號碼與 頭獎中獎號碼末3 位相同者各得獎金2百元
 *  增開六獎  同期統一發票收執聯末3位數號碼與增開六獎號碼相同者各得獎金2百元
 */

public class Validator {

    private SharedPreferences sharedPreferences;
    // 驗證 QRCode 內容
    /*public static boolean validate(String contentString) {

        // 發票資訊77碼
        if (contentString.length()!=77)
            return false;

        // 檢查前53碼是否只含數字
        if (!(contentString.substring(0,53).matches("[0-9]+, A-Za-z")))
            return false;

        return  true;
    }*/

    public Validator(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Utility.SHAREDPREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
    }

    public String validatePrize(String number) {
        if (sharedPreferences.getString("特別獎","").equals("")) {
            return "沒有對獎資料";
        }
        String result = checkForPrize(number);

        if (!result.equals(""))
            return result;
        else
            return "沒有中獎";
    }

    // 比對特獎號碼
    private String checkForLotterySpecialPrize(String receiptNumber) {
        String prizeNumber = sharedPreferences.getString("特獎", "");
        if (prizeNumber.equals(receiptNumber) && !prizeNumber.equals(""))
            return "特獎";
        else
            return checkForLotteryPrize(receiptNumber);
    }

    // 比對頭獎以下號碼
    private String checkForPrize(String receiptNumber) {

        String prizeNumber = sharedPreferences.getString("特別獎","");
        if (prizeNumber.equals(receiptNumber) && !prizeNumber.equals(""))
            return "特別獎";
        else
            return checkForLotterySpecialPrize(receiptNumber);
    }

    private String checkForLotteryPrize(String receiptNumber) {

        String[] prizeNumbers = {sharedPreferences.getString("頭獎1", ""),
                sharedPreferences.getString("頭獎2", ""),
                sharedPreferences.getString("頭獎3", "")};

        for (String compareString : prizeNumbers) {// 比對頭獎號碼
            int compareIndex = 0;
            for (int i = 7; i >=0 ;i--) {// 從最後碼開始比對
                if (compareString.charAt(i) != receiptNumber.charAt(i)) {
                    compareIndex = 7 - i;
                    break;
                }
                if (i == 0) // 8位數號碼與頭獎號碼相同
                    compareIndex = 8;
            }

            if (compareIndex > 2) {
                switch (compareIndex) {
                    case 3:
                        return "六獎";
                    case 4:
                        return "五獎";
                    case 5:
                        return "四獎";
                    case 6:
                        return "三獎";
                    case 7:
                        return "二獎";
                    case 8:
                        return "頭獎";
                }
            }
        }
        return checkForLastPrize(receiptNumber);
    }

    private String checkForLastPrize(String receiptNumber) {
        String[] prizeNumbers = {sharedPreferences.getString("增開六獎1", ""),
                sharedPreferences.getString("增開六獎2", "")
        };

        String lastNumber = receiptNumber.substring(5);
        for (String compareString : prizeNumbers) {
            if (compareString.equals(lastNumber))
                return "增開六獎";
        }
        return "";
    }

}
