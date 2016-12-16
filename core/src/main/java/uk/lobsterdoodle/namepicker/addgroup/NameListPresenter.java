package uk.lobsterdoodle.namepicker.addgroup;

import java.util.List;

import javax.inject.Inject;

import static java.util.Arrays.asList;

public class NameListPresenter {

    private List<String> stubbedData = asList("Scott", "Rob", "Peter");

    @Inject
    public NameListPresenter() {
    }

    public NameCardCellData dataFor(int position) {
        return new NameCardCellData(stubbedData.get(position));
    }

    public int itemCount() {
        return 3;
    }
}
