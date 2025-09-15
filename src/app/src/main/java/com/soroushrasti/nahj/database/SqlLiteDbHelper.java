package com.soroushrasti.nahj.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SqlLiteDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ger-nahj.db";
    private static final String DB_PATH_SUFFIX = "/databases/";
    static Context mCtx;
    public static final String COL_TITLE_EN = "title_en";
    public static final String COL_CNT_EN = "cnt_en";

    public SqlLiteDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCtx = context;
    }

    private String getLangPref() {
        SharedPreferences sp = mCtx.getSharedPreferences("com.soroushrasti.nahj.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sp.getString("LANG", "fa");
    }

    private void ensureEnglishColumns(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("PRAGMA table_info(nahj)", null)) {
            boolean hasTitleEn = false;
            boolean hasCntEn = false;
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                if (COL_TITLE_EN.equalsIgnoreCase(name)) hasTitleEn = true;
                if (COL_CNT_EN.equalsIgnoreCase(name)) hasCntEn = true;
            }
            if (!hasTitleEn) {
                try { db.execSQL("ALTER TABLE nahj ADD COLUMN " + COL_TITLE_EN + " TEXT DEFAULT ''"); } catch (SQLException ignore) {}
            }
            if (!hasCntEn) {
                try { db.execSQL("ALTER TABLE nahj ADD COLUMN " + COL_CNT_EN + " TEXT DEFAULT ''"); } catch (SQLException ignore) {}
            }
        }
    }

    public ArrayList<Model> getDetails(int setCat, int setId, boolean setFav) {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureEnglishColumns(db);
        String lang = getLangPref();
        ArrayList<Model> modelList = new ArrayList<>();
        Cursor cursor;
        if (setId != 0) {
            cursor = db.rawQuery("SELECT * FROM nahj WHERE id = " + setId + " LIMIT 1", null);
        } else {
            if (setFav) {
                cursor = db.rawQuery("SELECT * FROM nahj WHERE fav = 1", null);
            } else {
                cursor = (setCat != 0) ? db.rawQuery("SELECT * FROM nahj WHERE cat = " + setCat + " ORDER BY num ASC", null) : db.rawQuery("SELECT * FROM nahj ORDER BY num ASC", null);
            }
        }
        if (cursor != null) {
            int idxId = cursor.getColumnIndex("id");
            int idxCat = cursor.getColumnIndex("cat");
            int idxNum = cursor.getColumnIndex("num");
            int idxTitle = cursor.getColumnIndex("title");
            int idxCnt = cursor.getColumnIndex("cnt");
            int idxFav = cursor.getColumnIndex("fav");
            int idxTitleEn = cursor.getColumnIndex(COL_TITLE_EN);
            int idxCntEn = cursor.getColumnIndex(COL_CNT_EN);
            while (cursor.moveToNext()) {
                String pickedTitle = cursor.getString(idxTitle);
                String pickedCnt = cursor.getString(idxCnt);
                if ("en".equals(lang) && idxTitleEn != -1 && idxCntEn != -1) {
                    String tEn = cursor.getString(idxTitleEn);
                    String cEn = cursor.getString(idxCntEn);
                    if (tEn != null && !tEn.isEmpty()) pickedTitle = tEn;
                    if (cEn != null && !cEn.isEmpty()) pickedCnt = cEn;
                }
                Model count = new Model(cursor.getInt(idxId), cursor.getInt(idxCat), cursor.getString(idxNum), pickedTitle, pickedCnt, cursor.getInt(idxFav));
                count.setLanguage(lang);
                modelList.add(count);
            }
            cursor.close();
            db.close();
        }
        return modelList;
    }

    public void updateFav(Model model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("fav", model.getFav());
        db.update("nahj", cv, "id = ?", new String[]{String.valueOf(model.getId())});
        db.close();
    }

    public void updateTitle(Model model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", model.getTitle());
        db.update("nahj", cv, "id = ?", new String[]{String.valueOf(model.getId())});
        db.close();
    }

    public void updateCnt(Model model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("cnt", model.getcnt());
        db.update("nahj", cv, "id = ?", new String[]{String.valueOf(model.getId())});
        db.close();
    }

    public void updateEnglishTranslation(int id, String titleEn, String cntEn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureEnglishColumns(db);
        ContentValues cv = new ContentValues();
        if (titleEn != null) cv.put(COL_TITLE_EN, titleEn);
        if (cntEn != null) cv.put(COL_CNT_EN, cntEn);
        db.update("nahj", cv, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void bulkUpdateEnglish(ArrayList<Model> models) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureEnglishColumns(db);
        db.beginTransaction();
        try {
            for (Model m : models) {
                ContentValues cv = new ContentValues();
                cv.put(COL_TITLE_EN, m.getTitle());
                cv.put(COL_CNT_EN, m.getcnt());
                db.update("nahj", cv, "id = ?", new String[]{String.valueOf(m.getId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public ArrayList<Model> getUntranslated() {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureEnglishColumns(db);
        ArrayList<Model> list = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT id,cat,num,title,cnt,fav,"+COL_TITLE_EN+","+COL_CNT_EN+" FROM nahj WHERE ("+COL_TITLE_EN+" IS NULL OR "+COL_TITLE_EN+"='' OR "+COL_CNT_EN+" IS NULL OR "+COL_CNT_EN+"='') ORDER BY id ASC", null)) {
            while (c.moveToNext()) {
                Model m = new Model(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5));
                m.setLanguage("fa");
                list.add(m);
            }
        }
        db.close();
        return list;
    }

    public int importTranslationsFromJsonArray(org.json.JSONArray arr) {
        int success = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ensureEnglishColumns(db);
        db.beginTransaction();
        try {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    org.json.JSONObject o = arr.getJSONObject(i);
                    int id = o.getInt("id");
                    String t = o.optString("title_en", "");
                    String c = o.optString("cnt_en", "");
                    ContentValues cv = new ContentValues();
                    cv.put(COL_TITLE_EN, t);
                    cv.put(COL_CNT_EN, c);
                    int rows = db.update("nahj", cv, "id=?", new String[]{String.valueOf(id)});
                    if (rows > 0) success++;
                } catch (Exception ignore) {}
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    private void CopyDatabaseFromAssets() throws IOException {
        InputStream myInput = mCtx.getAssets().open(DB_NAME);
        String outFileName = getDatabasePath();
        File f = new File(mCtx.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!f.exists())
            f.mkdir();

        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private static String getDatabasePath() {
        return mCtx.getApplicationInfo().dataDir + DB_PATH_SUFFIX + DB_NAME;
    }

    public SQLiteDatabase openDataBase() throws SQLiteException {
        File dbFile = mCtx.getDatabasePath(DB_NAME);
        SharedPreferences sharedPref = mCtx.getSharedPreferences("com.soroushrasti.nahj.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int dbVersion = sharedPref.getInt("DB_VERSION", 1);
        if (dbFile.exists()) {
            if (DB_VERSION != dbVersion) {
                try {
                    dbFile.delete();
                    Toast.makeText(mCtx, "Deleted the old database", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(mCtx, "Error deleting the old database", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException("Error deleting the old database", e);
                }
            }
        }
        if (!dbFile.exists()) {
            try {
                CopyDatabaseFromAssets();
                editor.putInt("DB_VERSION", DB_VERSION);
                editor.apply();
//                Toast.makeText(mCtx, "Coppying database success from assets folder", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
//                Toast.makeText(mCtx, "Coppying database success from assets folder", Toast.LENGTH_SHORT).show();
//                throw new RuntimeException("Error coppying database success from assets folder", e);
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}