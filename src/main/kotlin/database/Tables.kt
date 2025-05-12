package database

import data.model.DiscountType
import data.model.OrderStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime


object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

object MenuItems: IntIdTable("menu_items") {
    val categoryId = integer("category_id").references(Categories.id)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val isActive = bool("is_active").default(true)
    val discountType = enumerationByName("discount_type", 20, DiscountType::class).nullable()
    val discountValue = double("discount_value").nullable()
}

object ItemVariants: IntIdTable("item_variants") {
    val itemId = integer("item_id").references(MenuItems.id)
    val size = varchar("size", 20)
    val price = double("price")
    val memberPrice = double("member_price").nullable()
}

// database/Orders.kt
object Orders : IntIdTable("orders") {
    val customerName = varchar("customer_name", 100).default("N/A")
    val phone = varchar("phone", 20).default("N/A")
    val email = varchar("email", 50).default("N/A")
    val totalAmount = double("total_amount").default(0.0)
    val memberId = integer("member_id").nullable()  // If member exists
    val isMember = bool("is_member").default(false)
    val createdAt = datetime("created_at").default(DateTime.now())
    val status = enumerationByName("status", 20, OrderStatus::class).default(OrderStatus.ACTIVE)
}

object OrderItems : IntIdTable("order_items") {
    val orderId = integer("order_id").references(Orders.id)
    val itemId = integer("item_id").references(MenuItems.id)
    val variantSize = varchar("variant_size", 20)
    val quantity = integer("quantity")
    val price = double("price")  // Price at time of ordering
    val memberPriceApplied = bool("member_price_applied").default(false)
    val discountApplied = double("discount_applied").default(0.0)
}

object Members : IntIdTable("members") {
    val phone = varchar("phone", 20).uniqueIndex()
    val name = varchar("name", 50)
    val joinDate = datetime("join_date")
}