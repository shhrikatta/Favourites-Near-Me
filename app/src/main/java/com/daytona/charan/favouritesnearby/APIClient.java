package com.daytona.charan.favouritesnearby;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by anupamchugh on 09/09/16.
 */
public class APIClient {

    private static Retrofit retrofit = null;

//    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyBRRWY-ummj8Vtm4EmTXF-Avx1UbIFK76k";
//    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyB8LU-lqZyBLOg9RnRuRsubZMryHAlStlc";
//    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyDeJ0DZ9uoxnZE9cPN5PKwjpdzGVP5Aww0";
    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyDn29L87mlc-21CJBsXAuxI2l833ECMMtY";
//    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyDORkeixpBVJwvO8f7yQYIdvvj-OZCK3u8";

    public static String base_url = "https://maps.googleapis.com/maps/api/";

    public static Retrofit getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).addInterceptor(interceptor).build();


        retrofit = null;

        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        return retrofit;
    }

}
