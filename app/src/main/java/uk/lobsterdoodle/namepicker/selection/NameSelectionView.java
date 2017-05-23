package uk.lobsterdoodle.namepicker.selection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.model.Name;

public class NameSelectionView extends FrameLayout {

    @BindView(R.id.name_selection_view_checkbox)
    CheckedTextView checkBox;

    private CheckedChangeListener listener;

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
        ButterKnife.bind(this, LayoutInflater.from(context).inflate(R.layout.name_selection_view, this, true));
        setOnClickListener(v -> {
            checkBox.toggle();
            if (listener != null) {
                listener.onCheckedChanged(checkBox.isChecked());
            }
        });
    }

    public void bind(Name name, CheckedChangeListener listener) {
        this.listener = listener;
        checkBox.setText(name.getName());
        checkBox.setChecked(name.getToggledOn());
    }
}
