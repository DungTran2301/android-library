package com.nartgnud.core.data

import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ConverterBuilder internal constructor() {

    var factory: Converter.Factory? = null

    fun build(): Converter.Factory = factory ?: GsonConverterFactory.create(
        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    )
}

class LoggingBuilder internal constructor() {

    var level: HttpLoggingInterceptor.Level = NONE

    fun build(): Interceptor = HttpLoggingInterceptor().apply {
        level = this@LoggingBuilder.level
    }
}

class ClientBuilder internal constructor() {


    var interceptors: List<Interceptor> = listOf()

    var readTimeoutInMillis: Long = 10_000

    var writeTimeoutInMillis: Long = 10_000


    var authenticator: Authenticator? = null

    private val loggingBuilder = LoggingBuilder()

    fun logging(block: LoggingBuilder.() -> Unit) {
        loggingBuilder.apply(block)
    }

    fun build(): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingBuilder.build())
        .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeoutInMillis, TimeUnit.MILLISECONDS)
        .apply { interceptors.forEach { addInterceptor(it) } }
        .apply { authenticator?.let { authenticator(it) } }
        .build()
}

class ApiBuilder<T> internal constructor(private val api: Class<T>) {
    private val converterBuilder = ConverterBuilder()
    private val clientBuilder = ClientBuilder()

    var baseUrl: String = ""

    fun converter(block: ConverterBuilder.() -> Unit) {
        converterBuilder.apply(block)
    }

    fun client(block: ClientBuilder.() -> Unit) {
        clientBuilder.apply(block)
    }

    fun build(): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(clientBuilder.build())
        .addConverterFactory(converterBuilder.build())
        .build()
        .create(api)

}

private fun <T> apiBuilder(
    api: Class<T>,
    block: ApiBuilder<T>.() -> Unit
): T = ApiBuilder(api).apply(block).build()

fun <API> buildServiceApi(
    serviceBaseUrl: String,
    serviceApiClass: Class<API>,
    clientBuilder: (ClientBuilder.() -> Unit)? = null,
    converterBuilder: (ConverterBuilder.() -> Unit)? = null
): API {
    return apiBuilder(serviceApiClass) {
        baseUrl = serviceBaseUrl
        client(clientBuilder ?: {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            interceptors = listOf(httpLoggingInterceptor)
            logging {
                level = HttpLoggingInterceptor.Level.BODY
            }
        })
        converter(converterBuilder ?: {
            factory = GsonConverterFactory.create()
        })
    }
}