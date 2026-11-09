package com.example.medication.util;

import android.view.View;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusyCallback<T> implements Callback<T> {
    private final Callback<T> callback;
    private final View[] buttons;

    public BusyCallback(Callback<T> delegate, View... buttons) {
        this.callback = delegate;
        this.buttons = buttons;
        setEnabled(false);
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        setEnabled(true);
        callback.onResponse(call, response);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        setEnabled(true);
        callback.onFailure(call, t);
    }

    private void setEnabled(boolean enabled) {
        for (View b : buttons) b.setEnabled(enabled);
    }
}
