package uk.lobsterdoodle.namepicker.storage

interface RemoteDb {
    fun createUser(userId: String)

    fun editGroupDetails(groupId: Long, groupName: String)
    fun removeGroup(groupId: Long)

    fun addName(groupId: Long, nameId: Long, name: String)
    fun deleteName(groupId: Long, nameId: Long)
}
