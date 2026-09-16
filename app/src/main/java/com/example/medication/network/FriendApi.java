package com.example.medication.network;

import com.example.medication.model.request.FriendRequestAnswerDto;
import com.example.medication.model.request.FriendRequestCreateDto;
import com.example.medication.model.response.ApiResponse;
import com.example.medication.model.response.FriendListDto;
import com.example.medication.model.response.FriendQrCodeDto;
import com.example.medication.model.response.ReceivedFriendRequestDto;
import com.example.medication.model.response.UserProfileDto;
import com.example.medication.model.response.UserSearchResultDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FriendApi {

    /**
     *  친구 요청 API
     */
    // 닉네임으로 사용자 검색
    @GET("/api/friend/search")
    Call<ApiResponse<UserSearchResultDto>> searchUser(@Query("nickname") String nickname);

    // 친구 요청 보내기
    @POST("/api/friend/request")
    Call<ApiResponse<Void>> createFriendRequest(@Body FriendRequestCreateDto request);

    // 내가 받은 친구 요청 목록
    @GET("/api/friend/request/received")
    Call<ApiResponse<List<ReceivedFriendRequestDto>>> getReceivedFriendRequests();

    // 친구 요청 수락/거절
    @PATCH("/api/friend/request/{requestId}/answer")
    Call<ApiResponse<Void>> answerFriendRequest(
            @Path("requestId") Long requestId,
            @Body FriendRequestAnswerDto answer);


    // 내 QR 코드 발급/재발급 ("내 QR 보여주기" 화면을 열 때마다 호출, 5분 후 만료)
    @POST("/api/friend/qr-code")
    Call<ApiResponse<FriendQrCodeDto>> issueQrCode();

    // QR 코드로 알아낸 사용자 프로필 + 나와의 관계 상태 조회
    @GET("/api/friend/profile/{code}")
    Call<ApiResponse<UserProfileDto>> getUserProfile(@Path("code") String code);

    /**
     *  친구 관계 API
     */

    //  친구 목록 가져오기
    @GET("/api/friend/list")
    Call<ApiResponse<FriendListDto>> getFriendList();

    //  친구 삭제하기 (삭제 후 갱신된 친구 목록을 받는다)
    @DELETE("/api/friend")
    Call<ApiResponse<FriendListDto>> deleteFriend(@Query("friendId") Long friendId);
}
