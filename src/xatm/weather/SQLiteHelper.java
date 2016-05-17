package xatm.weather;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
    private final static String DBNAME = "info.db";
    private final String BUSCARDTABLENAME = "buscardnumbers";
    private final String CELLTABLENAME = "cellinfo";
    private final String CELL2GEOTABLENAME = "cell2geo";

    private static SQLiteHelper sqlitehelper = null;
    public synchronized static SQLiteHelper getInstance(Context context) {
        if(sqlitehelper == null) {
            sqlitehelper = new SQLiteHelper(context.getApplicationContext());
        }
        return sqlitehelper;
    }

    public SQLiteHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+BUSCARDTABLENAME+" (buscardnumber TEXT PRIMARY KEY)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+CELLTABLENAME+" (timestamp INTEGER PRIMARY KEY,mcc TEXT,mnc TEXT,lac INTEGER,cid INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+CELL2GEOTABLENAME+" (mcc TEXT, mnc TEXT, lac INTEGER, cid INTEGER, longitude REAL, latitude REAL, address TEXT, roads TEXT, PRIMARY KEY(mcc, mnc, lac, cid))");
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public ArrayList<String> readbuscardtable() {
        ArrayList<String> buscardnumbers = new ArrayList<String>();
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM "+BUSCARDTABLENAME, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    buscardnumbers.add(cursor.getString(cursor.getColumnIndex("buscardnumber")));
                }
            }
            cursor.close();
            Log.d("weather", "sqlitehelper read buscard successfully ");
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper read buscard failed ");
        }
        finally {
            return buscardnumbers;
        }
    }

    public void appendbuscardtable(String buscardnumberstring) {
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO "+BUSCARDTABLENAME+" VALUES (?)", new Object[]{buscardnumberstring});
            Log.d("weather", "sqlitehelper append buscard  buscardnumberstring:" + buscardnumberstring);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper append buscard failed ");
        }
    }

    public void deletebuscardtable(String buscardnumberstring) {
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM "+BUSCARDTABLENAME+" WHERE buscardnumber=(?)", new Object[]{buscardnumberstring});
            Log.d("weather", "sqlitehelper delete buscard  buscardnumberstring:" + buscardnumberstring);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper delete buscard failed ");
        }
    }

    public void appendcelltable(Long ts, String MCC, String MNC, int Lac, int Cid) {
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO "+CELLTABLENAME+" VALUES (?, ?, ?, ?, ?)", new Object[]{ts, MCC, MNC, Lac, Cid});
            Log.d("weather", "sqlitehelper append cell ts: " + ts + " mcc:" + MCC + " mnc:" + MNC + " lac:" + Lac + " cid:" + Cid);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper append cell failed ");
        }
    }

    public Object[] readcell2geotable(String MCC, String MNC, int Lac, int Cid) {
        double longitude = 0.0;
        double latitude = 0.0;
        String address = "";
        String roads = "";
        Object[] result = null;

        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM "+CELL2GEOTABLENAME+" WHERE mcc='"+MCC+"' and mnc='"+MNC+"' and lac="+Lac+" and cid="+Cid, null);
            if(cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                    latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                    address = cursor.getString(cursor.getColumnIndex("address"));
                    roads = cursor.getString(cursor.getColumnIndex("roads"));
                }
                result = new Object[]{longitude, latitude, address, roads};
            }
            cursor.close();
            Log.d("weather", "sqlitehelper read cell2geo  MCC:"
                                         + MCC + " MNC:" + MNC + " lac:" + Lac + " cid:" + Cid
                                         + " longitude:"+longitude+" latitude:"+latitude+" address:"+address + " roads:" + roads);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper read cell2geo failed ");
        }
        finally {
            return result;
        }
    }

    public void appendcell2geotable(String MCC, String MNC, int Lac, int Cid, double longitude, double latitude, String address, String roads) {
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO "+CELL2GEOTABLENAME +" VALUES (?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{MCC, MNC, Lac, Cid, longitude, latitude, address, roads});
            Log.d("weather", "sqlitehelper append cell2geo  MCC:"
                                         + MCC + " MNC:" + MNC + " lac:" + Lac + " cid:" + Cid
                                         + " longitude:"+longitude+" latitude:"+latitude+" address:"+address + " roads:" + roads);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper append cell2geo failed ");
        }
    }

    public void updatecell2geotable(String MCC, String MNC, int Lac, int Cid, double longitude, double latitude, String address, String roads) {
        SQLiteDatabase db = sqlitehelper.getWritableDatabase();
        try {
            db.execSQL("UPDATE "+CELL2GEOTABLENAME +" SET longitude="+longitude+", latitude="+latitude+", address='"+address+"', roads='"+roads+"' WHERE mcc='"+MCC+"' and mnc='"+MNC+"' and lac="+Lac+" and cid="+Cid);
            Log.d("weather", "sqlitehelper update cell2geo  MCC:"
                                         + MCC + " MNC:" + MNC + " lac:" + Lac + " cid:" + Cid
                                         + " longitude:"+longitude+" latitude:"+latitude+" address:"+address + " roads:" + roads);
        }
        catch(SQLException e) {
            Log.e("weather", "sqlitehelper update cell2geo failed ");
        }
    }
}
