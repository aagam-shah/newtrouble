package troubleshoot.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Created by Mrunal Dave on 06/04/2014.
 */
public class Champion extends Fragment {
    public ImageView imageView;
    public int year, month, id;
    public String champion_local,name,locality;
    public TextView champion_name,champion_locality;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_champion, container, false);
        //new comment
        SharedPreferences pref = getActivity().getSharedPreferences("troubles", Context.MODE_PRIVATE);
        year = pref.getInt("champion_year", 0);
        month = pref.getInt("champion_month", 0);
        champion_local = pref.getString("champion_img", "Default");
        name=pref.getString("champion_name","Default");
        locality=pref.getString("champion_locality","Default");
        imageView = (ImageView) view.findViewById(R.id.champion_image);
        champion_name=(TextView)view.findViewById(R.id.champion_name);
        champion_locality=(TextView)view.findViewById(R.id.champion_locality);
        ConnectionDetector detector = new ConnectionDetector(getActivity());
        if (month == 0) {
            //download latest champion
            if(detector.isConnectingToInternet())
            new ChampionDownloader().execute();
            else{
                Toast.makeText(getActivity(),"Can't connect to internet",Toast.LENGTH_SHORT).show();
            }

        } else {
            Date d = new Date();
            int curr_month = d.getMonth();
            curr_month++;
            if (curr_month != month) {
                //Download complains
                if(detector.isConnectingToInternet())
                    new ChampionDownloader().execute();
                else{
                    Toast.makeText(getActivity(),"Can't connect to internet",Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!champion_local.equals("Default")) {


                    Bitmap bm = BitmapFactory.decodeFile(champion_local);
                    Bitmap resized = Bitmap.createScaledBitmap(bm, 200, 200, true);
                    bm.recycle();
                    Bitmap conv_bm = getRoundedRectBitmap(resized, 200);
                    resized.recycle();
                    imageView.setImageBitmap(conv_bm);
                    if(!name.equals("Default")){
                        champion_name.setText(name);
                        champion_locality.setText(locality);
                    }
                    else{
                        if(detector.isConnectingToInternet())
                            new ChampionDownloader().execute();
                        else{
                            Toast.makeText(getActivity(),"Can't connect to internet",Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    //Download complains
                    if(detector.isConnectingToInternet())
                        new ChampionDownloader().execute();
                    else{
                        Toast.makeText(getActivity(),"Can't connect to internet",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, 200, 200);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(90, 90, 90, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

        } catch (NullPointerException e) {
            Log.e("Champion", "NPE");
        } catch (OutOfMemoryError o) {
            Log.e("Champion", "OOM");
        }
        return result;
    }


    class ChampionDownloader extends AsyncTask<String, String, String> {
        public String tempLoc = "";
        public ProgressDialog pdg;
        public String userpic = "";
        public String username="";
        public String locality="";
        public String month="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // pdg = ProgressDialog.show(getActivity(), "", "Fetching new champion");

        }


        @Override
        protected String doInBackground(String[] objects) {
            //Download data and get Image
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/champion_get.php");

            try {
                HttpResponse resp = client.execute(post);

                String response = EntityUtils.toString(resp.getEntity());
                Log.e("champion", response);

                JSONObject jsonObject = new JSONObject(response);

                username = jsonObject.getString("name");
                userpic = jsonObject.getString("profilepic");
                locality = jsonObject.getString("locality");
                month = jsonObject.getString("month");
                Log.e("username", "" + username);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }


            File root = android.os.Environment.getExternalStorageDirectory();

            File dir = new File(root.getAbsolutePath() + "/mnt/sdcard/troubles");
            if (dir.exists() == false) {
                dir.mkdirs();
            }

            URL url = null; //you can write here any link
            try {
                url = new URL("http://blog-aagam.rhcloud.com/" + userpic);
                File file = new File(dir, "TS-CHAMP.jpg");
                Log.e("exact path", file.getAbsolutePath());
                tempLoc = file.getAbsolutePath();
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
                tempLoc = "";
                //e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
           // pdg.dismiss();
            Log.e("finish", "champion");
            if (!tempLoc.equals("")) {
                Bitmap bm = BitmapFactory.decodeFile(tempLoc);
                Bitmap resized = Bitmap.createScaledBitmap(bm, 200, 200, true);
                bm.recycle();
                Bitmap conv_bm = getRoundedRectBitmap(resized, 200);
                resized.recycle();
                imageView.setImageBitmap(conv_bm);
                champion_name.setText(username);
                champion_name.setVisibility(View.VISIBLE);
                SharedPreferences pref = getActivity().getSharedPreferences("troubles", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("champion_name",username);
                editor.putString("champion_img",tempLoc);
                editor.putString("champion_locality",locality);
                editor.putInt("champion_month",Integer.parseInt(month));
                editor.commit();
            } else {
                champion_name.setVisibility(View.GONE);
                Log.e("error", "fetching champion");
                Toast.makeText(getActivity(),
                        "Champion can't be retrieved at this time", Toast.LENGTH_SHORT).show();
            }
        }
    }
}