package troubleshoot.app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Aagam Shah on 29/3/14.
 */
public class ComplainAdapter extends SimpleCursorAdapter {
    public ComplainAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

    }
}
