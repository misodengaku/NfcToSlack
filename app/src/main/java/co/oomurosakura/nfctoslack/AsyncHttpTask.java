package co.oomurosakura.nfctoslack;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by miso on 2018/03/08.
 */

public class AsyncHttpTask extends AsyncTask<String, Void, String> {
    private Listener listener;

    // 非同期処理
    @Override
    protected String doInBackground(String... params) {

        String urlSt = params[0];

        HttpURLConnection con = null;
        String result = null;
        String body = params[1];

        try {
            // URL設定
            URL url = new URL(urlSt);

            // HttpURLConnection
            con = (HttpURLConnection) url.openConnection();

            // request POST
            con.setRequestMethod("POST");

            // no Redirects
            con.setInstanceFollowRedirects(false);

            con.setRequestProperty("Content-type", "application/json");

            // データを書き込む
            con.setDoOutput(true);

            // 時間制限
            con.setReadTimeout(10000);
            con.setConnectTimeout(20000);

            // 接続
            con.connect();

            // POSTデータ送信処理
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write( body.getBytes("UTF-8") );
                out.flush();
                Log.d("debug","flush");
            } catch (IOException e) {
                // POST送信エラー
                e.printStackTrace();
                result="POST送信エラー";
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // レスポンスを受け取る処理等
                result="HTTP_OK";
            }
            else{
                result="status="+String.valueOf(status);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return result;
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(String result);
    }
}
