package uk.lobsterdoodle.namepicker.selection

import org.greenrobot.eventbus.Subscribe
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.model.Name
import uk.lobsterdoodle.namepicker.storage.GroupNamesRetrievedEvent
import uk.lobsterdoodle.namepicker.storage.KeyValueStore
import java.util.*
import javax.inject.Inject
import kotlin.Comparator

class SelectionNamesUseCase @Inject
constructor(private val bus: EventBus, private val store: KeyValueStore) {

    private val nextSortFor = mapOf(
            NameSortType.NONE to NameSortType.ASC,
            NameSortType.ASC to NameSortType.DESC,
            NameSortType.DESC to NameSortType.ASC)

    private val comparatorsFor = mapOf(
            NameSortType.NONE to Comparator { _: Name, _: Name -> 0 },
            NameSortType.ASC to Comparator { first:Name, second:Name -> first.name.compareTo(second.name, true) },
            NameSortType.DESC to Comparator { first:Name, second:Name -> second.name.compareTo(first.name, true) })

    init { bus.register(this) }

    @Subscribe
    fun on(event: GroupNamesRetrievedEvent) {
        val key = "group_sort_type_for_group_${event.groupId}"
        Collections.sort(event.names, comparatorsFor[currentSortType(key)])
        bus.post(GroupNamesSortedEvent(event.names))
    }

    @Subscribe
    fun on(event: SortMenuItemSelectedEvent) {
        val key = "group_sort_type_for_group_${event.groupId}"
        val nextSortType = nextSortFor[currentSortType(key)]

        store.edit().put(key, nextSortType.toString()).commit()
        Collections.sort(event.names, comparatorsFor[nextSortType])
        bus.post(GroupNamesSortedEvent(event.names))
    }

    private fun currentSortType(key: String): NameSortType {
        return NameSortType.valueOf(store.getString(key, NameSortType.NONE.toString()))
    }
}
