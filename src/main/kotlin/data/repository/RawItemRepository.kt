package data.repository

import data.model.RawItem
import database.InventoryTransactions
import database.RawItems
import database.TransactionType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface RawItemRepository {
    suspend fun createRawItem(rawItem: RawItem)
    suspend fun getRawItem(id: Int): RawItem?
    suspend fun getAllRawItems(): List<RawItem>
    suspend fun updateStock(
        rawItemId: Int,
        delta: Double,
        reason: String,
        orderId: Int? = null
    )
    suspend fun getAllRawItemsBelowThreshold(): List<RawItem>
}

class RawItemRepositoryImpl: RawItemRepository {
    override suspend fun createRawItem(rawItem: RawItem) {
        transaction {
            RawItems.insert {
                it[name] = rawItem.name
                it[description] = rawItem.description
                it[unit] = rawItem.unit
                it[currentStock] = rawItem.currentStock
                it[alertThreshold] = rawItem.alertThreshold
                it[supplier] = rawItem.supplier
            }
        }
    }

    override suspend fun getRawItem(id: Int): RawItem? {
        return transaction {
            RawItems.select { RawItems.id eq id }.singleOrNull()?.toRawItem()
        }
    }

    override suspend fun getAllRawItems(): List<RawItem> {
        return transaction {
            RawItems.selectAll().map { it.toRawItem() }
        }
    }

    override suspend fun updateStock(rawItemId: Int, delta: Double, reason: String, orderId: Int?) {
        transaction {
            // Update stock
            RawItems.update({ RawItems.id eq rawItemId }) {
                with(SqlExpressionBuilder) {
                    it.update(currentStock, currentStock + delta)
                }
            }

            // Create transaction record
            InventoryTransactions.insert {
                it[this.rawItemId] = rawItemId
                it[amount] = delta
                it[transactionType] = if (delta > 0) TransactionType.PURCHASE else TransactionType.USAGE
                it[notes] = reason
                it[this.orderId] = orderId
            }
        }
    }

    override suspend fun getAllRawItemsBelowThreshold(): List<RawItem> {
        return transaction {
            RawItems.select { RawItems.currentStock less RawItems.alertThreshold }
                .map { it.toRawItem() }
        }
    }

    private fun ResultRow.toRawItem() = RawItem(
        id = this[RawItems.id].value,
        name = this[RawItems.name],
        description = this[RawItems.description],
        unit = this[RawItems.unit],
        currentStock = this[RawItems.currentStock],
        alertThreshold = this[RawItems.alertThreshold],
        supplier = this[RawItems.supplier]
    )

}