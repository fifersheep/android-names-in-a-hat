package uk.lobsterdoodle.namepicker.storage

import uk.lobsterdoodle.namepicker.model.Group
import uk.lobsterdoodle.namepicker.model.GroupDetails
import uk.lobsterdoodle.namepicker.model.Name

interface DbHelper {
    val allGroups: List<Group>
    fun createGroup(groupName: String): Long
    fun addNameToGroup(groupId: Long, name: String): Long
    fun retrieveGroupNames(groupId: Long): List<Name>
    fun retrieveGroupDetails(groupId: Long): GroupDetails?
    fun removeName(nameId: Long): Name?
    fun removeGroup(groupId: Long): GroupDetails?
    fun editGroupName(groupId: Long, newName: String)
    fun updateName(name: Name)
    fun toggleAllNamesInGroup(groupId: Long, toggleOn: Boolean)
}
