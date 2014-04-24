package troubleshoot.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

/**
 * The activity which handle the login of the app.
 * Main functionalities:
 * 1. Login
 * 2. Forgot Password
 */
public class LoginActivity extends Activity {

    TextView phoneno, password;
    String phone, pass;
    Button login, register;
    ImageButton forgotpassword;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        forgotpassword = (ImageButton) findViewById(R.id.ibforgotpass);

        // dialog for forgot password
        forgotpassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder d = new AlertDialog.Builder(context);
                d.setIcon(R.drawable.am_logo1);
                d.setTitle("Forgot Password?");
                d.setMessage("Enter your contact no. in the field below. The password details and further instructions will be sent to your registered email-id");
                final EditText input = new EditText(context);
                d.setView(input);
                d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString();
                        ConnectionDetector detector = new ConnectionDetector(getApplicationContext());
                        if (detector.isConnectingToInternet()) {
                            new ForgetPass(value).execute();
                        } else {
                            Toast.makeText(getApplicationContext(), "No connection"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        // do something
                        // continue
                    }
                });
                d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                d.show();
            }
        });

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

    /**
     * Send the email to the phone number entered in the dialog box
     */
    class ForgetPass extends AsyncTask<String, String, String> {
        public String phoneno = "";

        public ForgetPass(String value) {
            phoneno = value;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.contains("sent!"))
                Toast.makeText(getApplicationContext(), "Please check your mail", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(getApplicationContext(), "No registered phone no. found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://blog-aagam.rhcloud.com" +
                    "/sendmail.php?phone=" + phoneno);

            try {
                HttpResponse resp = client.execute(post);

                String response = EntityUtils.toString(resp.getEntity());
                Log.e("response of conf", "" + response);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            // return null;
        }
    }

    /**
     * Check the credentials entered by the user online
     * and save the details like the locality,email id locally in the app
     * <p/>
     * Create the Database. And initiate all the variables like champion
     */
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
                    editor.putString("password", pass);
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
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
            }

        }
    }


}



