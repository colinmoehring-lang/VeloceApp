package de.veloce.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.veloce.app.BuildConfig
import de.veloce.app.data.remote.VeloceApi
import de.veloce.app.data.repository.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Singleton
class SessionInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val session = runBlocking { sessionStore.observeSession().first() }
        val authenticatedRequest = if (session == null) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
        }
        val response = chain.proceed(authenticatedRequest)
        if (response.code == 401 && session != null) {
            runBlocking { sessionStore.clear() }
        }
        return response
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionInterceptor: SessionInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(sessionInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideVeloceApi(retrofit: Retrofit): VeloceApi = retrofit.create(VeloceApi::class.java)
}
