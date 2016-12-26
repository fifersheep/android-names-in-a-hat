package uk.lobsterdoodle.namepicker.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.util.As;
import uk.lobsterdoodle.namepicker.overview.OverviewCardCellData;

public class OverviewCard extends CardView {
    private final Context context;

    @InjectView(R.id.overview_card_title)
    TextView title;

    @InjectView(R.id.overview_card_count)
    TextView count;

    @InjectView(R.id.overview_card_overflow)
    ImageView overflow;

    public OverviewCard(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
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
        this.count.setText(String.format("%s names", String.valueOf(data.nameCount)));

        PopupMenu popup = new PopupMenu(context, overflow);
        popup.inflate(R.menu.menu_overview_card);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.overview_card_overflow_edit_names:
                    Toast.makeText(context, "Edit Names for group " + data.groupId, Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return true;
            }
        });
        this.overflow.setOnClickListener(btn -> popup.show());
    }
}
