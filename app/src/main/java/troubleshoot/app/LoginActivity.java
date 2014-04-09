package troubleshoot.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    TextView phoneno, password;
    String phone, pass;
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);
        if (preferences.getInt("id", -1) != -1) {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.login);
        phoneno = (TextView) findViewById(R.id.etcontact);
        password = (TextView) findViewById(R.id.etpassword);

        login = (Button) findViewById(R.id.blogin);
        register = (Button) findViewById(R.id.bregister);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
                boolean isInternetConnected = cd.isConnectingToInternet();
                if (!isInternetConnected) {
                    Toast.makeText(getApplicationContext(), "Network Unavailable.Please try again later.", Toast.LENGTH_SHORT).show();
                } else {

                    if (!(phoneno.getText().toString().equals("") || password.getText().toString().equals(""))) {
                        phone = phoneno.getText().toString();
                        pass = password.getText().toString();
                        new LoginExec().execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent openSignup1 = new Intent(getApplicationContext(), Signup1.class);
                startActivity(openSignup1);
            }
        });
    }


    class LoginExec extends AsyncTask<String, String, String> {
        public ProgressDialog pdg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdg = ProgressDialog.show(LoginActivity.this, "", "Signing in");
        }

        @Override
        protected String doInBackground(String[] objects) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com/signin.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("phone", phone));
            pairs.add(new BasicNameValuePair("pass", pass));

            //pairs.add(new BasicNameValuePair("location",retlocation));

            String result = null;
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = client.execute(post);
                result = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pdg.dismiss();
            if (s != null || (!s.equals("wrong"))) {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    Log.e("userid", jsonObject.getString("userid"));


                    SharedPreferences preferences = getSharedPreferences("troubles", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("id", Integer.parseInt(jsonObject.getString("userid")));
                    editor.putString("name", jsonObject.getString("name"));
                    editor.putString("locality", jsonObject.getString("locality"));
                    editor.putString("img_loc", "Default");

                    String loc = jsonObject.getString("profilepic");
                    loc = "https://blog-aagam.rhcloud.com/" + loc;
                    Log.e("done json", "" + loc);
                    editor.putString("img_ol", loc);
                    editor.putString("phone", jsonObject.getString("phoneno"));
                    editor.putString("email", jsonObject.getString("emailid"));
                    //adding champion month as 0
                    editor.putInt("champion_month", 0);
                    editor.putInt("champion_year", 0);
                    editor.putString("champion_name", "Default");

                    editor.commit();
                    DB db = new DB(getApplicationContext());
                    db.drop();
                    db.close();

                    Intent dashboardactivity = new Intent(getApplicationContext(),
                            Dashboard.class);
                    startActivity(dashboardactivity);
                    finish();
                } catch (Exception e) {
                    Log.e("json", "error " + s);
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e("retur error", "" + s);
                Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }


}



