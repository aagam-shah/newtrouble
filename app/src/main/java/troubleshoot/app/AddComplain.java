package troubleshoot.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Aagam Shah on 25/3/14.
 */
public class AddComplain extends Fragment {
    public ImageView iv;
    public Button addDetails;
    public Uri fileUri;
    public String path;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "TroubleShooter";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_complain, container, false);
        addDetails = (Button) view.findViewById(R.id.addDetails);
        Button click = (Button) view.findViewById(R.id.sel_image);
        iv = (ImageView) view.findViewById(R.id.sel_imagev);
        //   iv.setVisibility(View.INVISIBLE);
        addDetails.setVisibility(View.INVISIBLE);
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // iv.setVisibility(View.INVISIBLE);
                iv.setImageResource(R.drawable.sel);
                addDetails.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        addDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction trans = getFragmentManager()
                        .beginTransaction();
                AddComplain2 adc2 = new AddComplain2();

                Bundle b = new Bundle();
                b.putString("path", fileUri.getPath());
                adc2.setArguments(b);

                trans.replace(R.id.root_frame, adc2);
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();
            }
        });

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                it.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
                startActivityForResult(it, 111);
            }
        });

        return view;
    }

    public String imagepath;

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private  File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        SharedPreferences preferences = getActivity().
                getSharedPreferences("troubles", Context.MODE_PRIVATE);
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new java.util.Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "TS_"+preferences.getInt("id",0)+"_" + timeStamp + ".jpg");
            path=mediaStorageDir.getPath() + File.separator
                    + "TS_"+preferences.getInt("id",0)+"_" + timeStamp + ".jpg";
        }  else {
            return null;
        }

        return mediaFile;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getActivity(),""+data.getData().toString(),Toast.LENGTH_SHORT).show();
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else if (resultCode == getActivity().RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getActivity(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        }
        else if (requestCode == 111) {

            /*Uri selectedImageUri = data.getData();

            String filePath = "";
            imagepath = getPath(selectedImageUri);

            if (imagepath != null) {
                filePath = imagepath;
        /*        File f = new File(filePath);
                Log.e("orig size: "," "+f.length());*/
                Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                Log.e("compressed size 50: ", " " + outputStream.size());

                Bitmap bitmap1 = BitmapFactory.decodeFile(fileUri.getPath());
                ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG,40,outputStream2);
                Log.e("compressed size 40: "," "+outputStream2.size());

                Bitmap bmp = bitmap.createScaledBitmap(bitmap,480,480,true);
                ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG,20,outputStream1);


                Log.e("rescale size: "," "+outputStream1.size());
                //bmp.recycle();
                bitmap.recycle();
                Log.e("imagepath", fileUri.getPath());

            iv.setImageBitmap(bitmap1);

        } else {
                Log.e("else", "null");
                return;
        }



//            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
//            iv.setImageBitmap(bitmap);
            iv.setVisibility(View.VISIBLE);
            addDetails.setVisibility(View.VISIBLE);
          //  Log.e("pathin 1", imagepath);
            Log.e("pathin 1", fileUri.getPath());

    }

    public String getPath(Uri uri) {
        String res = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
       /* int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
       */
    }

    public void uploadImage() {
        Intent itn = new Intent();
        itn.setType("image/*");
        itn.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(itn, "Select Image"), 131);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
