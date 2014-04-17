package troubleshoot.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Aagam Shah on 25/3/14.
 */

public class ProfileFragment extends Fragment {

    public TextView name, email, locality, phoneno;
    public ImageView profile_image;
    public SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.profile, container, false);
        name = (TextView) view.findViewById(R.id.textname);
        email = (TextView) view.findViewById(R.id.textemail);
        locality = (TextView) view.findViewById(R.id.textlocality);
        phoneno = (TextView) view.findViewById(R.id.textcontact);
        preferences = getActivity().getSharedPreferences("troubles", Context.MODE_PRIVATE);
        profile_image = (ImageView) view.findViewById(R.id.profile_image);

        name.setText(preferences.getString("name", "Default"));
        email.setText(preferences.getString("email", "Default"));
        locality.setText(preferences.getString("locality", "Default"));
        phoneno.setText(preferences.getString("phone", "Default"));
        String imgloc = preferences.getString("img_loc", "Default");

        if (imgloc.equals("Default")) {
            new ImageDownloader(profile_image, preferences.getString("img_ol", "Default")).execute();
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;

            Bitmap bitmap = BitmapFactory.decodeFile(imgloc,options);
            profile_image.setImageBitmap(bitmap);
        }

        return view;
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
            SharedPreferences pref = getActivity().getSharedPreferences("troubles", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("img_loc", tempLoc);
            editor.commit();

            Bitmap bitmap = BitmapFactory.decodeFile(tempLoc);
            iv.setImageBitmap(bitmap);

        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("resume", "app");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("destroy", "app");
    }
}
