package com.example.medication.util;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuthEventBus {

    public interface Listener {
        void onForceLogout();
    }

    private static final AuthEventBus INSTANCE = new AuthEventBus();

    public static AuthEventBus get() {
        return INSTANCE;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private AuthEventBus() {}

    public void subscribe(Listener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    public void publish() {
        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onForceLogout();
            }
        });
    }
}