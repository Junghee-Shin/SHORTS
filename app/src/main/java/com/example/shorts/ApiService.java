package com.example.shorts;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("AddPicture.php")
    Call<AddPictureRes> addPicture(@Part MultipartBody.Part image);

    @FormUrlEncoded
    @POST("DelPicture.php")
    Call<AddPictureRes> delPicture(@Field("image") String image);

}
