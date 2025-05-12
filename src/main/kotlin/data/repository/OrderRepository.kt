package data.repository

import data.model.Order
import data.model.OrderItem
import data.model.OrderStatus
import database.Members
import database.MenuItems
import database.OrderItems
import database.Orders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

// data/repository/OrderRepository.kt
interface OrderRepository {
    // Order Operations
    suspend fun createOrder(order: Order): Int
    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus)
    suspend fun getOrdersByDateRange(start: DateTime, end: DateTime): List<Order>

    // Order Item Operations
    suspend fun addItemToOrder(orderId: Int, item: OrderItem)
    suspend fun removeItemFromOrder(orderItemId: Int)

    // Reports
    suspend fun getDailyOrders(): List<Order>
    suspend fun getWeeklyOrders(): List<Order>
    suspend fun getMonthlyOrders(): List<Order>
    suspend fun searchSoldItems(itemName: String): List<OrderItem>

    suspend fun isPhoneNumberRegistered(phone: String): Boolean

}

class OrderRepositoryImpl: OrderRepository {
    override suspend fun createOrder(order: Order): Int = transaction{
        // Insert order
        val orderId = Orders.insert {
            it[customerName] = order.customerName
            it[phone] = order.phone
            it[email] = order.email
            it[totalAmount] = order.totalAmount
            it[memberId] = order.memberId
            it[isMember] = order.isMember
            it[status] = order.status
        } get Orders.id

        // Insert order items
        order.items.forEach { item ->
            OrderItems.insert {
                it[this.orderId] = orderId.value
                it[itemId] = item.itemId
                it[variantSize] = item.variantSize
                it[quantity] = item.quantity
                it[price] = item.price
                it[memberPriceApplied] = item.memberPriceApplied
                it[discountApplied] = item.discountApplied
            }
        }
        orderId.value
    }


    override suspend fun getOrdersByDateRange(start: DateTime, end: DateTime): List<Order> =
        transaction {
            Orders.select { Orders.createdAt.between(start, end) }
                .map { rowToOrder(it) }
        }

    override suspend fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        transaction {
            Orders.update({ Orders.id eq orderId }) {
                it[Orders.status] = status
            }
        }
    }

    override suspend fun addItemToOrder(orderId: Int, item: OrderItem) {
        transaction {
            OrderItems.insert {
                it[this.orderId] = orderId
                it[this.itemId] = item.itemId
                it[variantSize] = item.variantSize
                it[quantity] = item.quantity
                it[price] = item.price
                it[memberPriceApplied] = item.memberPriceApplied
                it[discountApplied] = item.discountApplied
            }
        }
    }

    override suspend fun removeItemFromOrder(orderItemId: Int) {
        transaction {
            OrderItems.deleteWhere { OrderItems.id eq orderItemId }
        }
    }

    override suspend fun getDailyOrders(): List<Order> {
        val todayStart = DateTime.now().withTimeAtStartOfDay()
        val todayEnd = todayStart.plusDays(1)
        return getOrdersByDateRange(todayStart, todayEnd)
    }

    override suspend fun getWeeklyOrders(): List<Order> {
        val end = DateTime.now()
        val start = end.minusDays(7)

        return getOrdersByDateRange(start, end)
    }

    override suspend fun getMonthlyOrders(): List<Order> {
        val end = DateTime.now()
        val start = end.minusDays(30)
        return getOrdersByDateRange(start, end)
    }

    override suspend fun searchSoldItems(itemName: String): List<OrderItem> {
        return transaction {
            (OrderItems innerJoin MenuItems)
                .select { MenuItems.name.lowerCase() like "%${itemName.lowercase()}%" }
                .map { rowToOrderItem(it) }
        }
    }

    override suspend fun isPhoneNumberRegistered(phone: String): Boolean {
        return transaction {
            Members.select { Members.phone eq phone }.count() > 0
        }
    }

    // Existing helper methods
    private fun rowToOrderItem(row: ResultRow): OrderItem {
        return OrderItem(
            id = row[OrderItems.id].value,
            itemId = row[OrderItems.itemId],
            variantSize = row[OrderItems.variantSize],
            quantity = row[OrderItems.quantity],
            price = row[OrderItems.price],
            memberPriceApplied = row[OrderItems.memberPriceApplied],
            discountApplied = row[OrderItems.discountApplied]
        )
    }

    private fun rowToOrder(row: ResultRow): Order {
        val items = OrderItems
            .select { OrderItems.orderId eq row[Orders.id].value }
            .map { rowToOrderItem(it) }

        return Order(
            id = row[Orders.id].value,
            customerName = row[Orders.customerName],
            phone = row[Orders.phone],
            email = row[Orders.email],
            items = items,
            totalAmount = row[Orders.totalAmount],
            memberId = row[Orders.memberId],
            isMember = row[Orders.isMember],
            status = row[Orders.status],
            createdAt = row[Orders.createdAt]
        )
    }

}