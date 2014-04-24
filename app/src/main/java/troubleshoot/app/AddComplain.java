package troubleshoot.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        iv = (ImageView) view.findViewById(R.id.sel_imagev);

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

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(it, 111);
            }
        });
        addDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Set listener, so that when the add details button is clicked than
                open the Complain 2 page */

                FragmentTransaction trans = getFragmentManager()
                        .beginTransaction();
                AddComplain2 adc2 = new AddComplain2();

                /* Passing the path of the selected image */
                Bundle b = new Bundle();
                b.putString("path", fileUri.getPath());
                adc2.setArguments(b);

                trans.replace(R.id.root_frame, adc2);
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();


            }
        });

        return view;
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Generating a new Image and returning the file
     * @param type Type of the image
     * @return
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
        SharedPreferences preferences = getActivity().
                getSharedPreferences("troubles", Context.MODE_PRIVATE);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        } else if (resultCode == getActivity().RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getActivity(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        } else if (requestCode == 111) {
            //The data returned from the camera activity
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            iv.setImageBitmap(BitmapFactory.decodeFile(fileUri.getPath(), options));

        } else {
            Log.e("else", "null");
            return;
        }

        iv.setVisibility(View.VISIBLE);
        addDetails.setVisibility(View.VISIBLE);
        Log.e("pathin 1", fileUri.getPath());

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
