package com.example.medication.network;

import com.example.medication.model.ChatMessage;
import com.example.medication.model.request.ChatRoomRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApi {

    @POST("/api/chat/room")
    Call<ApiResponse<ChatRoomResponse>> enterChatRoom(@Body ChatRoomRequest request);

    @GET("/api/chat/room/{roomId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getPreviousMessages(@Path("roomId") Long roomId);
}