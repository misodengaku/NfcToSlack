package co.oomurosakura.nfctoslack;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NfcRegistrationActivity extends AppCompatActivity {


    private NfcAdapter mNfcAdapter;
    private AsyncHttpTask httpTask;
    private CardStore.CardStoreDbHelper cardStoreDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_registration);

        cardStoreDbHelper = new CardStore().new CardStoreDbHelper(getBaseContext());
        db = cardStoreDbHelper.getWritableDatabase();

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView cardIDView = findViewById(R.id.cardIDView);
                EditText webhookTextView = findViewById(R.id.webhookTextView);
                EditText channelTextView = findViewById(R.id.channelTextView);
                EditText postTextView = findViewById(R.id.postTextView);

                String card_id = cardIDView.getText().toString();
                String webhook_url = webhookTextView.getText().toString();
                String channel_name = channelTextView.getText().toString();
                String post_text = postTextView.getText().toString();


                Cursor cursor = db.rawQuery("SELECT * FROM cards WHERE card_id=?;", new String[]{card_id});
                if (cursor.getCount() != 0)
                {
                    try {
                        db.execSQL("UPDATE cards SET webhook_url=?,channel_name=?,post_text=? WHERE card_id=?;", new String[]{webhook_url, channel_name, post_text, card_id});
                    }catch (SQLException e)
                    {
                        Log.e("ERROR", e.toString());
                    }
                    Toast.makeText(getApplicationContext(), "Update complete!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }



                try {
                    db.execSQL("INSERT INTO cards (card_id,webhook_url,channel_name,post_text) VALUES (?,?,?,?);", new String[]{card_id, webhook_url, channel_name, post_text});
                }catch (SQLException e)
                {
                    Log.e("ERROR", e.toString());
                }

                Toast.makeText(getApplicationContext(), "Registration complete!", Toast.LENGTH_SHORT).show();
                finish();
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
        if(!mNfcAdapter.isEnabled()){Toast.makeText(getApplicationContext(), "off Nfc feature", Toast.LENGTH_SHORT).show();
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

        String action = intent.getAction();
        if(TextUtils.isEmpty(action)){
            return;
        }

        if(!action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            return;
        }

        //成功！と表示してみる
        byte[] ids = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        StringBuilder tagIdBuilder = new StringBuilder("");
        for (int i=0; i<ids.length; i++) {
            tagIdBuilder.append(String.format("%02x", ids[i] & 0xff));
        }
        String tagId = tagIdBuilder.toString();


        Cursor cursor = db.rawQuery("SELECT * FROM cards WHERE card_id=?;", new String[]{tagId});
        if (cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            EditText webhookTextView = findViewById(R.id.webhookTextView);
            EditText channelTextView = findViewById(R.id.channelTextView);
            EditText postTextView = findViewById(R.id.postTextView);

            webhookTextView.setText(cursor.getString(2));
            channelTextView.setText(cursor.getString(3));
            postTextView.setText(cursor.getString(4));

        }

        TextView cardIDView = findViewById(R.id.cardIDView);
        cardIDView.setText(tagId.toString());


        Toast.makeText(getApplicationContext(), "カード情報を取り込みました", Toast.LENGTH_SHORT).show();
    }

}
