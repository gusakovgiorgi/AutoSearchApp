package net.gusakov.newnettiauto.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import net.gusakov.newnettiauto.database.AutoDatabaseHelper;

import timber.log.Timber;

import static net.gusakov.newnettiauto.Constants.ProviderConstants.AUTHORITY;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.AUTO_CONTENT_ITEM_TYPE;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.AUTO_CONTENT_TYPE;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.AUTO_CONTENT_URI;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.AUTO_PATH;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.DEFAULT_SORT_ORDER;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.URI_AUTOS;
import static net.gusakov.newnettiauto.Constants.ProviderConstants.URI_AUTOS_ID;
import static net.gusakov.newnettiauto.Constants.DBConstants.*;

public class AutoContentProvider extends ContentProvider {

    private static final String TAG = AutoContentProvider.class.getSimpleName();
    private static final UriMatcher mUriMatcher;
    private AutoDatabaseHelper mAutoDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public AutoContentProvider() {
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Timber.d("delete");
        SQLiteDatabase db=mAutoDatabaseHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)){
            case URI_AUTOS:
                Timber.d("delete all data");
                count=db.delete(DB_TABLE_AUTO,where,whereArgs);
                break;
            case URI_AUTOS_ID:
                String id=uri.getLastPathSegment();
                Timber.d("delete id="+id);
                count = db.delete(DB_TABLE_AUTO, TB_AUTO_ID + "=" + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        Timber.d("getType");
        switch (mUriMatcher.match(uri)){
            case URI_AUTOS:
                return AUTO_CONTENT_TYPE;
            case URI_AUTOS_ID:
                return AUTO_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        Timber.d("insert");
        // Validate the requested uri
        if (mUriMatcher.match(uri) != URI_AUTOS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db=mAutoDatabaseHelper.getWritableDatabase();
        long rowID = db.insert(DB_TABLE_AUTO, null, values);
        Timber.d("inserted data row = "+rowID);
        Uri resultUri = ContentUris.withAppendedId(AUTO_CONTENT_URI, rowID);

        // nootificate ContentResolver that data on resultUri changes
        getContext().getContentResolver().notifyChange(resultUri, null);
        Timber.d("notificate about changes");
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        Timber.i("initializing content provider");
        mAutoDatabaseHelper=new AutoDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Timber.d("query");
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DB_TABLE_AUTO);

        switch (mUriMatcher.match(uri)){
            case URI_AUTOS:
                Timber.d("URI_AUTOS");
                break;
            case URI_AUTOS_ID:
                String id=uri.getLastPathSegment();
                queryBuilder.appendWhere(TB_AUTO_ID + "=" + id);
                Timber.d("URI_AUTOS_ID = "+id);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)){
            orderBy=DEFAULT_SORT_ORDER;
        }else{
            orderBy=sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mAutoDatabaseHelper.getReadableDatabase();
        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        Timber.d("update");
        SQLiteDatabase db=mAutoDatabaseHelper.getWritableDatabase();
        int count=0;
        switch (mUriMatcher.match(uri)){
            case URI_AUTOS:
                Timber.v(new IllegalArgumentException("incorrect uri "+uri),"no id selected");
                break;
            case URI_AUTOS_ID:
                String id=uri.getLastPathSegment();
                count = db.update(DB_TABLE_AUTO, values, TB_AUTO_ID + "=" + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, AUTO_PATH, URI_AUTOS);
        mUriMatcher.addURI(AUTHORITY, AUTO_PATH + "/#", URI_AUTOS_ID);
    }
}
