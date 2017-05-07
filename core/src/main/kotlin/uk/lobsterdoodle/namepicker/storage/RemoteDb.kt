package uk.lobsterdoodle.namepicker.storage

interface RemoteDb {
    fun createUser(userId: String)

    fun editGroupDetails(groupId: Long, groupName: String, nameCount: Int)
}
