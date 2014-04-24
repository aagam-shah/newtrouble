package troubleshoot.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Aagam Shah on 29/3/14.
 * After the complain is sucessfully added,show the complain id.
 */
public class SuccessComplain extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.success, container, false);
        String id = getArguments().getString("complainNo");
        TextView tv =(TextView)view.findViewById(R.id.cno);
        tv.setText(id);
        Button ok = (Button)view.findViewById(R.id.successbutt);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction trans = getFragmentManager()
                        .beginTransaction();
                AddComplain complain=new AddComplain();
                trans.replace(R.id.root_frame, complain);
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
