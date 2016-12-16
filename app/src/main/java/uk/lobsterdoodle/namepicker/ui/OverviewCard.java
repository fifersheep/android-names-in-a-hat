package uk.lobsterdoodle.namepicker.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.util.As;
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData;

public class OverviewCard extends CardView {

    @InjectView(R.id.overview_card_title)
    TextView title;

    @InjectView(R.id.overview_card_count)
    TextView count;

    public OverviewCard(Context context) {
        super(context);
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ButterKnife.inject(this, LayoutInflater.from(getContext()).inflate(R.layout.overview_card, this, true));
        setCardBackgroundColor(getContext().getResources().getColor(android.R.color.white));
        setUseCompatPadding(true);
        setCardElevation(As.px(getContext(), 2));
    }

    public void bind(OverviewCardCellData data) {
        this.title.setText(data.listTitle);
        this.count.setText(String.valueOf(data.nameCount));
    }
}
