package presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import data.model.Order
import data.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.DateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportViewModel : KoinComponent {
    private val orderRepo: OrderRepository by inject()
    var orders by mutableStateOf<List<Order>>(emptyList())
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    var exportMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadOrders()
    }

    fun filterOrders(dateRange: String) {
        coroutineScope.launch {
            orders = when (dateRange) {
                "Today" -> orderRepo.getDailyOrders()
                "This Week" -> orderRepo.getWeeklyOrders()
                "This Month" -> orderRepo.getMonthlyOrders()
                else -> orderRepo.getOrdersByDateRange(
                    start = DateTime.now().withTimeAtStartOfDay(),
                    end = DateTime.now()
                )
            }
        }
    }


    private fun loadOrders() {
        coroutineScope.launch {
            orders = orderRepo.getOrdersByDateRange(
                start = DateTime.now().minusMonths(1),
                end = DateTime.now()
            )
        }
    }

    fun getAllOrders(): List<Order> = orders


    fun exportToPdf(orders: List<Order>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val reportsDir = File(System.getProperty("user.home"), "YourApp/Reports").apply {
                    if (!exists()) mkdirs()
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val pdfFile = File(reportsDir, "report_$timestamp.pdf")

                PdfDocument(PdfWriter(pdfFile)).use { pdfDoc ->
                    val document = Document(pdfDoc)
                    document.add(Paragraph("Sales Report"))

                    // Add table content
                    orders.forEach { order ->
                        document.add(Paragraph("Order ID: ${order.id}"))
                        document.add(Paragraph("Customer: ${order.customerName}"))
                        document.add(Paragraph("Total: Rs.${"%.2f".format(order.totalAmount)}"))
                        document.add(Paragraph("\n"))
                    }

                    document.close()
                }

               coroutineScope.launch {
                    exportMessage = "PDF saved to:\n${pdfFile.absolutePath}"
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    exportMessage = "PDF export failed: ${e.localizedMessage}"
                }
            }
        }
    }

    fun exportToExcel(orders: List<Order>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val reportsDir = File(System.getProperty("user.home"), "YourApp/Reports").apply {
                    if (!exists()) mkdirs()
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val excelFile = File(reportsDir, "report_$timestamp.xls")

                HSSFWorkbook().use { workbook ->
                    val sheet = workbook.createSheet("Orders Report")

                    // Create header row
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Order ID")
                    headerRow.createCell(1).setCellValue("Customer Name")
                    headerRow.createCell(2).setCellValue("Total Amount")
                    headerRow.createCell(3).setCellValue("Date")

                    // Populate data
                    orders.forEachIndexed { index, order ->
                        val row = sheet.createRow(index + 1)
                        row.createCell(0).setCellValue(order.id.toString())
                        row.createCell(1).setCellValue(order.customerName)
                        row.createCell(2).setCellValue(order.totalAmount)
                        row.createCell(3).setCellValue(
                            SimpleDateFormat("yyyy-MM-dd HH:mm").format(order.createdAt)
                        )
                    }

                    // Auto-size columns
                    for (i in 0..3) {
                        sheet.autoSizeColumn(i)
                    }

                    FileOutputStream(excelFile).use { fos ->
                        workbook.write(fos)
                    }
                }

                coroutineScope.launch {
                    exportMessage = "Excel saved to:\n${excelFile.absolutePath}"
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    exportMessage = "Excel export failed: ${e.localizedMessage}"
                }
            }
        }
    }

    fun clearMessage() {
        exportMessage = null
    }
}