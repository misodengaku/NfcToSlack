package co.oomurosakura.nfctoslack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by miso on 2018/03/08.
 */

public final class CardStore {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    public CardStore() {}

    /* Inner class that defines the table contents */
    public static class CardEntry implements BaseColumns {
        public static final String TABLE_NAME = "cards";
        public static final String COLUMN_CARD_ID = "card_id";
        public static final String COLUMN_WEBHOOK_URL = "webhook_url";
        public static final String COLUMN_CHANNEL_NAME= "channel_name";
        public static final String COLUMN_POST_TEXT= "post_text";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_CARDS =
            "CREATE TABLE " + CardEntry.TABLE_NAME + " (" +
                    CardEntry._ID + " INTEGER PRIMARY KEY," +
                    CardEntry.COLUMN_CARD_ID+ TEXT_TYPE + COMMA_SEP +
                    CardEntry.COLUMN_WEBHOOK_URL+ TEXT_TYPE + COMMA_SEP +
                    CardEntry.COLUMN_CHANNEL_NAME+ TEXT_TYPE + COMMA_SEP +
                    CardEntry.COLUMN_POST_TEXT + TEXT_TYPE + " )";

    private static final String SQL_DELETE_CARDS =
            "DROP TABLE IF EXISTS " + CardEntry.TABLE_NAME;

    public class CardStoreDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Cards.db";

        public CardStoreDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_CARDS);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_CARDS);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}