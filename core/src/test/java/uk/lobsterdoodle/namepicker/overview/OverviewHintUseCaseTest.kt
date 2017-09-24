package uk.lobsterdoodle.namepicker.overview

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import uk.lobsterdoodle.namepicker.events.EventBus

class OverviewHintUseCaseTest {
    private lateinit var bus: EventBus
    private lateinit var usecase: OverviewHintUseCase

    @Before
    fun setUp() {
        bus = mock()
        usecase = OverviewHintUseCase(bus)
    }

    @Test
    fun `show add group tooltip when no groups retrieved`() {
        usecase.on(OverviewRetrievedEvent(emptyList()))
        verify(bus).post(ShowAddGroupHintEvent)
    }

    @Test
    fun `do not show add group tooltip when at least 1 group retrieved`() {
        usecase.on(OverviewRetrievedEvent(listOf(OverviewCardCellData(1, "", 1))))
        verify(bus, never()).post(ShowAddGroupHintEvent)
    }
}