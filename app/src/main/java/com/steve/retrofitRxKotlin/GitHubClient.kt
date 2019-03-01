package com.steve.retrofitRxKotlin


import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET

interface GitHubClient {
    @get:GET("users")
    val gtUsers: Single<List<GitHubEntry>> // for RxAndroid (Single)

    @get:GET("users")
    val gitHubUsers: Call<List<GitHubEntry>> // for Retrofit (Call)
}
