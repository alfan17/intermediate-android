package com.alfan.story

import android.content.SharedPreferences
import com.alfan.story.data.repository.StoryRepository
import com.alfan.story.data.repository.UserRepository
import com.alfan.story.data.response.LoginResponse
import com.alfan.story.data.services.StoryServices
import com.alfan.story.data.services.UserServices
import com.alfan.story.library.PreferencesExt
import com.alfan.story.library.PreferencesExt.getObject
import com.alfan.story.viewmodels.StoryViewModel
import com.alfan.story.viewmodels.UserViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val libraryModule = module {
    single { PreferencesExt.defaultPrefs(androidContext()) }
}

val serviceModule = module {
    factory { get<Retrofit>().create(UserServices::class.java) }
    factory { get<Retrofit>().create(StoryServices::class.java) }
}

val featureModule = module {
    single { StoryRepository(get()) }
    viewModel { StoryViewModel(get()) }

    single { UserRepository(get()) }
    viewModel { UserViewModel(get()) }
}

val remoteModule = module {

    single {
        HttpLoggingInterceptor()
            .apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                val tokenUser = get<SharedPreferences>().getObject<LoginResponse.Result>("login")

                if (tokenUser != null) {
                    request.addHeader("Authorization", "Bearer ${tokenUser.token}")
                }

                chain.proceed(request.build())
            }.build()
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}