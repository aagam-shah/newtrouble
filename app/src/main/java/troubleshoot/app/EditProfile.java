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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

/**
 * Created by Vandit on 4/12/2014.
 */
public class EditProfile extends Activity {


    public EditText editname, editlocality, editemail, editpass, editconfpass;
    public String name, locality, mail, pass;
    public Button buttondone, buttonphoto;
    public ImageView profile_image;
    public SharedPreferences preferences;
    public int id;
    public String path = "";
    public String origPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        id = preferences.getInt("id", 0);
        String imgloc = preferences.getString("img_loc", "Default");
        path = imgloc;
        origPath = imgloc;
        Bitmap bitmap = BitmapFactory.decodeFile(imgloc);
        profile_image.setImageBitmap(bitmap);
        buttondone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = editname.getText().toString();
                mail = editemail.getText().toString();
                locality = editlocality.getText().toString();
                pass = editpass.getText().toString();
                new UpdateProfile().execute();
            }
        });
    }


    class UpdateProfile extends AsyncTask<String, String, String> {
        public ProgressDialog pdg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdg = ProgressDialog.show(EditProfile.this, "", "Updating profile");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pdg.dismiss();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            //  retlocation = null;
            ///add code for image
            if (!path.equals(origPath)) {
                File sourceFile = new File(path);
                if (!sourceFile.isFile()) {
                    //pdg.dismiss();
                    Log.e("app" + path, "" + path);
                    Log.e("app", "error");
                    return null;
                }
                Log.e("app", "success");

                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Bitmap bmp = bitmap.createScaledBitmap(bitmap, 640, 480, true);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
                //File file = new File(filepath);
                MultipartEntity mpEntity = new MultipartEntity();
                // ContentBody cbFile = new FileBody(sourceFile, "image/jpg");
                ContentBody cbFile = new InputStreamBody(inputStream, "" + pass);
                mpEntity.addPart("userfile", cbFile);
                httppost.setEntity(mpEntity);
                try {
                    HttpResponse response = httpclient.execute(httppost);

                    HttpEntity resEntity = response.getEntity();

                    // Log.e("resp", "" + EntityUtils.toString(resEntity));
                    path = EntityUtils.toString(resEntity);
                    Log.e("response", path);
                    //Log.e("resp loc",""+resEntity.toString());
                } catch (Exception e) {
                    Log.e("resp", "exception in response");
                    e.printStackTrace();
                    return "";
                }

            } else {
                path = "Default";
            }
            try {


                //adding data
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/updateprofile.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("id",""+id));
                pairs.add(new BasicNameValuePair("name", name));
                pairs.add(new BasicNameValuePair("password", pass));
                pairs.add(new BasicNameValuePair("emailid", mail));
                pairs.add(new BasicNameValuePair("locality", locality));
                //pairs.add(new BasicNameValuePair("phone", phones));
                pairs.add(new BasicNameValuePair("imgloc", path));

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
    }

}

