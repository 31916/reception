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
    private static final String RANGE = "2025!A:F"; // シート名と範囲
    private static final String NAME = "田中 太郎";
    private static final String SCHOOL = "東京第一高校";
    private static final String GRADE = "3年";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Sheets service = SheetsQuickstart.getSheetsService();

        // 日付・時刻の取得
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // データを取得
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE)
                .execute();
        List<List<Object>> values = response.getValues();

        int targetRow = -1;

        // 既存データの中から一致する入室済みデータを探す
        if (values != null) {
            for (int i = 1; i < values.size(); i++) { // A2行目からチェック
                List<Object> row = values.get(i);
                if (row.size() >= 5 &&
                        NAME.equals(row.get(0)) &&
                        SCHOOL.equals(row.get(1)) &&
                        GRADE.equals(row.get(2)) &&
                        today.equals(row.get(3)) &&
                        !row.get(4).toString().isEmpty() &&
                        (row.size() < 6 || row.get(5).toString().isEmpty())) {
                    targetRow = i + 1; // 1-based row number
                    break;
                }
            }
        }

        if (targetRow != -1) {
            // 退室処理
            String cell = "F" + targetRow;
            List<List<Object>> writeData = List.of(List.of(time));
            ValueRange body = new ValueRange().setValues(writeData);
            service.spreadsheets().values()
                    .update(SPREADSHEET_ID, "2025!" + cell, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            System.out.println("退室時間を記録しました！");
        } else {
            // 入室処理
            List<List<Object>> newRow = List.of(
                    List.of(NAME, SCHOOL, GRADE, today, time, "")
            );
            ValueRange appendBody = new ValueRange().setValues(newRow);
            service.spreadsheets().values()
                    .append(SPREADSHEET_ID, RANGE, appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            System.out.println("入室時間を記録しました！");
        }
    }
}
