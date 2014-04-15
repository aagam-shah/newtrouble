package troubleshoot.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Signup2 extends Activity {

    public Button submit;
    public ImageButton imgbutton;
    public String emails, passws, phones, locals, names;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signup2);

        emails = getIntent().getStringExtra("email");
        passws = getIntent().getStringExtra("pass");
        phones = getIntent().getStringExtra("phone");
        locals = getIntent().getStringExtra("loc");
        names = getIntent().getStringExtra("name");
        imgbutton = (ImageButton) findViewById(R.id.ibprofilephoto);
        imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 133);
            }
        });

        submit = (Button) findViewById(R.id.bsubmit);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
                boolean isInternetConnected = cd.isConnectingToInternet();
                if (!isInternetConnected) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Network Unavailable.Please try again later.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    new SignUp().execute();
                }
            }
        });
    }

    public String imagepath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == 133) {
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);

            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);

            imgbutton.setImageBitmap(bitmap);
            submit.setEnabled(true);
            Log.e("pathin 1", imagepath);
            Toast.makeText(getApplicationContext(), "path: " + imagepath, Toast.LENGTH_SHORT).show();
            //new Uploader(""+imagepath,getActivity()).execute();

        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    class SignUp extends AsyncTask<String, String, String> {
        public ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(Signup2.this, "", "Signing up...");
            Log.e("pre", "yoo");
        }

        public String retlocation;

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            retlocation = null;
            ///add code for image

            File sourceFile = new File(imagepath);
            if (!sourceFile.isFile()) {
                //pdg.dismiss();
                Log.e("app" + imagepath, "" + imagepath);
                Log.e("app", "error");
                return null;
            }
            Log.e("app", "success");

            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            Bitmap bmp = bitmap.createScaledBitmap(bitmap,640,480,true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
            //File file = new File(filepath);
            MultipartEntity mpEntity = new MultipartEntity();
            // ContentBody cbFile = new FileBody(sourceFile, "image/jpg");
            ContentBody cbFile = new InputStreamBody(inputStream, "" + phones);
            mpEntity.addPart("userfile", cbFile);
            httppost.setEntity(mpEntity);
            try {
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity resEntity = response.getEntity();

                // Log.e("resp", "" + EntityUtils.toString(resEntity));
                retlocation = EntityUtils.toString(resEntity);
                Log.e("response", retlocation);
                //Log.e("resp loc",""+resEntity.toString());
            } catch (Exception e) {
                Log.e("resp", "exception in response");
                e.printStackTrace();
                return "";
            }


            try {


                //adding data
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/signup.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("name", names));
                pairs.add(new BasicNameValuePair("password", passws));
                pairs.add(new BasicNameValuePair("emailid", emails));
                pairs.add(new BasicNameValuePair("locality", locals));
                pairs.add(new BasicNameValuePair("phone", phones));
                pairs.add(new BasicNameValuePair("imgloc", retlocation));

                post.setEntity(new UrlEncodedFormEntity(pairs));

                HttpResponse response = client.execute(post);

                result = EntityUtils.toString(response.getEntity());

                Log.e("res", result);
            } catch (Exception e) {
                Log.e("res", "exce");
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            Log.e("done", "" + s);
            if (s != null || s.toLowerCase().equals("exist")) {
                SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("id", Integer.parseInt(s));
                editor.putString("name", names);
                editor.putString("locality", locals);
                editor.putString("img_loc", imagepath);
                editor.putString("img_ol", "https://blog-aagam.rhcloud.com/" + retlocation);
                editor.putString("phone", phones);
                editor.putString("email", emails);

                //add champion feature
                editor.putInt("champion_month", 0);
                editor.putInt("champion_year", 0);
                editor.putString("champion_name", "Default");

                editor.commit();
                Log.e("added", "to SP");
                DB db = new DB(getApplicationContext());
                db.drop();

                Intent i = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(i);
                finish();
            } else {
                if (s.equals("exist"))
                    Toast.makeText(getApplicationContext(), "Phone number already exist", Toast.LENGTH_SHORT);
                else
                    Toast.makeText(getApplicationContext(), "Error signing up", Toast.LENGTH_SHORT);
            }

        }
    }

}

