package troubleshoot.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Signup1 extends Activity {

    public Button cont;
    public EditText editphone, editpass,editconfpass, editemail, editname;
    public AutoCompleteTextView editloc;
    public String[] area_array = {"Bapu Nagar", "Prahlad Nagar", "C.G.Road", "S.G. Road", "Navrangpura", "Vastrapur", "Ashram Road",
            "Paldi", "Saraspur", "Satellite Area", "Sarangpur Darwaza", "Ambawadi", "Ellis Bridge", "Ghatlodia", "Gulbai Tekra",
            "Gita Mandir Road", "Mem Nagar", "Naranpura",  "University Area", "Mithakhali Six Roads", "Vadaj", "Vasana", "Naroda",
            "Narol", "Asarwa", "Meghani Nagar", "Ranip", "Bopal", "Sabarmati", "Shahibaug", "Astodia", "Dariapur", "Kadia",
            "Kalupur", "Lal Darwaza", "Raipur", "Shahpur", "Dani Limbada", "Jamalpur", "Kankaria", "Khokra", "Mehmedabad",
            "Odhav", "Maninagar", "Sanand", "Chandkheda", "Vijay Char Rasta", "Vejalpur", "Jivraj Park", "Shivaranjani",
            "Nehru Park","Bodakdev","Judges Bunglow", "Others"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* int ids = getSharedPreferences("troubles", Context.MODE_PRIVATE).getInt("id",-1);
        if(ids!=-1){
            Intent newIntent = new Intent(getApplicationContext(),Dashboard.class);
            startActivity(newIntent);
            finish();
        }
        */

        setContentView(R.layout.signup1);
        editemail = (EditText) findViewById(R.id.editemail);
        editpass = (EditText) findViewById(R.id.editpassword);
        editconfpass = (EditText) findViewById(R.id.editconfirmpassword);
        editphone = (EditText) findViewById(R.id.editphone);
       // editloc = (EditText) findViewById(R.id.editarea);
        editname = (EditText) findViewById(R.id.editname);
        editloc = (AutoCompleteTextView) findViewById(R.id.editarea);
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,area_array);
        editloc.setAdapter(adapter);


        cont = (Button) findViewById(R.id.bcontinue);
        cont.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String regex = "^[7-9]{1}[0-9]{9}$";
                String regexemail = ".+@.+\\..+";

                String name = editname.getText().toString();
                String area = editloc.getText().toString();
                String number  = editphone.getText().toString();
                String password  = editpass.getText().toString();
                String confpassword  = editconfpass.getText().toString();
                String email  = editemail.getText().toString();
                boolean result = checknull(name,area);
            if(result){
                if(number.matches(regex)) {
                    if(password.length()>4){
                        if(password.matches(confpassword)) {
                            if(email.matches(regexemail)){
                                Thread nextstep = new Thread() {
                                  public void run() {

                                    Intent openSignup2 = new Intent(getApplicationContext(), Signup2.class);
                                    openSignup2.putExtra("email", editemail.getText().toString());
                                    openSignup2.putExtra("pass", editpass.getText().toString());
                                    openSignup2.putExtra("phone", editphone.getText().toString());
                                    openSignup2.putExtra("loc", editloc.getText().toString());
                                    openSignup2.putExtra("name", editname.getText().toString());

                                    startActivity(openSignup2);

                                  }
                                };
                              nextstep.start();
                            } else{
                                Toast.makeText(getApplicationContext(),"Enter valid email",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                         Toast.makeText(getApplicationContext(),"Password mismatch",Toast.LENGTH_SHORT).show();
                        }
                    } else{
                        Toast.makeText(getApplicationContext(),"Password too short",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Enter valid contact number",Toast.LENGTH_SHORT).show();
                }
            }
            }
        });
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }


}

