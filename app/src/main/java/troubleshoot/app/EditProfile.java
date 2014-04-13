package troubleshoot.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Vandit on 4/12/2014.
 */
public class EditProfile extends Activity {


    public EditText editname, editlocality, editemail, editpass, editconfpass;
    public Button buttondone,buttonphoto;
    public ImageView profile_image;
    public SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        editname = (EditText) findViewById(R.id.etname);
        editemail = (EditText) findViewById(R.id.ettextemail);
        editlocality = (EditText) findViewById(R.id.ettextlocality);
        editpass = (EditText) findViewById(R.id.etnewpass);
        editconfpass = (EditText) findViewById(R.id.etconfpass);
        buttondone = (Button) findViewById(R.id.bchange);
        buttonphoto = (Button) findViewById(R.id.bchangephoto);

        preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
        profile_image = (ImageView) findViewById(R.id.profile_image);

        //putting default values
        editname.setText(preferences.getString("name", "Default"));
        editemail.setText(preferences.getString("email", "Default"));
        editlocality.setText(preferences.getString("locality", "Default"));

        String imgloc = preferences.getString("img_loc", "Default");

        if (imgloc.equals("Default")) {
            new ImageDownloader(profile_image, preferences.getString("img_ol", "Default")).execute();
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(imgloc);
            profile_image.setImageBitmap(bitmap);
        }


    }


    class ImageDownloader extends AsyncTask<String, String, String> {
        public ImageView iv;
        public String imgurl = "";
        public String tempLoc = "";
        public ProgressDialog pdg;

        public ImageDownloader(ImageView imageView, String url) {
            //    pdg = ProgressDialog.show(getActivity(), "", "Downloading Profile Pic", true);
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

                File file = new File(dir, "TS-" + System.currentTimeMillis() + ".jpg");
                Log.e("exact path", file.getAbsolutePath());
                tempLoc = file.getAbsolutePath();
                long startTime = System.currentTimeMillis();
                Log.e("DownloadManager", "download begining");

               /* Open a connection to that URL. */
                URLConnection ucon = url.openConnection();

               /*
                * Define InputStreams to read from the URLConnection.
                */
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

               /*
                * Read bytes to the Buffer until there is nothing more to read(-1).
                */
                ByteArrayBuffer baf = new ByteArrayBuffer(5000);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

               /* Convert the Bytes read to a String. */
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.e("error", "new image");
                //e.printStackTrace();
                return null;
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // pdg.dismiss();
            Log.e("done", "imagedownload");
            iv.setImageResource(R.drawable.ic_launcher);
            SharedPreferences pref = getSharedPreferences("troubles", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("img_loc", tempLoc);
            editor.commit();

            Bitmap bitmap = BitmapFactory.decodeFile(tempLoc);
            iv.setImageBitmap(bitmap);

        }
    }
}

