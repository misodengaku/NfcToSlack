package co.oomurosakura.nfctoslack;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class NfcTapActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private AsyncHttpTask httpTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_tap);

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(getApplication(), NfcRegistrationActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //▼NFCの機能判定
        //NFC機能なし機種
        if(mNfcAdapter == null){
            Toast.makeText(getApplicationContext(), "no Nfc feature", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //NFC通信OFFモード
        if(!mNfcAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), "off Nfc feature", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //▲NFCの機能判定

        //NFCを見つけたときに反応させる
        //PendingIntent→タイミング（イベント発生）を指定してIntentを発生させる
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);

        //タイミングは、タグ発見時とする。
        IntentFilter[] intentFilter = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };

        //反応するタグの種類を指定。
        String[][] techList = new String[][]{
                {
                        android.nfc.tech.NfcA.class.getName(),
                        android.nfc.tech.NfcB.class.getName(),
                        android.nfc.tech.IsoDep.class.getName(),
                        android.nfc.tech.MifareClassic.class.getName(),
                        android.nfc.tech.MifareUltralight.class.getName(),
                        android.nfc.tech.NdefFormatable.class.getName(),
                        android.nfc.tech.NfcV.class.getName(),
                        android.nfc.tech.NfcF.class.getName(),
                }
        };
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techList);
    }



    //NFCをタッチした後の処理
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        //▼▼▼▼ここから
        String action = intent.getAction();
        if(TextUtils.isEmpty(action)){
            Toast.makeText(getApplicationContext(), "action is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) && !action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) && !action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            Toast.makeText(getApplicationContext(), "no tag", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "tag detected", Toast.LENGTH_SHORT).show();


        byte[] ids = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        StringBuilder tagIdBuilder = new StringBuilder("");
        for (int i=0; i<ids.length; i++) {
            tagIdBuilder.append(String.format("%02x", ids[i] & 0xff));
        }

        String tagId = tagIdBuilder.toString();



        CardStore.CardStoreDbHelper cardStoreDbHelper = new CardStore().new CardStoreDbHelper(getBaseContext());
        SQLiteDatabase db = cardStoreDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cards WHERE card_id=?;", new String[]{tagId});
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();

            String webhook_url = cursor.getString(2);
            String channel_name = cursor.getString(3);
            String post_text = cursor.getString(4);

            String httpBody = String.format("{\"text\": \"%s\",\"channel\": \"#%s\",\"username\": \"NFCToSlack\",\"icon_emoji\": \":nfc:\", \"as_user\": true}", post_text, channel_name);

            httpTask = new AsyncHttpTask();
            httpTask.setListener(createListener());
            httpTask.execute(webhook_url, httpBody);

//            Toast.makeText(getApplicationContext(), httpBody, Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), webhook_url, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "登録されていないカードです", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();

//        if(action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
//            finish();
//        }
        finish();

    }


    private AsyncHttpTask.Listener createListener() {
        return new AsyncHttpTask.Listener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getApplicationContext(), "投稿が完了しました" , Toast.LENGTH_SHORT).show();
            }
        };
    }
}
