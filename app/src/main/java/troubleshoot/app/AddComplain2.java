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
    public String[] categ_array = {"Wastage", "Roads", "Stray animals", "Cleanliness"};
    public EditText title, descr, addr;
    public Spinner category;
    public AutoCompleteTextView locality;
    public Button submit;
    public Context ctx;
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
                    new Post().execute();
                }

            }
        });

        return view;
    }


    public class Post extends AsyncTask<String, Long, String> {
        ProgressDialog pdg;
        String titletext, descrtext, datetext, imgol, localitytext, categ;
        String retlocation = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdg = ProgressDialog.show(ctx, "", "Posting Complain");
            pdg.setCanceledOnTouchOutside(true);
            titletext = title.getText().toString();
            descrtext = descr.getText().toString();
            localitytext = locality.getText().toString();
            categ = category.getSelectedItem().toString();
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            datetext = sdf.format(d);

        }

        @Override
        protected String doInBackground(String[] objects) {

            File sourceFile = new File(path);
            if (!sourceFile.isFile()) {
                //pdg.dismiss();
                Toast.makeText(getActivity(), "Image not found.", Toast.LENGTH_SHORT).show();
                Log.e("app" + path, "" + path);
                Log.e("app", "error");
                return null;
            }
            Log.e("app", "success");

            Log.e("original size: ", " " + sourceFile.length());
            Bitmap bitmap = BitmapFactory.decodeFile(path);


            Bitmap bmp = bitmap.createScaledBitmap(bitmap, 640, 480, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            Log.e("rescale size: ", " " + outputStream.size());
            InputStream in = new ByteArrayInputStream(outputStream.toByteArray());

            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
            MultipartEntity mpEntity = new MultipartEntity();
            //ContentBody cbFile = new FileBody(sourceFile, "image/jpg");
            Random r = new Random();

            ContentBody cbFile = new InputStreamBody(in, "TS_" + r.nextInt(10000));
            mpEntity.addPart("userfile", cbFile);
            httppost.setEntity(mpEntity);
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity resEntity = response.getEntity();
                retlocation = EntityUtils.toString(resEntity);
                Log.e("response", retlocation);
                //Log.e("resp loc",""+resEntity.toString());
            } catch (Exception e) {
                Log.e("resp", "exception in response");
                e.printStackTrace();
                return "";
            }


            //upload data now

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/complain_upload.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("title", titletext));
            pairs.add(new BasicNameValuePair("description", descrtext));
            pairs.add(new BasicNameValuePair("address", addr.getText().toString()));
            pairs.add(new BasicNameValuePair("locality", localitytext));
            pairs.add(new BasicNameValuePair("imageloc", retlocation));
            pairs.add(new BasicNameValuePair("category", categ));
            SharedPreferences preferences = getActivity().
                    getSharedPreferences("troubles", Context.MODE_PRIVATE);
            pairs.add(new BasicNameValuePair("userid", "" + preferences.getInt("id", -1)));
            pairs.add(new BasicNameValuePair("datetime", datetext));
            pairs.add(new BasicNameValuePair("username", preferences.getString("name", "TS")));
            pairs.add(new BasicNameValuePair("status", "Pending"));
            //pairs.add(new BasicNameValuePair("location",retlocation));
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
}
