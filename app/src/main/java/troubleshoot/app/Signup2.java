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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Signup2 extends Activity {

    public Button submit;
    public ImageButton imgbutton;
    public String emails, passws, phones, locals, names;
    public Uri fileUri;
    private static final String IMAGE_DIRECTORY_NAME = "TroubleShooter";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public Context context = this;
    public String path = "";


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

    /**
     * Show the image after selecting the image from the picker dialog
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (resultCode == Activity.RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getApplicationContext(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        }

        //Display the image according to the resolution of the phone
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        if (requestCode == 133) {
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);

            Bitmap bitmap = BitmapFactory.decodeFile(imagepath, options);

            imgbutton.setImageBitmap(bitmap);
            submit.setEnabled(true);
            Log.e("pathin 1", imagepath);
            Toast.makeText(getApplicationContext(), "path: " + imagepath, Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 112) {

            imagepath = path;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            submit.setEnabled(true);
            imgbutton.setImageBitmap(bitmap);
        }
    }

    /**
     * Get the URI of the type of image
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create new file with the @type of image and return it
     */
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
     * Get path of the image selected from the gallery/camera
     */
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    /**
     * Send all the data online and accordingly display the message.
     * It might happen that the phone no exists.So show error accordingly
     *
     */
    class SignUp extends AsyncTask<String, String, String> {
        public ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(Signup2.this, "", "Signing up...");
            Log.e("pre", "yoo");
        }


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

            // Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            Bitmap bmp = decodeSampledBitmapFromResource(imagepath, 640, 480);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            bmp.recycle();
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
            //File file = new File(filepath);
            MultipartEntity mpEntity = new MultipartEntity();
            // ContentBody cbFile = new FileBody(sourceFile, "image/jpg");
            ContentBody cbFile = new InputStreamBody(inputStream, "profile" + phones);
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
            if (!(s == null || s.toLowerCase().equals("exist"))) {
                SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("id", Integer.parseInt(s));
                editor.putString("name", names);
                editor.putString("locality", locals);
                editor.putString("img_loc", imagepath);
                editor.putString("img_ol", "https://blog-aagam.rhcloud.com/" + retlocation);
                editor.putString("phone", phones);
                editor.putString("email", emails);
                editor.putString("password", passws);
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

                if (s.toLowerCase().equals("exist"))
                    Toast.makeText(Signup2.this, "Phone number already exist", Toast.LENGTH_SHORT);
                else
                    Toast.makeText(Signup2.this, "Error signing up", Toast.LENGTH_SHORT);
            }

        }
    }

}

