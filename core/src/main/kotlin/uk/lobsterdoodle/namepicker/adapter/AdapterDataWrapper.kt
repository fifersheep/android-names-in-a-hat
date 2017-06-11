package uk.lobsterdoodle.namepicker.adapter

import java.util.ArrayList
import java.util.Collections

open class AdapterDataWrapper<T> {
    private val data = Collections.synchronizedList(ArrayList<T>())

    fun count(): Int {
        synchronized(this.data) {
            return this.data.size
        }
    }

    fun item(position: Int): T {
        synchronized(this.data) {
            return this.data[position]
        }
    }

    fun data(): List<T> {
        synchronized(this.data) {
            return ArrayList(this.data)
        }
    }

    fun replaceItem(position: Int, item: T) {
        synchronized(this.data) {
            this.data.set(position, item)
        }
    }

    fun replaceData(data: List<T>): List<T> {
        synchronized(this.data) {
            this.data.clear()
            this.data.addAll(data)
            return this.data
        }
    }

    fun modifyData(modification: Function1<T, T>) {
        synchronized(this.data) {
            for (item in this.data) {
                this.data[this.data.indexOf(item)] = modification.invoke(item)
            }
        }
    }
}
