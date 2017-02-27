package uk.lobsterdoodle.namepicker.adapter;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AdapterDataWrapperTest {
    private AdapterDataWrapper<TestClass> wrapper;

    @Before
    public void setUp() {
        wrapper = new AdapterDataWrapper<>();
    }

    @Test
    public void get_count() {
        wrapper.replaceData(asList(testClass(1), testClass(2), testClass(3)));
        assertThat(wrapper.count(), is(equalTo(3)));
    }

    @Test
    public void get_item() {
        wrapper.replaceData(asList(testClass(1), testClass(2), testClass(3)));
        assertThat(wrapper.item(0), is(equalTo(testClass(1))));
        assertThat(wrapper.item(1), is(equalTo(testClass(2))));
        assertThat(wrapper.item(2), is(equalTo(testClass(3))));
    }

    @Test
    public void replace_item() {
        wrapper.replaceData(asList(testClass(1), testClass(2), testClass(3)));
        wrapper.replaceItem(1, testClass(4));
        assertThat(wrapper.item(0), is(equalTo(testClass(1))));
        assertThat(wrapper.item(1), is(equalTo(testClass(4))));
        assertThat(wrapper.item(2), is(equalTo(testClass(3))));
    }

    @Test
    public void replace_data() {
        wrapper.replaceData(asList(testClass(1), testClass(2), testClass(3)));
        wrapper.replaceData(asList(testClass(4), testClass(5), testClass(6)));
        assertThat(wrapper.item(0), is(equalTo(testClass(4))));
        assertThat(wrapper.item(1), is(equalTo(testClass(5))));
        assertThat(wrapper.item(2), is(equalTo(testClass(6))));
    }

    @Test
    public void modify_data() {
        wrapper.replaceData(asList(testClass(1), testClass(2), testClass(3)));
        wrapper.modifyData(input -> new TestClass(input.id + 3));
        assertThat(wrapper.item(0), is(equalTo(testClass(4))));
        assertThat(wrapper.item(1), is(equalTo(testClass(5))));
        assertThat(wrapper.item(2), is(equalTo(testClass(6))));
    }

    private TestClass testClass(int id) {
        return new TestClass(id);
    }

    private class TestClass {
        final int id;

        TestClass(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestClass)) return false;

            TestClass testClass = (TestClass) o;

            return id == testClass.id;

        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "id=" + id +
                    '}';
        }
    }
}