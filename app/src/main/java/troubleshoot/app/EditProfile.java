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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

/**
 * Created by Vandit on 4/12/2014.
 */
public class EditProfile extends Activity {


    public EditText editname, editemail, editpass, editconfpass;
    public AutoCompleteTextView editlocality;
    public String name, locality, mail, pass,confpass;
    public Button buttondone;
    public ImageView profile_image;
    public SharedPreferences preferences;
    public int id;
    public String path = "";
    public String origPath;
    public String[] area_array = { "Khadia", "Kalupur", "Dariyapur", "Shahpur", "Raykhad", "Jamalpur", "Dudheshwar", "Madhupura", "Girdharnagar",
            "Rajpur", "Arbudanagar", "Odhav", "Vastral", "Mahavirnagar", "Bhaipura", "Amraiwadi", "Ramol", "Hathijan",
            "Paldi", "Vasna", "Ambawadi", "Navrangpura", "Juna Vadaj", "Nava Vadaj", "Naranpura", "Stadium", "Sabarmati", "Chandkheda", "Motera", "Stadium", "Sabarmati",
            "Saraspur", "Sardarnagar", "Noblenagar", "Naroda", "Kubernagar", "Saijpur", "Meghaninagar", "Asarva", "Naroda Road", "India Colony", "Krushnanagar", "Thakkarnagar", "Saraspur",
            "Isanpur", "Lambha", "Maninagar", "Kankaria", "Behrampura", "Dani Limda", "Ghodasar", "Indrapuri", "Khokhra", "Vatva", "Isanpur", "Stadium", "Sabarmati",
            "Vejalpur", "Jodhpur", "Bodakdev", "Thaltej", "Ghatlodia", "Ranip", "Kali", "Gota", "Satellite"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        editname = (EditText) findViewById(R.id.etname);
        editemail = (EditText) findViewById(R.id.ettextemail);
        editlocality = (AutoCompleteTextView) findViewById(R.id.ettextlocality);
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,area_array);
        editlocality.setAdapter(adapter);
        editlocality.setDropDownBackgroundResource(R.drawable.autcomplete);
        editpass = (EditText) findViewById(R.id.etnewpass);
        editconfpass = (EditText) findViewById(R.id.etconfpass);
        buttondone = (Button) findViewById(R.id.bchange);




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
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 153);
            }
        });


        buttondone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String regexemail = ".+@.+\\..+";
                name = editname.getText().toString();
                mail = editemail.getText().toString();
                locality = editlocality.getText().toString();
                pass = editpass.getText().toString();
                confpass  = editconfpass.getText().toString();

                boolean result = checknull(name,locality);
                if(result){
                    if(pass.length()==0){
                       pass = preferences.getString("password","Default");
                       confpass= pass;
                    }
                    if(pass.length()>4) {
                        if (pass.matches(confpass)) {
                            if (mail.matches(regexemail)) {

                                new UpdateProfile().execute();

                            } else{
                                Toast.makeText(getApplicationContext(), "Enter valid email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Password mismatch", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                            Toast.makeText(getApplicationContext(),"Password too short",Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == 153) {
            Uri selectedImageUri = data.getData();
            path = getPath(selectedImageUri);

            Bitmap bitmap = BitmapFactory.decodeFile(path);

            profile_image.setImageBitmap(bitmap);
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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


            if (s.contains("not") || s.equals("")) {
                Toast.makeText(EditProfile.this, "error", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("name", name);
                editor.putString("locality", locality);
                editor.putString("img_loc", path);
                editor.putString("email", mail);

                editor.commit();

            }

        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
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
                    result = EntityUtils.toString(resEntity);
                    Log.e("response", result);
                    //Log.e("resp loc",""+resEntity.toString());
                } catch (Exception e) {
                    Log.e("resp", "exception in response");
                    e.printStackTrace();
                    return "";
                }

            } else {
                result = "Default";
            }
            try {
                //adding data
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/updateprofile.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("id", "" + id));
                pairs.add(new BasicNameValuePair("name", name));
                pairs.add(new BasicNameValuePair("password", pass));
                pairs.add(new BasicNameValuePair("emailid", mail));
                pairs.add(new BasicNameValuePair("locality", locality));
                //pairs.add(new BasicNameValuePair("phone", phones));
                pairs.add(new BasicNameValuePair("imgloc", result));

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

    public boolean checknull(String name, String area) {
        boolean res = false;
        if(name.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter the name", Toast.LENGTH_SHORT).show();
            return res;
        }else{
            if(area.matches("")) {
                Toast.makeText(getApplicationContext(), "Enter the area", Toast.LENGTH_SHORT).show();
                return res;
            }else {
                res=true;
                return res;
            }
        }
    }

}

