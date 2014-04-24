package troubleshoot.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Aagam Shah on 28/3/14.
 * Database structure of the offline database
 */
public class DB extends SQLiteOpenHelper {

    public String CREATE_TABLE_COMPLAIN = "CREATE TABLE IF NOT EXISTS complain" +
            " ( _id integer primary key ,datecreated text default '01/03/2014', complainid integer, title text, " +
            "description text,locality text, img_loc text, img_ol text, status text)";

    public String CREATE_TABLE_ADMIN = "CREATE TABLE IF NOT EXISTS admin" +
            " ( _id integer primary key , complainid integer, date text, " +
            "officer text,reviewer text, reviewercomment text, officercomment text)";

    public String DROP_TABLE_COMPLAIN = "DROP TABLE IF EXISTS complain";
    public String DROP_TABLE_ADMIN = "DROP TABLE IF EXISTS admin";

    public DB(Context ctx) {
        super(ctx, "troubles", null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("created", "db");
        db.execSQL(CREATE_TABLE_COMPLAIN);
        db.execSQL(CREATE_TABLE_ADMIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL(DROP_TABLE_ADMIN);
        db.execSQL(DROP_TABLE_COMPLAIN);
        db.execSQL(CREATE_TABLE_COMPLAIN);
        db.execSQL(CREATE_TABLE_ADMIN);
    }

    public void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_TABLE_ADMIN);
        db.execSQL(DROP_TABLE_COMPLAIN);
        db.execSQL(CREATE_TABLE_COMPLAIN);
        db.execSQL(CREATE_TABLE_ADMIN);

    }

    /**
     * Add new Complain to the databse
     *
     * @param c Object of {@link troubleshoot.app.Complain} with the details
     */
    public void add(Complain c) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("complainid", c.complainid);
        values.put("title", c.title);
        values.put("description", c.descr);
        values.put("locality", c.locality);
        values.put("img_loc", c.imgloc);
        values.put("img_ol", c.imgol);
        values.put("status", c.status);
        values.put("datecreated", c.date);
        db.insert("complain", null, values);
        db.close();
    }

    /**
     * Remove old admin database of the complains and create new
     */
    public void adminDrop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_TABLE_ADMIN);
        db.execSQL(CREATE_TABLE_ADMIN);
    }

    /**
     * Get a specific complain by its id
     *
     * @param id
     * @return respected Complain
     */
    public troubleshoot.app.Complain getComplain(int id) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db != null ? db.query("complain", new String[]{"_id", "complainid", "locality", "title", "status", "img_ol", "img_loc"},
                "_id = " + id, null, null, null, null, null) : null;
        c.moveToFirst();
        String title = c.getString(c.getColumnIndex("title"));
        String locality = c.getString(c.getColumnIndex("locality"));
        int complainid = c.getInt(c.getColumnIndex("complainid"));
        String status = c.getString(c.getColumnIndex("status"));


        String img_l = c.getString(c.getColumnIndex("img_loc"));
        String img_ol = c.getString(c.getColumnIndex("img_ol"));
        troubleshoot.app.Complain cp;
        if (status.toLowerCase().equals("pending")) {
            cp = new troubleshoot.app.Complain(complainid, title, status, locality, img_l, img_ol);
        } else if (status.toLowerCase().equals("approved")) {
            Cursor c1 = db.query("admin", new String[]{"date", "officer", "reviewer"
                            , "reviewercomment", "officercomment"},
                    "complainid = " + complainid, null, null, null, null
            );


            if (c1.getCount() < 1) {
                Log.e("null", "admin");
                troubleshoot.app.Complain cp1 = new troubleshoot.app.Complain(complainid, title, status, locality, img_l, img_ol);
                return cp1;
            }
            c1.moveToFirst();
            String reviewer = c1.getString(c1.getColumnIndex("reviewer"));
            String reviewer_c = c1.getString(c1.getColumnIndex("reviewercomment"));
            String officer = c1.getString(c1.getColumnIndex("officer"));
            String officer_c = c1.getString(c1.getColumnIndex("officercomment"));
            String date = c1.getString(c1.getColumnIndex("date"));
            troubleshoot.app.Complain cp2 = new troubleshoot.app.Complain(complainid, title, status, locality, img_l, img_ol,
                    date, reviewer, reviewer_c, officer, officer_c);
            return cp2;
        } else {
            cp = new troubleshoot.app.Complain(complainid, title, status, locality, img_l, img_ol);
        }
        return cp;
    }

    /**
     * Get whole list of the complains present in the local database
     */
    public Cursor getList() {
        SQLiteDatabase db = getWritableDatabase();

        try {
            Cursor c = db != null ? db.query("complain", new String[]{"_id", "title", "status",
                            "datecreated"},
                    null, null, null, null, "complainid DESC", null
            ) : null;

            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Change the status of a complain from its id
     *
     * @param complainid The complain id whose status is to be modified
     */
    public void changeStatus(int complainid) {
        ContentValues values = new ContentValues();
        values.put("status", "Approved");
        SQLiteDatabase database = getWritableDatabase();
        try {
            database.update("complain", values, "complainid = " + complainid, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * After fetching the image location,storing the local image path of
     * of the specific complain
     */
    public void addImagePath(String s, int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("img_loc", s);
        try {
            db.update("complain", values, "complainid = " + id, null);
        } catch (Exception e) {
            Log.e("nullpointer", "adding imagepath:id " + id + " path: " + s);
            e.printStackTrace();
        }
        db.close();
    }

    /**
     * Add complain with it's id only if it is not present in the local database
     */
    public void addORinsert(ContentValues cv, int i) {


        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.query("complain", new String[]{"complainid"},
                    "complainid = " + i, null, null, null, null);

            int ct = c.getCount();
            if (ct < 1) {
                db.insert("complain", null, cv);
            } else {
                try {
                    if (!cv.getAsString("status").toLowerCase().equals("pending")) {
                        cv.remove("img_loc");
                        db.update("complain", cv, "complainid = " + i, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add the admin information of a complain in the admin database
     */
    public void addAdmin(ContentValues c) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            long id = db.insert("admin", null, c);
            if (id == -1) {
                Log.e("error", "inserting admin");

            } else {
                Log.e("success admin", "id " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }
}
