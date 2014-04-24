package troubleshoot.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Aagam Shah on 26/3/14.
 * Wrapping the fragments of AddComplain1 & AddComplain2 in this fragment
 */
public class ComplainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frame, container, false);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.root_frame, new AddComplain());

        transaction.commit();

        return view;
    }
}
