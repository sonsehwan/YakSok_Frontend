package com.example.medication.network;

import com.example.medication.model.request.ChatRoomRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("/api/chat/room")
    Call<ApiResponse<ChatRoomResponse>> enterChatRoom(@Body ChatRoomRequest request);
}