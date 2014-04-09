package troubleshoot.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aagam Shah on 25/3/14.
 */
public class MyComplainsFragment extends Fragment {
    public static ListView lv;
    public ComplainAdapter adapter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.e("refresh", "action");
                ConnectionDetector detector = new ConnectionDetector(getActivity());
                if (detector.isConnectingToInternet()) {
                    Toast.makeText(getActivity(), "Refreshing", Toast.LENGTH_SHORT).show();
                    new DownloadAll().execute();
                } else
                    Toast.makeText(getActivity(), "Can't connect to internet", Toast.LENGTH_SHORT).show();
                // Not implemented here
                return false;
            default:
                break;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_complains, container, false);
        lv = (ListView) view.findViewById(R.id.complains_list);
        DB db = new DB(getActivity());
        Cursor c = db.getList();
        setHasOptionsMenu(true);
        adapter = new ComplainAdapter(getActivity(), R.layout.complain_item,
                c, new String[]{"_id", "title", "status", "datecreated"},
                new int[]{R.id.item_hidden, R.id.item_title, R.id.item_status
                        , R.id.item_date}
        );

        lv.setAdapter(adapter);
        ConnectionDetector detector = new ConnectionDetector(getActivity());
        if (detector.isConnectingToInternet())
            new DownloadAll().execute();
        else
            Toast.makeText(getActivity(), "Can't connect to internet", Toast.LENGTH_SHORT).show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent it = new Intent(getActivity(), FullComplain.class);
                it.putExtra("id", (int) l);
                getActivity().startActivity(it);

                //long is the id received which can be used for starting new activity

                TextView tv = (TextView) view.findViewById(R.id.item_hidden);
                TextView tv1 = (TextView) view.findViewById(R.id.item_title);
                Log.e("i", "" + i);
                Log.e("l", "" + l);
                Log.e("hide", "" + tv.getText().toString());
                Log.e("titl", "" + tv1.getText().toString());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("MYCOMPL", "RESUME");
        Cursor c = new DB(getActivity()).getList();
        // db.close();
        adapter = new ComplainAdapter(getActivity(), R.layout.complain_item,
                c, new String[]{"_id", "title", "status", "datecreated"},
                new int[]{R.id.item_hidden, R.id.item_title, R.id.item_status
                        , R.id.item_date}
        );
        lv.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class DownloadAll extends AsyncTask<String, String, String> {
        String result = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences preferences = getActivity().
                        getSharedPreferences("troubles", Context.MODE_PRIVATE);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(
                        "https://blog-aagam.rhcloud.com/getmycomplains.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();

                // pairs.add(new BasicNameValuePair("userid", "" + preferences.getInt("id", 0)));
                pairs.add(new BasicNameValuePair("userid", "" + preferences.getInt("id", 0)));
                httpPost.setEntity(new UrlEncodedFormEntity(pairs));

                HttpResponse response = httpClient.execute(httpPost);

                result = EntityUtils.toString(response.getEntity());

            } catch (Exception e) {
                Log.e("error", "fetching all");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.e("mycom", "" + s);
            try {
                JSONArray array = new JSONArray(s);
                DB db = new DB(getActivity());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put("complainid", Integer.parseInt(jsonObject.getString("complainid")));
                    values.put("title", jsonObject.getString("title"));
                    values.put("description", jsonObject.getString("description"));
                    values.put("locality", jsonObject.getString("locality"));
                    values.put("img_loc", "Default");
                    values.put("img_ol", "https://blog-aagam.rhcloud.com/" + jsonObject.getString("imageloc"));
                    values.put("status", jsonObject.getString("status"));
                    values.put("datecreated", jsonObject.getString("datetime"));

                    db.addORinsert(values, Integer.parseInt(jsonObject.getString("complainid")));

                }
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("error", "yoo");
            }
            Cursor c = new DB(getActivity()).getList();
            // db.close();
            adapter = new ComplainAdapter(getActivity(), R.layout.complain_item,
                    c, new String[]{"_id", "title", "status", "datecreated"},
                    new int[]{R.id.item_hidden, R.id.item_title, R.id.item_status
                            , R.id.item_date}
            );
            lv.setAdapter(adapter);

            new DownloadAdminComplains().execute();
        }
    }

    class DownloadAdminComplains extends AsyncTask<String, String, String> {

        String result = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                SharedPreferences preferences = getActivity()
                        .getSharedPreferences("troubles", Context.MODE_PRIVATE);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(
                        "https://blog-aagam.rhcloud.com/getadmincomplains.php");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();

                pairs.add(new BasicNameValuePair("userid", "" + preferences.getInt("id", 0)));

                httpPost.setEntity(new UrlEncodedFormEntity(pairs));

                HttpResponse response = httpClient.execute(httpPost);

                result = EntityUtils.toString(response.getEntity());

            } catch (Exception e) {
                Log.e("error", "fetching all");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("result", "" + s);

            if (s.equals("nothing")) {
                Log.e("nothing", "");
            } else {
                try {
                    DB db = new DB(getActivity());
                    db.adminDrop();
                    JSONArray jsonArray = new JSONArray(s);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Log.e("added", "" + i);
                        JSONObject mini = jsonArray.getJSONObject(i);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("complainid", Integer.parseInt(mini.getString("complainid")));
                        db.changeStatus(Integer.parseInt(mini.getString("complainid")));
                        contentValues.put("date", mini.getString("deadline"));
                        contentValues.put("officer", mini.getString("agent"));
                        contentValues.put("officercomment", mini.getString("agentcomment"));
                        contentValues.put("reviewer", mini.getString("adminname"));
                        contentValues.put("reviewercomment", mini.getString("admincomment"));
                        db.addAdmin(contentValues);
                    }
                    db.close();

                    Cursor c = new DB(getActivity()).getList();
                    // db.close();
                    adapter = new ComplainAdapter(getActivity(), R.layout.complain_item,
                            c, new String[]{"_id", "title", "status", "datecreated"},
                            new int[]{R.id.item_hidden, R.id.item_title, R.id.item_status
                                    , R.id.item_date}
                    );
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                } catch (Exception e) {
                    Log.e("exception json", "" + s);
                    return;
                }
            }

        }
    }

}
