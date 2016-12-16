package uk.lobsterdoodle.namepicker.addgroup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.lobsterdoodle.namepicker.R;
import uk.lobsterdoodle.namepicker.application.App;
import uk.lobsterdoodle.namepicker.ui.DataSetChangedListener;

public class AddGroupFragment extends Fragment implements DataSetChangedListener {

    @InjectView(R.id.add_group_name_list)
    RecyclerView nameList;

    @Inject NameListPresenter presenter;

    private NameListAdapter nameListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get(getActivity()).component().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_group, container, false);
        ButterKnife.inject(this, view);
        nameListAdapter = new NameListAdapter();
        nameList.setLayoutManager(new LinearLayoutManager(getActivity()));
        nameList.setAdapter(nameListAdapter);
        return view;
    }

    @Override
    public void dataSetChanged() {
        getActivity().runOnUiThread(() -> nameListAdapter.notifyDataSetChanged());
    }

    public class NameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NameCardViewHolder(new NameCard(getContext()));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            NameCardViewHolder cardViewHolder = (NameCardViewHolder) holder;
            cardViewHolder.bind(presenter.dataFor(position));
        }

        @Override
        public int getItemCount() {
            return presenter.itemCount();
        }
    }

    public class NameCardViewHolder extends RecyclerView.ViewHolder {
        public NameCard view;

        public NameCardViewHolder(NameCard view) {
            super(view);
            this.view = view;
        }

        public void bind(NameCardCellData data) {
            view.bind(data);
        }
    }
}
