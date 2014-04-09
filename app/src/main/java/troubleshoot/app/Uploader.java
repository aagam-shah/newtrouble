package troubleshoot.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.File;

/**
 * Created by Aagam Shah on 25/3/14.
 */
public class Uploader extends AsyncTask {

    public String path;
    public Context ctx;
    public ProgressDialog pdg;
    public Uploader(String s,Context ct){
        path=s;
        ctx=ct;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        pdg = ProgressDialog.show(ctx,"Uploading","Message",true,true);
        Toast.makeText(ctx,"Uploading..",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Object o) {
       // super.onPostExecute(o);
      //  pdg.dismiss();


    }

    @Override
    protected Object doInBackground(Object[] objects) {

        File sourceFile = new File(path);
        if(!sourceFile.isFile()){
            //pdg.dismiss();
            Log.e("app"+path,""+path);
            Log.e("app","error");
            return null;
        }
        Log.e("app","success");


        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost("https://blog-aagam.rhcloud.com/img_upload.php");
        //File file = new File(filepath);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(sourceFile, "image/jpg");
        mpEntity.addPart("userfile", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        try {
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity resEntity = response.getEntity();
            Log.e("resp", "" + EntityUtils.toString(resEntity));

            //Log.e("resp",""+resEntity.toString());
        }catch (Exception e){
            Log.e("resp","exception in response");
            e.printStackTrace();
        }

        return null;
    }
}
