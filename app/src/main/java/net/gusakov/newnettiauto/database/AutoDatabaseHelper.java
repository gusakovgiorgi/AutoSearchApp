package net.gusakov.newnettiauto.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import timber.log.Timber;

import static net.gusakov.newnettiauto.Constants.DBConstants.*;

public class AutoDatabaseHelper extends SQLiteOpenHelper {

    public AutoDatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
        Timber.d("Create AutoDatabaseHelper constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("Executing database create sql");
        db.execSQL(DB_CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w( "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(DB_DROP_STRING);
        onCreate(db);
    }
}
