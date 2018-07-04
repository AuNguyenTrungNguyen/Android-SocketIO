package antnguyen.citiship.Util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConfig {
    private static Retrofit mRetrofit;
    private static final String BASE_URL = "https://hoclamweb.club/";

    public static Retrofit getRetrofitInstance() {
        if (mRetrofit == null) {
            mRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofit;
    }
}