package uk.lobsterdoodle.namepicker.selection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;

public class SelectionActivity extends AppCompatActivity {
    private static final String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";

    @InjectView(R.id.selection_tmp)
    TextView tmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        final long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        tmp.setText(String.format("Selection for %s", String.valueOf(groupId)));
    }

    public static Intent launchIntent(Context context, long groupId) {
        final Intent intent = new Intent(context, SelectionActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }
}
