
package nitin.com.myapplication;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, double lat, double lng) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.NAME, name);
        contentValue.put(DatabaseHelper.LAT, lat);
        contentValue.put(DatabaseHelper.LNG, lng);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }
    public Cursor fetchNearest(double location_lat,double location_lng) {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.LAT, DatabaseHelper.LNG };
        //Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
                                                //SELECT * AS distance FROM items ORDER BY ABS(location_lat - lat) + ABS(location_lng - lng) ASC
        Cursor cursor = database.rawQuery( "SELECT * FROM "+DatabaseHelper.TABLE_NAME+" ORDER BY (("+location_lat+" - "+DatabaseHelper.LAT+")*("+location_lat+" - "+DatabaseHelper.LAT+")) + (("+location_lng+" - "+DatabaseHelper.LNG+")*("+location_lng+" - "+DatabaseHelper.LNG+")) ASC LIMIT 19", null );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.LAT, DatabaseHelper.LNG };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, double lat, double lng) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NAME, name);
        contentValues.put(DatabaseHelper.LAT, lat);
        contentValues.put(DatabaseHelper.LNG, lng);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }
    public void deleteAll(){
        database.execSQL("delete from "+ DatabaseHelper.TABLE_NAME);
    }

}
