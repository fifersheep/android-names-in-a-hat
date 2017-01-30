package uk.lobsterdoodle.namepicker.selection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.model.Name;

public class NameSelectionView extends FrameLayout {
    @InjectView(R.id.name_selection_view_checkbox)
    CheckBox checkBox;

    public NameSelectionView(Context context) {
        super(context);
        init(context);
    }

    public NameSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NameSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        App.get(context).component().inject(this);
        ButterKnife.inject(this, LayoutInflater.from(context).inflate(R.layout.name_selection_view, this, true));
    }

    public void bind(Name name, CompoundButton.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setText(name.name);
        checkBox.setChecked(name.toggledOn);
        checkBox.setOnCheckedChangeListener(listener);
    }
}
