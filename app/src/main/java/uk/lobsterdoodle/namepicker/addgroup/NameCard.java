package uk.lobsterdoodle.namepicker.addgroup;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.util.As;
import uk.lobsterdoodle.namepicker.edit.NameCardActions;
import uk.lobsterdoodle.namepicker.model.Name;

public class NameCard extends CardView {

    @BindView(R.id.name_card_name)
    TextView name;

    @BindView(R.id.name_card_delete_icon)
    ImageView delete;

    public NameCard(Context context) {
        super(context);
        init();
    }

    public NameCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NameCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ButterKnife.bind(this, LayoutInflater.from(getContext()).inflate(R.layout.name_card, this, true));
        setCardBackgroundColor(getContext().getResources().getColor(android.R.color.white));
        setUseCompatPadding(true);
        setCardElevation(As.px(getContext(), 2));
    }

    public void bind(NameCardActions nameCardActions, Name name) {
        this.name.setText(name.getName());
        delete.setOnClickListener(v -> nameCardActions.deleteName(name.getId()));
    }
}
