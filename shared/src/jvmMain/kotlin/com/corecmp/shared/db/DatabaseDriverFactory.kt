package com.corecmp.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File("corecmp.db")
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        
        // Only create the schema if the file didn't exist or is empty
        if (!dbFile.exists() || dbFile.length() == 0L) {
            CoreCmpDatabase.Schema.create(driver)
        }
        
        return driver
    }
}
