package database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object PosDatabase {
    fun init() {
        Database.connect(
            "jdbc:sqlite:pizza_pos.db",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(CategoriesTable)
        }
    }

}