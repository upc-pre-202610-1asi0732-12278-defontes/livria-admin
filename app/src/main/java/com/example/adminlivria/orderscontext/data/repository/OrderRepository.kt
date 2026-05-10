package com.example.adminlivria.orderscontext.data.repository

import OrderDto
import com.example.adminlivria.common.Resource
import com.example.adminlivria.orderscontext.data.local.OrderDao
import com.example.adminlivria.orderscontext.data.remote.OrderService
import com.example.adminlivria.orderscontext.domain.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import toDomain

class OrderRepository(
    private val service: OrderService,
    private val dao: OrderDao
) {


    suspend fun getAllOrders(): Resource<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getAllOrders()
            if (response.isSuccessful) {
                response.body()?.let { listDto ->
                    val orders = listDto.map { it.toDomain() }
                    return@withContext Resource.Success(data = orders)
                }
            }
            return@withContext Resource.Error("Parece que no hay conexión a internet. Revisa tu red.")
        } catch (e: Exception) {
            return@withContext Resource.Error("No pudimos conectarnos al servidor. Inténtalo más tarde.")
        }
    }

    suspend fun getOrderById(orderId: Int): Resource<Order> = withContext(Dispatchers.IO) {
        try {
            val response = service.getOrderById(orderId)
            if (response.isSuccessful) {
                response.body()?.let { orderDto ->
                    val order = orderDto.toDomain()
                    return@withContext Resource.Success(data = order)
                }
            }
            return@withContext Resource.Error("No se pudo encontrar esta orden. Es posible que haya sido eliminada.")
        } catch (e: Exception) {
            return@withContext Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
        }
    }


    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val statusUpdate = mapOf("status" to newStatus)
            val response = service.updateOrderStatus(orderId, statusUpdate)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Oops, no pudimos actualizar el estado de la orden. Inténtalo de nuevo.")
            }
        } catch (e: Exception) {
            Resource.Error("Parece que no tienes conexión a internet. Verifica tu red.")
        }
    }


    suspend fun searchOrders(input: String): Resource<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getAllOrders()

            if (response.isSuccessful) {
                response.body()?.let { ordersDto: List<OrderDto> ->
                    val filteredOrders = ordersDto
                        .filter { it.userFullName.contains(input, ignoreCase = true) || it.code.contains(input, ignoreCase = true) }
                        .map { it.toDomain() }

                    if (filteredOrders.isNotEmpty()) {
                        return@withContext Resource.Success(data = filteredOrders)
                    }

                    return@withContext Resource.Error(
                        message = "No se encontraron órdenes para '$input'"
                    )
                }
                return@withContext Resource.Error(message = "Tuvimos un problema al cargar los datos de las órdenes.")
            }
            return@withContext Resource.Error(message = "No pudimos completar tu búsqueda. Inténtalo más tarde.")

        } catch (e: Exception) {
            return@withContext Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
        }
    }


}