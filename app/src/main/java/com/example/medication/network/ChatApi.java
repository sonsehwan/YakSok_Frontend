package com.example.medication.network;

import com.example.medication.model.ChatMessage;
import com.example.medication.model.request.ChatRoomRequest;
import com.example.medication.model.request.FriendChatRoomRequest;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.ChatRoomListDto;
import com.example.medication.model.response.ChatRoomResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    @POST("/api/chat/room")
    Call<ApiResponse<ChatRoomResponse>> enterChatRoom(@Body ChatRoomRequest request);

    @POST("/api/chat/room/friend")
    Call<ApiResponse<ChatRoomResponse>> enterFriendChatRoom(@Body FriendChatRoomRequest request);

    @GET("/api/chat/room/{roomId}/messages")
    Call<ApiResponse<List<ChatMessage>>> getPreviousMessages(@Path("roomId") Long roomId, @Query("userId") Long userId);

    // 로그인한 유저가 참여 중인 채팅방 목록
    @GET("/api/chat/room")
    Call<ApiResponse<List<ChatRoomListDto>>> getMyChatRooms(@Query("userId") Long userId);

    // 채팅방을 목록에서 삭제(숨김)
    @DELETE("/api/chat/room")
    Call<ApiResponse<List<ChatRoomListDto>>> deleteChatRoom(@Query("userId") Long userId, @Query("roomId") Long roomId);

}