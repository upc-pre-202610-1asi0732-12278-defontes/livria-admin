package com.example.adminlivria.common

import com.example.adminlivria.profilecontext.data.remote.AuthService
import android.content.Context
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.orderscontext.data.local.OrderDao
import com.example.adminlivria.orderscontext.data.remote.OrderService
import com.example.adminlivria.orderscontext.data.repository.OrderRepository
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.remote.UserAdminService
import com.example.adminlivria.stockcontext.data.remote.InventoryService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.adminlivria.BuildConfig
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.common.data.local.AdminDatabase
import com.example.adminlivria.statscontext.data.local.StatsDao
import com.example.adminlivria.statscontext.data.repository.StatsRepository

// Origen del API: BuildConfig.BASE_URL (Gradle). Mismo host que livria-user (secret API_BASE en CI).
// Local: en `local.properties` → API_BASE=http://10.0.2.2:5119  (emulador) o tu IP LAN; sin `/api/v1` ni barra final.
// O: ./gradlew -PAPI_BASE=http://127.0.0.1:5119 assembleDebug
val BASE_URL: String = BuildConfig.BASE_URL

private lateinit var tokenManager: TokenManager

fun initializeTokenManager(context: Context) {
    tokenManager = TokenManager(context)
}


private fun createOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()

        val token = tokenManager.getToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()
}



private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(createOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val authServiceInstance: AuthService by lazy {
    retrofit.create(AuthService::class.java)
}


val inventoryServiceInstance: InventoryService by lazy {
    retrofit.create(InventoryService::class.java)
}

val userAdminServiceInstance: UserAdminService by lazy {
    retrofit.create(UserAdminService::class.java)
}

val userClientServiceInstance: com.example.adminlivria.clientscontext.data.remote.UserClientService by lazy {
    retrofit.create(com.example.adminlivria.clientscontext.data.remote.UserClientService::class.java)
}

val bookServiceInstance: BookService by lazy {
    retrofit.create(BookService::class.java)
}

val orderServiceInstance: OrderService by lazy {
    retrofit.create(OrderService::class.java)
}

lateinit var adminDatabaseInstance: AdminDatabase

lateinit var orderDaoInstance: OrderDao

lateinit var bookDaoInstance: BookDao

lateinit var statsDaoInstance: StatsDao

fun initializeDaos(context: Context) {
    adminDatabaseInstance = AdminDatabase.getInstance(context)
    bookDaoInstance = adminDatabaseInstance.bookDao()
    statsDaoInstance = adminDatabaseInstance.statsDao()
}

val booksRepositoryInstance: BooksRepository by lazy {
    BooksRepository(
        dao = bookDaoInstance,
        service = bookServiceInstance
    )
}

val statsRepositoryInstance: StatsRepository by lazy {
    StatsRepository(
        dao = statsDaoInstance,
        bookInfoSource = booksRepositoryInstance
    )
}

val orderRepositoryInstance: OrderRepository by lazy {
    OrderRepository(
        service = orderServiceInstance,
        dao = orderDaoInstance
    )
}