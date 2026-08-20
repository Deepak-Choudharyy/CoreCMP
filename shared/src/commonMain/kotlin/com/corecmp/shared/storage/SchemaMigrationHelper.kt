package com.corecmp.shared.storage

/**
 * Builder for ordered schema migrations (PLN-157).
 */
class SchemaMigrationHelper(
    private val store: LocalDataStore,
) {
    private val migrations = mutableListOf<SchemaMigration>()

    fun fromTo(from: Int, to: Int, block: (LocalDataStore) -> Unit): SchemaMigrationHelper {
        migrations += object : SchemaMigration {
            override val fromVersion: Int = from
            override val toVersion: Int = to
            override fun migrate(store: LocalDataStore) = block(store)
        }
        return this
    }

    fun migrateTo(targetVersion: Int) {
        store.migrate(targetVersion, migrations)
    }
}
