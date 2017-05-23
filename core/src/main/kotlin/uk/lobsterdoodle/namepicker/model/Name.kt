package uk.lobsterdoodle.namepicker.model

data class Name(val id: Long, val name: String, val toggledOn: Boolean) {

    fun copyWith(toggledOn: Boolean): Name {
        return Name(id, name, toggledOn)
    }
}
