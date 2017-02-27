package uk.lobsterdoodle.namepicker.adapter;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterDataWrapper<T> {
    final private List<T> data = Collections.synchronizedList(new ArrayList<T>());

    public int count() {
        return data.size();
    }

    public T item(int position) {
        return data.get(position);
    }

    public List<T> data() {
        return data;
    }

    public void replaceItem(int position, T item) {
        data.set(position, item);
    }

    public void replaceData(List<T> data) {
        synchronized (this.data) {
            this.data.clear();
            this.data.addAll(data);
        }
    }

    public void modifyData(Function<T, T> modification) {
        synchronized (data) {
            for (T item : data) {
                data.set(data.indexOf(item), modification.apply(item));
            }
        }
    }
}
