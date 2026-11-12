package uk.lobsterdoodle.namepicker.shared.data

import android.content.Context
import androidx.room3.Room

/**
 * Opens the app's existing database (`Classrooms.db` in the standard database directory,
 * where 2.7.0 left it), migrating it to the current schema on first use.
 * Call once per process; see [NamesStorage].
 */
fun NamesStorage(context: Context): NamesStorage = NamesStorage(
    Room.databaseBuilder<NamesDatabase>(context = context.applicationContext, name = DATABASE_NAME)
        .buildNamesDatabase(),
)
