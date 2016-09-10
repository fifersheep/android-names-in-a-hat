package uk.lobsterdoodle.namepicker.overview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import uk.lobsterdoodle.namepicker.R;

public class OverviewActivity extends AppCompatActivity implements OverviewView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        OverviewPresenter presenter = new OverviewPresenter();
        presenter.onViewCreated(this);
    }

    @Override
    public void toastPresenterCreated() {
        Toast.makeText(OverviewActivity.this, "Presenter Created!", Toast.LENGTH_LONG).show();
    }
}