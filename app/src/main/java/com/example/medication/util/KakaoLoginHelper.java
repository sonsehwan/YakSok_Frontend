package com.example.medication.util;

import android.content.Context;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoLoginHelper {

    public interface Listener {
        void onToken(String kakaoAccessToken);
        void onError(Throwable error);
        void onCancel();
    }

    public static void login(Context context, Listener listener) {
        Function2<OAuthToken, Throwable, Unit> callback = (token, error) -> {
            if (error != null) listener.onError(error);
            else if (token != null) listener.onToken(token.getAccessToken());
            return Unit.INSTANCE;
        };

        UserApiClient client = UserApiClient.getInstance();

        if (client.isKakaoTalkLoginAvailable(context)) {
            client.loginWithKakaoTalk(context, (token, error) -> {
                if (error instanceof ClientError
                        && ((ClientError) error).getReason() == ClientErrorCause.Cancelled) {
                    listener.onCancel();
                    return Unit.INSTANCE;
                }
                if (error != null) {
                    client.loginWithKakaoAccount(context, callback);
                    return Unit.INSTANCE;
                }
                return callback.invoke(token, null);
            });
        } else {
            client.loginWithKakaoAccount(context, callback);
        }
    }
}