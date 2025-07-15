import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SheetsAppendExample {
    private static final String APPLICATION_NAME = "Google Sheets API Java";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SPREADSHEET_ID = "1y1ylV-xV7MDMCY3kSSi0Qf-7-1X9uH0QQVnH9_BWyI4"; // あなたのID
    private static final String RANGE = "2025!A:F"; // シート名＋範囲

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // HTTP通信の準備
        final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Sheets APIサービス生成（認証処理は既存のgetCredentialsを流用）
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, SheetsQuickstart.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 現在時刻と日付を自動取得
        String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm").format(new Date());

        // 固定データで1行作成
        List<Object> row = Arrays.asList(
                "田中 太郎",          // 名前
                "東京第一高校",        // 高校
                "3年",               // 学年
                date,               // 日付
                time,               // 入室時間
                ""                  // 退出時間は空欄
        );

        // 書き込み処理
        ValueRange body = new ValueRange().setValues(List.of(row));
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        System.out.println("スプレッドシートに1行追加しました！");
    }
}
