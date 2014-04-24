package troubleshoot.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vandit on 4/12/2014.
 * Show the preloaded values of the profile and the ability to modify the profile and update it
 */
public class EditProfile extends Activity {

    public Uri fileUri;
    private static final String IMAGE_DIRECTORY_NAME = "TroubleShooter";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public EditText editname, editemail, editpass, editconfpass;
    public AutoCompleteTextView editlocality;
    public String name, locality, mail, pass, confpass;
    public Button buttondone;
    public ImageView profile_image;
    public SharedPreferences preferences;
    public Context context = this;
    public int id;
    public String path = "";
    public String origPath;
    public String[] area_array = {"Khadia", "Kalupur", "Dariyapur", "Shahpur", "Raykhad", "Jamalpur", "Dudheshwar", "Madhupura", "Girdharnagar",
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
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, area_array);
        editlocality.setAdapter(adapter);
        editlocality.setDropDownBackgroundResource(R.drawable.autcomplete);
        editpass = (EditText) findViewById(R.id.etnewpass);
        editconfpass = (EditText) findViewById(R.id.etconfpass);
        buttondone = (Button) findViewById(R.id.bchange);
        preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
        profile_image = (ImageView) findViewById(R.id.profile);

        //putting current values of the profile
        editname.setText(preferences.getString("name", "Default"));
        editemail.setText(preferences.getString("email", "Default"));
        editlocality.setText(preferences.getString("locality", "Default"));
        id = preferences.getInt("id", 0);
        String imgloc = preferences.getString("img_loc", "Default");
        path = imgloc;
        origPath = imgloc;
        Bitmap bitmap = BitmapFactory.decodeFile(imgloc);
        profile_image.setImageBitmap(bitmap);

        //on click listener on imageview, show the chooser dialog of camera & gallery
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder d = new AlertDialog.Builder(context);
                d.setItems(R.array.select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, 133);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            startActivityForResult(intent, 112);
                        }
                    }
                });

                d.show();
            }
        });

        // on click listener on done button,update the profile online also
        buttondone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String regexemail = ".+@.+\\..+";
                name = editname.getText().toString();
                mail = editemail.getText().toString();
                locality = editlocality.getText().toString();
                pass = editpass.getText().toString();
                confpass = editconfpass.getText().toString();

                boolean result = checknull(name, locality);
                if (result) {
                    if (pass.length() == 0) {
                        pass = preferences.getString("password", "Default");
                        confpass = pass;
                    }
                    if (pass.length() > 4) {
                        if (pass.matches(confpass)) {
                            if (mail.matches(regexemail)) {

                                new UpdateProfile().execute();
                                Intent openprofile = new Intent(getApplicationContext(), Dashboard.class);
                                startActivity(openprofile);

                            } else {
                                Toast.makeText(getApplicationContext(), "Enter valid email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Password mismatch", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Password too short", Toast.LENGTH_SHORT).show();
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
        if (requestCode == 133) {
            Uri selectedImageUri = data.getData();
            path = getPath(selectedImageUri);

            Bitmap bitmap = BitmapFactory.decodeFile(path);

            profile_image.setImageBitmap(bitmap);
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getApplicationContext(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        }
        if (requestCode == 112) {
            //Uri selectedImageUri = data.getData();
            //path = getPath(selectedImageUri);

            Bitmap bitmap = BitmapFactory.decodeFile(path);

            profile_image.setImageBitmap(bitmap);

        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    //returning image / video
    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME
        );

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new java.util.Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "TS_" + preferences.getInt("id", 0) + "_" + timeStamp + ".jpg");
            path = mediaStorageDir.getPath() + File.separator
                    + "TS_" + preferences.getInt("id", 0) + "_" + timeStamp + ".jpg";
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Get path of the image clicked or selected from the gallery
     */

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Update the profile online also asynchronously
     */
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
                editor.putString("password", pass);
                editor.commit();

            }

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
        protected String doInBackground(String... strings) {
            String result;
            //uploading image first and saving its returned location
            if (!path.equals(origPath)) {
                File sourceFile = new File(path);
                if (!sourceFile.isFile()) {
                    //pdg.dismiss();
                    Log.e("app" + path, "" + path);
                    Log.e("app", "error");
                    return null;
                }

                //Scale the image to 640x480 resolution
                Bitmap bmp = decodeSampledBitmapFromResource(path, 640, 480);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                //Compress it by 50%
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                bmp.recycle();
                InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
                MultipartEntity mpEntity = new MultipartEntity();
                ContentBody cbFile = new InputStreamBody(inputStream, "ID_champion" + id);
                mpEntity.addPart("userfile", cbFile);
                httppost.setEntity(mpEntity);
                try {
                    HttpResponse response = httpclient.execute(httppost);

                    HttpEntity resEntity = response.getEntity();
                    //Get the upload image location
                    result = EntityUtils.toString(resEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }

            } else {
                result = "Default";
            }
            try {
                //adding data of the modified profile to be update online
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/updateprofile.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("id", "" + id));
                pairs.add(new BasicNameValuePair("name", name));
                pairs.add(new BasicNameValuePair("password", pass));
                pairs.add(new BasicNameValuePair("emailid", mail));
                pairs.add(new BasicNameValuePair("locality", locality));
                pairs.add(new BasicNameValuePair("imgloc", result));
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = client.execute(post);
                result = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                return null;
            }

            return result;

        }
    }

    /**
     * Verify that the input data is not empty
     */
    public boolean checknull(String name, String area) {
        boolean res = false;
        if (name.matches("")) {
            Toast.makeText(getApplicationContext(), "Enter the name", Toast.LENGTH_SHORT).show();
            return res;
        } else {
            if (area.matches("")) {
                Toast.makeText(getApplicationContext(), "Enter the area", Toast.LENGTH_SHORT).show();
                return res;
            } else {
                res = true;
                return res;
            }
        }
    }

}

