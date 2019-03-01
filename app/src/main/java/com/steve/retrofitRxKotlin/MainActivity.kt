package com.steve.retrofitRxKotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    internal val baseUrl = "https://api.github.com/"
    internal lateinit var listView: ListView
    private val disposable = CompositeDisposable()
    internal lateinit var retrofit: Retrofit
    internal lateinit var gitHubClient: GitHubClient
    internal lateinit var adapter: EntryListAdapter
    //internal lateinit var okHttpClient: OkHttpClient
    private var gitHubEntries = ArrayList<GitHubEntry>()

    private val entryObservable: Observable<List<GitHubEntry>>
        get() = gitHubClient
                .gtUsers
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<ListView>(R.id.page_list)

        adapter = EntryListAdapter(this, gitHubEntries)
        listView.adapter = adapter
        gitHubClient = getRetrofit().create(GitHubClient::class.java)
        val entriesObservable = entryObservable.replay()

        disposable.add(entriesObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<GitHubEntry>>() {
                    override fun onNext(entries: List<GitHubEntry>) {
                        gitHubEntries.clear()
                        printEntries(entries)
                        gitHubEntries.addAll(entries)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                }))

        entriesObservable.connect()
    }

    private fun printEntries(entries: List<GitHubEntry>) {
        for (entry in entries) {
            Log.e(TAG, "printEntries: id=" + entry.id + ", login=" + entry.login)
        }
    }

    fun getRetrofit(): Retrofit {
            retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(getOkHttpClient())
                    .build()
        return retrofit
    }

    private fun getOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        httpClient.addInterceptor(interceptor)

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Request-Type", "Android")
                    .addHeader("Content-Type", "application/json")

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        return httpClient.build()
    }

    companion object {
        private val TAG = "MainActivity"
        private val REQUEST_TIMEOUT = 60
    }
}
