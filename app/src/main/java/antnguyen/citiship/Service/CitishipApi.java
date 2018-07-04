package antnguyen.citiship.Service;

import antnguyen.citiship.Model.LoginModel;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CitishipApi {

    @FormUrlEncoded
    @POST("api/app/login")
    Call<LoginModel> sendLogin(@Field("username") String username,
                               @Field("password") String password);
}
