import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SheetsCheckInOut {

    private static final String SPREADSHEET_ID = "1y1ylV-xV7MDMCY3kSSi0Qf-7-1X9uH0QQVnH9_BWyI4";
    private static final String ROSTER_SHEET = "0000";  // 名簿シート
    private static final String RECORD_SHEET = "2025";  // 受付記録シート
    private static final String ID = "A001"; // ★ここを入力 IDベースで処理

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Sheets service = SheetsQuickstart.getSheetsService();

        // 日付・時刻の取得
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // 名簿（0000シート）から対象IDの情報を取得
        ValueRange rosterResponse = service.spreadsheets().values()
                .get(SPREADSHEET_ID, ROSTER_SHEET + "!A:F")
                .execute();
        List<List<Object>> roster = rosterResponse.getValues();

        String name = null;
        String school = null;
        String grade = null;

        for (List<Object> row : roster) {
            if (row.size() > 0 && ID.equals(row.get(0).toString())) {
                name = row.size() > 1 ? row.get(1).toString() : "";
                school = row.size() > 2 ? row.get(2).toString() : "";
                grade = row.size() > 3 ? row.get(3).toString() : "";
                break;
            }
        }

        if (name == null) {
            System.out.println("エラー：指定されたIDのデータが名簿に存在しません。");
            return;
        }

        // 記録シート（2025）から入室済みかを確認
        ValueRange recordResponse = service.spreadsheets().values()
                .get(SPREADSHEET_ID, RECORD_SHEET + "!A:G")
                .execute();
        List<List<Object>> records = recordResponse.getValues();

        int targetRow = -1;

        for (int i = 1; i < records.size(); i++) {
            List<Object> row = records.get(i);
            if (row.size() >= 6 &&
                ID.equals(row.get(0).toString()) &&
                today.equals(row.get(1).toString()) &&
                !row.get(5).toString().isEmpty() &&
                (row.size() < 7 || row.get(6).toString().isEmpty())) {
                targetRow = i + 1; // 1-based
                break;
            }
        }

        if (targetRow != -1) {
            // 退室時間の記録
            String cell = "G" + targetRow;
            List<List<Object>> writeData = List.of(List.of(nowTime));
            ValueRange body = new ValueRange().setValues(writeData);
            service.spreadsheets().values()
                    .update(SPREADSHEET_ID, RECORD_SHEET + "!" + cell, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            System.out.println("退室時間を記録しました！");
        } else {
            // 新規入室の記録
            List<List<Object>> newRow = List.of(
                    List.of(ID, today, name, grade, school, nowTime, "")
            );
            ValueRange appendBody = new ValueRange().setValues(newRow);
            service.spreadsheets().values()
                    .append(SPREADSHEET_ID, RECORD_SHEET + "!A:G", appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            System.out.println("入室時間を記録しました！");
        }
    }
}
