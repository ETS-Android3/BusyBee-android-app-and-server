package com.zubov.android.bzb.utils;

import com.zubov.android.bzb.model.DTO.DescriptionMarkerDTO;
import com.zubov.android.bzb.model.DTO.MarkerDTO;
import com.zubov.android.bzb.model.DTO.OrderDTO;
import com.zubov.android.bzb.model.DTO.OrderResponseDTO;
import com.zubov.android.bzb.model.DTO.OrderToUpdateDTO;
import com.zubov.android.bzb.model.DTO.UserCheckDTO;
import com.zubov.android.bzb.model.DTO.UserIdDTO;
import com.zubov.android.bzb.model.DTO.UserOrderDTO;
import com.zubov.android.bzb.model.DTO.UserResponseInfoDTO;
import com.zubov.android.bzb.model.DTO.UserUpdatePasswordDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface JSONPlaceHolderApi {
    @GET("orders")
    public Call<List<MarkerDTO>> getAllOrders();

    @POST("orders/description")
    public  Call<DescriptionMarkerDTO> getDescription(@Body MarkerDTO marker);

    @POST("users/login")
    public Call<UserResponseInfoDTO> checkUser(@Body UserCheckDTO userToCheck);

    @POST("users/registration")
    public Call<UserResponseInfoDTO> registerUser(@Body UserCheckDTO userToRegister);

    @POST("users/token")
    public Call<UserResponseInfoDTO> checkToken(@Body UserResponseInfoDTO userToCheck);

    @POST("orders/new")
    public Call<OrderResponseDTO> addOrder(@Body OrderDTO orderToAdd);

    @POST("orders/orders_list")
    public Call<List<UserOrderDTO>> getOrdersList(@Body UserIdDTO ordersMaster);

    @POST("orders/update")
    public Call<Boolean> updateOrders(@Body List<OrderToUpdateDTO> ordersToUpdate);

    @POST("orders/delete")
    Call<Boolean> deleteOrder(@Body OrderResponseDTO orderToDelete);

    @POST("users/update")
    Call<UserResponseInfoDTO> updatePassword(@Body UserUpdatePasswordDTO passwordToUpdate);

    @POST("users/delete")
    Call<Boolean> deleteUser(@Body UserCheckDTO userToDelete);
}

