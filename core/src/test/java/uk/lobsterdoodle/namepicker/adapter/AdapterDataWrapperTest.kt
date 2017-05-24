package uk.lobsterdoodle.namepicker.adapter

import org.junit.Before
import org.junit.Test

import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat

class AdapterDataWrapperTest {
    private lateinit var wrapper: AdapterDataWrapper<TestClass>

    @Before
    fun setUp() {
        wrapper = AdapterDataWrapper<TestClass>()
    }

    @Test
    fun get_count() {
        wrapper.replaceData(listOf(TestClass(1), TestClass(2), TestClass(3)))
        assertThat(wrapper.count(), `is`(equalTo(3)))
    }

    @Test
    fun get_item() {
        wrapper.replaceData(listOf(TestClass(1), TestClass(2), TestClass(3)))
        assertThat(wrapper.item(0), `is`(equalTo(TestClass(1))))
        assertThat(wrapper.item(1), `is`(equalTo(TestClass(2))))
        assertThat(wrapper.item(2), `is`(equalTo(TestClass(3))))
    }

    @Test
    fun replace_item() {
        wrapper.replaceData(listOf(TestClass(1), TestClass(2), TestClass(3)))
        wrapper.replaceItem(1, TestClass(4))
        assertThat(wrapper.item(0), `is`(equalTo(TestClass(1))))
        assertThat(wrapper.item(1), `is`(equalTo(TestClass(4))))
        assertThat(wrapper.item(2), `is`(equalTo(TestClass(3))))
    }

    @Test
    fun replace_data() {
        wrapper.replaceData(listOf(TestClass(1), TestClass(2), TestClass(3)))
        wrapper.replaceData(listOf(TestClass(4), TestClass(5), TestClass(6)))
        assertThat(wrapper.item(0), `is`(equalTo(TestClass(4))))
        assertThat(wrapper.item(1), `is`(equalTo(TestClass(5))))
        assertThat(wrapper.item(2), `is`(equalTo(TestClass(6))))
    }

    @Test
    fun modify_data() {
        wrapper.replaceData(listOf(TestClass(1), TestClass(2), TestClass(3)))
        wrapper.modifyData { input -> TestClass(input!!.id + 3) }
        assertThat(wrapper.item(0), `is`(equalTo(TestClass(4))))
        assertThat(wrapper.item(1), `is`(equalTo(TestClass(5))))
        assertThat(wrapper.item(2), `is`(equalTo(TestClass(6))))
    }

    private data class TestClass(val id: Int)
}