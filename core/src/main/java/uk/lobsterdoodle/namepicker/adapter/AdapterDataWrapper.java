package uk.lobsterdoodle.namepicker.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.jvm.functions.Function1;

public class AdapterDataWrapper<T> {
    final private List<T> data = Collections.synchronizedList(new ArrayList<T>());

    public int count() {
        synchronized (this.data) {
            return this.data.size();
        }
    }

    public T item(int position) {
        synchronized (this.data) {
            return this.data.get(position);
        }
    }

    public List<T> data() {
        synchronized (this.data) {
            return new ArrayList<>(this.data);
        }
    }

    public void replaceItem(int position, T item) {
        synchronized (this.data) {
            this.data.set(position, item);
        }
    }

    public List<T> replaceData(List<T> data) {
        synchronized (this.data) {
            this.data.clear();
            this.data.addAll(data);
            return this.data;
        }
    }

    public void modifyData(Function1<T, T> modification) {
        synchronized (this.data) {
            for (T item : this.data) {
                this.data.set(this.data.indexOf(item), modification.invoke(item));
            }
        }
    }
}
