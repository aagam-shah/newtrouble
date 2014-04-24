package troubleshoot.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Aagam Shah on 26/3/14.
 */

public class AddComplain2 extends Fragment {
    //public String[] categ_array = {"Wastage", "Roads", "Stray animals", "Cleanliness"};
    public String[] central_zone = {"Khadia", "Kalupur", "Dariyapur", "Shahpur", "Raykhad", "Jamalpur", "Dudheshwar", "Madhupura", "Girdharnagar"};
    public String[] east_zone = {"Rajpur", "Arbudanagar", "Odhav", "Vastral", "Mahavirnagar", "Bhaipura", "Amraiwadi", "Ramol", "Hathijan"};
    public String[] west_zone = {"Paldi", "Vasna", "Ambawadi", "Navrangpura", "Juna Vadaj", "Nava Vadaj", "Naranpura", "Stadium", "Sabarmati", "Chandkheda", "Motera", "Stadium", "Sabarmati"};
    public String[] north_zone = {"Saraspur", "Sardarnagar", "Noblenagar", "Naroda", "Kubernagar", "Saijpur", "Meghaninagar", "Asarva", "Naroda Road", "India Colony", "Krushnanagar", "Thakkarnagar", "Saraspur"};
    public String[] south_zone = {"Isanpur", "Lambha", "Maninagar", "Kankaria", "Behrampura", "Dani Limda", "Ghodasar", "Indrapuri", "Khokhra", "Vatva", "Isanpur", "Stadium", "Sabarmati"};
    public String[] new_west_zone = {"Vejalpur", "Jodhpur", "Bodakdev", "Thaltej", "Ghatlodia", "Ranip", "Kali", "Gota", "Satellite"};
    public String[] all = {"Khadia", "Kalupur", "Dariyapur", "Shahpur", "Raykhad", "Jamalpur", "Dudheshwar", "Madhupura", "Girdharnagar",
            "Rajpur", "Arbudanagar", "Odhav", "Vastral", "Mahavirnagar", "Bhaipura", "Amraiwadi", "Ramol", "Hathijan",
            "Paldi", "Vasna", "Ambawadi", "Navrangpura", "Juna Vadaj", "Nava Vadaj", "Naranpura", "Stadium", "Sabarmati", "Chandkheda", "Motera", "Stadium", "Sabarmati",
            "Saraspur", "Sardarnagar", "Noblenagar", "Naroda", "Kubernagar", "Saijpur", "Meghaninagar", "Asarva", "Naroda Road", "India Colony", "Krushnanagar", "Thakkarnagar", "Saraspur",
            "Isanpur", "Lambha", "Maninagar", "Kankaria", "Behrampura", "Dani Limda", "Ghodasar", "Indrapuri", "Khokhra", "Vatva", "Isanpur", "Stadium", "Sabarmati",
            "Vejalpur", "Jodhpur", "Bodakdev", "Thaltej", "Ghatlodia", "Ranip", "Kali", "Gota", "Satellite"};
    //central_zone+east_zone+west_zone+north_zone+south_zone+new_west_zone;
    public String[] categ_array = {"Road", "Divider", "Footpath", "Manhole", "Drainage Lines", "Water Lines", "Thermoplast", "Paint", "Light", "Street Light", "Dump Site", "Garbage Collection", "Sanitation", "Waste", "Green Waste", "Plants on divider", "Tree Guard"};
    public EditText title, descr, addr;
    public Spinner category;
    public AutoCompleteTextView locality;
    public Button submit;
    public Context ctx;
    public String titletext, descrtext, addrtext, datetext, imgol, localitytext, categ;
    public String path;
    public DB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_complain2, container, false);
        path = getArguments().getString("path");
        db = new DB(getActivity());
        ctx = getActivity();
        title = (EditText) view.findViewById(R.id.title);
        descr = (EditText) view.findViewById(R.id.description);
        addr = (EditText) view.findViewById(R.id.address);
        locality = (AutoCompleteTextView) view.findViewById(R.id.location);
        ArrayAdapter<String> localityda = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, all);
        locality.setAdapter(localityda);
        locality.setDropDownBackgroundResource(R.drawable.autcomplete);


        submit = (Button) view.findViewById(R.id.submit);
        category = (Spinner) view.findViewById(R.id.category);


        ArrayAdapter<String> da = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, categ_array);
        category.setAdapter(da);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectionDetector cd = new ConnectionDetector(getActivity());
                boolean isInternetConnected = cd.isConnectingToInternet();
                if (!isInternetConnected) {
                    Toast toast = Toast.makeText(getActivity(), "Network Unavailable.Please try again later.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    titletext = title.getText().toString();
                    descrtext = descr.getText().toString();
                    addrtext = addr.getText().toString();
                    localitytext = locality.getText().toString();
                    categ = category.getSelectedItem().toString();
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    datetext = sdf.format(d);

                    boolean check = checknull(titletext, descrtext, addrtext, localitytext);
                    if (check) {


                        new Post().execute();
                    }
                }

            }
        });

        return view;
    }


    public class Post extends AsyncTask<String, Long, String> {
        ProgressDialog pdg;

        String retlocation = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing the dialg initially
            pdg = ProgressDialog.show(ctx, "", "Posting Complain");

            //If the complain takes too much time to upload
            // than the user can cancel by touching out
            pdg.setCanceledOnTouchOutside(true);

        }

        @Override
        protected String doInBackground(String[] objects) {

            File sourceFile = new File(path);
            SharedPreferences preferences = getActivity().
                    getSharedPreferences("troubles", Context.MODE_PRIVATE);
            if (!sourceFile.isFile()) {

                Toast.makeText(getActivity(), "Image not found.", Toast.LENGTH_SHORT).show();
                return null;
            }

            //Scale the image to 640x480 resolution
            Bitmap bmp = decodeSampledBitmapFromResource(path, 640, 480);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            //Compress the newly scaled image to 50%
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            bmp.recycle();

            InputStream in = new ByteArrayInputStream(outputStream.toByteArray());

            //upload the image and get the upload image path

            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
            MultipartEntity mpEntity = new MultipartEntity();
            Random r = new Random();
            ContentBody cbFile = new InputStreamBody(in, preferences.getInt("id", -1) + "TS_" + r.nextInt(10000) + ".jpg");
            mpEntity.addPart("userfile", cbFile);
            httppost.setEntity(mpEntity);
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity resEntity = response.getEntity();
                retlocation = EntityUtils.toString(resEntity);
                Log.e("response", retlocation);
            } catch (Exception e) {
                Log.e("resp", "exception in response");
                e.printStackTrace();
                return "";
            }


            //upload data now since the image has been uploaded
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/complain_upload.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("title", titletext));
            pairs.add(new BasicNameValuePair("description", descrtext));
            pairs.add(new BasicNameValuePair("address", addr.getText().toString()));
            pairs.add(new BasicNameValuePair("locality", localitytext));
            pairs.add(new BasicNameValuePair("imageloc", retlocation));
            pairs.add(new BasicNameValuePair("category", categ));

            pairs.add(new BasicNameValuePair("userid", "" + preferences.getInt("id", -1)));
            pairs.add(new BasicNameValuePair("datetime", datetext));
            pairs.add(new BasicNameValuePair("username", preferences.getString("name", "TS")));
            pairs.add(new BasicNameValuePair("status", "Pending"));
            String result;
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = client.execute(post);
                bmp.recycle();
                result = EntityUtils.toString(response.getEntity());
                Log.e("result", result);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return result;
        }

        /**
         * Adjust the sample size image according to the required width and height
         *
         * @param options   Various options for the BitMap factory
         * @param reqWidth  Required width of the new Image
         * @param reqHeight Required height of the new Image
         * @return The appropriate sample size
         */
        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        /**
         * Sampling bitmap the native way results in MemoryOutOfBound. So this
         * is optimal way to sample the image
         *
         * @param paths
         * @param reqWidth
         * @param reqHeight
         * @return
         */
        public Bitmap decodeSampledBitmapFromResource(String paths,
                                                      int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //BitmapFactory.decodeResource(res, resId, options);
            BitmapFactory.decodeFile(paths, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(paths, options);
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            pdg.dismiss();
            if (!o.equals("")) {
                retlocation = "http://blog-aagam.rhcloud.com/" + retlocation;
                int id = Integer.parseInt(o);
                Toast.makeText(getActivity(), o, Toast.LENGTH_SHORT).show();
                Complain complain = new Complain(id, titletext, descrtext,
                        "Pending", datetext, localitytext, path, retlocation, categ);
                db.add(complain);
                db.close();
                Log.e("db", "added complain " + id);
                FragmentTransaction trans = getFragmentManager()
                        .beginTransaction();
                SuccessComplain successComplain = new SuccessComplain();
                Bundle b = new Bundle();
                b.putString("complainNo", "" + id);
                successComplain.setArguments(b);
                trans.replace(R.id.root_frame, successComplain);
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();
                Cursor c = new DB(getActivity()).getList();
                ComplainAdapter adapter = new ComplainAdapter(getActivity(), R.layout.complain_item,
                        c, new String[]{"_id", "title", "status", "datecreated"},
                        new int[]{R.id.item_hidden, R.id.item_title, R.id.item_status
                                , R.id.item_date}
                );
                MyComplainsFragment.lv.setAdapter(adapter);
            } else
                Toast.makeText(getActivity(), "Error posting complain", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verify that the data entered for the complain is not null
     *
     * @param title Title of the complain
     * @param des   Description of the complain
     * @param addr  Address of the complain
     * @param area  Area of the complain
     * @return Returns false if any one of them is empty
     */
    public boolean checknull(String title, String des, String addr, String area) {
        boolean res = false;
        if (title.matches("")) {
            Toast.makeText(getActivity(), "Enter title", Toast.LENGTH_SHORT).show();
            return res;
        } else {
            if (des.matches("")) {
                Toast.makeText(getActivity(), "Enter description", Toast.LENGTH_SHORT).show();
                return res;
            } else {
                if (addr.matches("")) {
                    Toast.makeText(getActivity(), "Enter address", Toast.LENGTH_SHORT).show();
                    return res;
                } else {
                    if (area.matches("")) {
                        Toast.makeText(getActivity(), "Enter the area", Toast.LENGTH_SHORT).show();
                        return res;
                    } else {
                        res = true;
                        return res;
                    }
                }
            }
        }
    }


}
