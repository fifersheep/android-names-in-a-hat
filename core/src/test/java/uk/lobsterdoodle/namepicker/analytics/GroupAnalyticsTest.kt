package uk.lobsterdoodle.namepicker.analytics

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.storage.GroupCreationSuccessfulEvent

class GroupAnalyticsTest {
    lateinit var bus: EventBus
    lateinit var analytics: GroupAnalytics

    @Before
    fun setUp() {
        bus = mock()
        analytics = GroupAnalytics(bus)
    }

    @Test
    fun registers_on_bus() {
        verify(bus).register(analytics)
    }

    @Test
    fun on_GroupCreationSuccessfulEvent_post_AnalyticsEvent() {
        analytics.on(GroupCreationSuccessfulEvent(24L, "Group Name"))
        verify(bus).post(AnalyticsEvent(
                GroupAnalytics.Key.group_created,
                mapOf(
                        GroupAnalytics.Param.group_id to "24",
                        GroupAnalytics.Param.group_name to "Group Name")))
    }


}