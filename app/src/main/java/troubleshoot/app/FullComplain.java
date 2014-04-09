package troubleshoot.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aagam Shah on 26/3/14.
 */
public class FullComplain extends ActionBarActivity {
    public TextView titletv, location;
    public ImageView image_full;
    public int id, complainID;
    public LinearLayout l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        id = getIntent().getIntExtra("id", 0);
        DB db = new DB(getApplicationContext());
        Complain complain = db.getComplain(id);
        db.close();
        setContentView(R.layout.full_complain);
        titletv = (TextView) findViewById(R.id.full_problem);
        titletv.setText(complain.title);
        complainID = complain.complainid;
        location = (TextView) findViewById(R.id.full_loc);
        location.setText(complain.locality);
        if (complain.status.toLowerCase().equals("pending")) {
            LinearLayout l = (LinearLayout) findViewById(R.id.deadlinelayout);

            l.setVisibility(View.GONE);

            l = (LinearLayout) findViewById(R.id.beforeapprove);
            l.setVisibility(View.VISIBLE);
            l = (LinearLayout) findViewById(R.id.afterapprove);
            l.setVisibility(View.GONE);
            TextView status_view = (TextView)findViewById(R.id.full_status);
            status_view.setText("Pending Approval");

        } else if(complain.status.toLowerCase().equals("spam")){
            LinearLayout l = (LinearLayout) findViewById(R.id.deadlinelayout);

            l.setVisibility(View.GONE);
            l = (LinearLayout) findViewById(R.id.afterapprove);
            l.setVisibility(View.GONE);
            l = (LinearLayout) findViewById(R.id.beforeapprove);
            l.setVisibility(View.VISIBLE);
            TextView status_view = (TextView)findViewById(R.id.full_status);
            status_view.setText("Rejected by team");


        }
        else {
            l = (LinearLayout) findViewById(R.id.deadlinelayout);
            TextView tv = (TextView) findViewById(R.id.full_deadline);

            String date_unformatted = complain.date;



            DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
            Date d = null;
            try {
                d = (Date) date.parse(date_unformatted);

            DateFormat required_format = new SimpleDateFormat("dd-MM-yyyy");

            tv.setText(required_format.format(d));
            } catch (ParseException e) {
                Log.e("error","date");
                e.printStackTrace();
            }



            TextView crosscheck = (TextView)findViewById(R.id.full_crosscheck);
            crosscheck.setText(complain.reviewer+", "+complain.reviewer_c);
            TextView approver = (TextView)findViewById(R.id.full_approver);
            approver.setText(complain.officer);
            TextView approver_c = (TextView)findViewById(R.id.full_response);
            approver_c.setText(complain.officer_c);

        }

        image_full = (ImageView) findViewById(R.id.full_image);
        Log.e("complain loc", complain.imgloc);

        if (complain.imgloc.equals("Default"))
            new ImageDownloader(image_full, complain.imgol).execute();
        else {

            File f = new File(complain.imgloc);
            if (f.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(complain.imgloc);

                image_full.setImageBitmap(bitmap);

            } else
                new ImageDownloader(image_full, complain.imgol).execute();
        }
        getSupportActionBar().setTitle(complain.title);
    }


    class ImageDownloader extends AsyncTask<String, String, String> {
        public ImageView iv;
        public String imgurl = "";
        public String tempLoc = "";

        public ImageDownloader(ImageView imageView, String url) {
            iv = imageView;
            imgurl = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] objects) {
            Bitmap b;
            File root = android.os.Environment.getExternalStorageDirectory();

            File dir = new File(root.getAbsolutePath() + "/mnt/sdcard/troubles");
            if (dir.exists() == false) {
                dir.mkdirs();
            }

            URL url = null; //you can write here any link
            try {
                url = new URL(imgurl);

                File file = new File(dir, "TS_" + System.currentTimeMillis() + ".jpg");
                tempLoc = file.getAbsolutePath();
                Log.e("app", "download begining " + tempLoc);

                URLConnection ucon = url.openConnection();

                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer baf = new ByteArrayBuffer(5000);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.e("error", "new image");
                e.printStackTrace();
                return "error";
            }
            return tempLoc;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && !s.equals("error")) {
                Bitmap bitmap = BitmapFactory.decodeFile(tempLoc);
                iv.setImageBitmap(bitmap);
                DB db = new DB(getApplicationContext());
                db.addImagePath(tempLoc, complainID);
                Log.e("db image", "added");
            } else {
                Toast.makeText(getApplicationContext(),
                        "Error downloading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.full_complain_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            Log.e("delete","complain");
            return true;
        }
        return true;
    }
}
