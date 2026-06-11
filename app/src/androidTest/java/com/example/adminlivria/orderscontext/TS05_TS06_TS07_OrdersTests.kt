// TS05 / TS06 / TS07 – Core Integration Tests
// Valida el contexto de órdenes del administrador sobre OrderRepository:
// - TS05: estadísticas y análisis de órdenes (totales, estados, promedio)
// - TS06: búsqueda de órdenes por código o cliente y estado vacío
// - TS07: presentación estructurada de los atributos esenciales de la orden
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.orderscontext

import OrderDto
import ItemDto
import ShippingDto
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.common.Resource
import com.example.adminlivria.orderscontext.data.local.OrderDao
import com.example.adminlivria.orderscontext.data.local.OrderEntity
import com.example.adminlivria.orderscontext.data.remote.OrderService
import com.example.adminlivria.orderscontext.data.repository.OrderRepository
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

// --------------------------------------------------------------------
// Fakes
// --------------------------------------------------------------------
private fun buildOrderDto(
    id: Int = 1,
    code: String = "ORD-001",
    client: String = "Ana Torres",
    status: String = "pending",
    total: Double = 100.0
) = OrderDto(
    id            = id,
    code          = code,
    userClientId  = id,
    userEmail     = "cliente@livria.com",
    userPhone     = "999888777",
    userFullName  = client,
    recipientName = client,
    status        = status,
    isDelivery    = true,
    shipping      = ShippingDto("Av. Lima 123", "Lima", "Miraflores", "Frente al parque"),
    total         = total,
    date          = "2026-06-01T10:30:00.000000",
    items         = listOf(
        ItemDto(1, 10, "Clean Code", "Robert C. Martin", 50.0, "cover.jpg", 2, total)
    )
)

private class FakeOrderService(
    var orders: List<OrderDto> = emptyList(),
    var shouldFail: Boolean = false
) : OrderService {

    override suspend fun getAllOrders(): Response<List<OrderDto>> =
        if (shouldFail) Response.error(500, "Server error".toResponseBody())
        else Response.success(orders)

    override suspend fun getOrderById(id: Int): Response<OrderDto> =
        orders.find { it.id == id }
            ?.let { Response.success(it) }
            ?: Response.error(404, "Not found".toResponseBody())

    override suspend fun getOrderByCode(code: String): Response<OrderDto> =
        orders.find { it.code == code }
            ?.let { Response.success(it) }
            ?: Response.error(404, "Not found".toResponseBody())

    override suspend fun getOrdersByClient(userClientId: Int): Response<List<OrderDto>> =
        Response.success(orders.filter { it.userClientId == userClientId })

    override suspend fun updateOrderStatus(
        orderId: Int,
        statusUpdate: Map<String, String>
    ): Response<Unit> = Response.success(Unit)
}

private class FakeOrderDao : OrderDao {
    override suspend fun update(orderEntity: OrderEntity) {}
    override suspend fun fetchAll(): List<OrderEntity> = emptyList()
    override suspend fun fetchById(id: Int): OrderEntity? = null
}

@RunWith(AndroidJUnit4::class)
class TS05_TS06_TS07_OrdersTests {

    private lateinit var service: FakeOrderService
    private lateinit var repository: OrderRepository

    @Before
    fun setUp() {
        service = FakeOrderService(
            orders = listOf(
                buildOrderDto(1, "ORD-001", "Ana Torres",   "pending",   100.0),
                buildOrderDto(2, "ORD-002", "Luis Alva",    "delivered", 200.0),
                buildOrderDto(3, "ORD-003", "Ana Torres",   "delivered", 300.0),
                buildOrderDto(4, "ORD-004", "Carla Méndez", "pending",    60.0)
            )
        )
        repository = OrderRepository(service, FakeOrderDao())
    }

    // ----------------------------------------------------------------
    // TS05 – Estadísticas y análisis de órdenes
    // (mismas fórmulas que presenta la vista de gestión de órdenes)
    // ----------------------------------------------------------------

    @Test
    fun ts05_ac1_orderStats_shouldComputeTotalsAndRevenue() = runBlocking {
        // Act
        val result = repository.getAllOrders()
        val orders = (result as Resource.Success).data!!

        // Assert — total de órdenes y ganancias generadas
        val totalRevenue = orders.sumOf { it.total }
        assertEquals(4, orders.size)
        assertEquals(660.0, totalRevenue, 0.001)
    }

    @Test
    fun ts05_ac2_orderStats_shouldCountPendingAndCompleted() = runBlocking {
        // Act
        val orders = (repository.getAllOrders() as Resource.Success).data!!

        // Assert — resumen del flujo de trabajo por estado
        val pending   = orders.count { it.status.equals("pending",   ignoreCase = true) }
        val completed = orders.count { it.status.equals("delivered", ignoreCase = true) }
        assertEquals(2, pending)
        assertEquals(2, completed)
    }

    @Test
    fun ts05_ac3_orderStats_shouldComputeAverageOrderValue() = runBlocking {
        // Act
        val orders = (repository.getAllOrders() as Resource.Success).data!!

        // Assert — valor promedio de las órdenes
        val average = orders.sumOf { it.total } / orders.size
        assertEquals(165.0, average, 0.001)
    }

    // ----------------------------------------------------------------
    // TS06 – Búsqueda de órdenes por identificador o cliente
    // ----------------------------------------------------------------

    @Test
    fun ts06_ac1_searchOrders_byCode_shouldReturnMatchingOrder() = runBlocking {
        // Act
        val result = repository.searchOrders("ORD-002")

        // Assert
        assertTrue(result is Resource.Success)
        val orders = (result as Resource.Success).data!!
        assertEquals(1, orders.size)
        assertEquals("Luis Alva", orders.first().userFullName)
    }

    @Test
    fun ts06_ac1_searchOrders_byClientName_shouldReturnAllClientOrders() = runBlocking {
        // Act
        val result = repository.searchOrders("Ana Torres")

        // Assert — todas las órdenes asociadas al cliente
        val orders = (result as Resource.Success).data!!
        assertEquals(2, orders.size)
        assertTrue(orders.all { it.userFullName == "Ana Torres" })
    }

    @Test
    fun ts06_ac2_searchOrders_whenNoMatch_shouldReturnEmptyStateMessage() = runBlocking {
        // Act — búsqueda sin coincidencias
        val result = repository.searchOrders("inexistente")

        // Assert — mensaje claro de que no se encontraron órdenes
        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("No se encontraron órdenes"))
    }

    // ----------------------------------------------------------------
    // TS07 – Tabla con los atributos esenciales de cada orden
    // ----------------------------------------------------------------

    @Test
    fun ts07_ac1_orderList_shouldExposeEssentialAttributes() = runBlocking {
        // Act
        val orders = (repository.getAllOrders() as Resource.Success).data!!
        val first = orders.first()

        // Assert — ID, código, fecha, cliente, total y estado presentes
        assertEquals(1,           first.id)
        assertEquals("ORD-001",   first.code)
        assertEquals("Ana Torres", first.userFullName)
        assertEquals("pending",   first.status)
        assertEquals(100.0,       first.total, 0.001)
        assertNotNull(first.date)
        assertTrue(first.items.isNotEmpty())
    }

    @Test
    fun ts07_ac1_orderMapping_whenShippingIsNull_shouldUseEmptyShipping() = runBlocking {
        // Arrange — orden sin datos de envío (recojo en tienda)
        service.orders = listOf(
            buildOrderDto(9, "ORD-009", "Ana Torres", "pending", 50.0)
                .copy(isDelivery = false, shipping = null)
        )

        // Act
        val orders = (repository.getAllOrders() as Resource.Success).data!!

        // Assert — el mapeo es seguro ante shipping nulo
        assertEquals("", orders.first().shipping.address)
        assertFalse(orders.first().isDelivery)
    }
}
