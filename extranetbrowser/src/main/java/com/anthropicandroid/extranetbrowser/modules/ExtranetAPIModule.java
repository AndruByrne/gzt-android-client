package com.anthropicandroid.extranetbrowser.modules;

/*
 * Created by Andrew Brin on 5/29/2016.
 */

import com.anthropicandroid.extranetbrowser.model.Occasion;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import rx.Observable;

@Module
public class ExtranetAPIModule {

    @Provides
    @ExtranetMapViewScope
    public ExtranetAPI getExtranetAPI(Retrofit retrofit) {
        return retrofit.create(ExtranetAPI.class);
    }

    public interface ExtranetAPI {
        @GET("extranet")
        public Observable<Occasion> getOccasionsFromLocation(
                double latitude,
                double longitude,
                String... keys);

        public Observable<Occasion> getOccasionsFromLocation(double latitude, double longitude);
    }

    @Provides
    @ExtranetMapViewScope
    public Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("http://gzt.com")
                .build();
    }
}
