package uk.lobsterdoodle.namepicker.storage

data class MassNameStateChangedEvent(val groupId: Long, val toggleOn: Boolean) {
    companion object Toggle {
        fun on(groupId: Long): MassNameStateChangedEvent {
            return MassNameStateChangedEvent(groupId, true)
        }

        fun off(groupId: Long): MassNameStateChangedEvent {
            return MassNameStateChangedEvent(groupId, false)
        }
    }
}
