package troubleshoot.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;



/**
 * Created by Mrunal Dave on 17-04-2014.
 */
public class Abt_us extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abtus);

        TextView textView =(TextView)findViewById(R.id.tvsite);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://www.ahmedabadmirror.com'> www.ahmedabadmirror.com </a>";
        textView.setText(Html.fromHtml(text));

        final TextView tv = (TextView)findViewById(R.id.tvnumber);
        tv.setClickable(true);
        
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:+" + tv.getText().toString().trim()));

                    startActivity(callIntent );
            }

        });
    }
}
