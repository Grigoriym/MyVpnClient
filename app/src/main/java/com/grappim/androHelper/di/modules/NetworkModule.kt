package com.grappim.androHelper.di.modules

import android.content.Context
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.grappim.androHelper.BuildConfig
import com.grappim.androHelper.api.IpifyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class IpifyApiQualifier

@Qualifier
annotation class IpifyRetrofit

@Qualifier
annotation class HttpLoggingInterceptorQualifier

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWifiManager(
        @ApplicationContext context: Context
    ): WifiManager = ContextCompat.getSystemService(context, WifiManager::class.java)!!

    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager =
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)!!

    @Provides
    @Singleton
    fun provideTelephonyManager(
        @ApplicationContext context: Context
    ): TelephonyManager =
        ContextCompat.getSystemService(context, TelephonyManager::class.java)!!

    @Provides
    @Singleton
    fun provideDhcpInfo(
        wifiManager: WifiManager
    ): DhcpInfo = wifiManager.dhcpInfo

    @Provides
    @Singleton
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())

    @Provides
    @Singleton
    @IpifyApiQualifier
    fun providerFixerApi(
        @IpifyRetrofit retrofit: Retrofit
    ): IpifyApi = retrofit.create(IpifyApi::class.java)

    @Provides
    @Singleton
    @IpifyRetrofit
    fun providerFixerRetrofit(
        builder: Retrofit.Builder,
        client: OkHttpClient
    ): Retrofit =
        builder.baseUrl(BuildConfig.IpifyBaseUrl)
            .client(client)
            .build()

    @Provides
    @Singleton
    fun providerOkHttpClient(
        @HttpLoggingInterceptorQualifier loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()

    @Provides
    @Singleton
    @HttpLoggingInterceptorQualifier
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message -> Timber.tag("API").d(message) }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

}