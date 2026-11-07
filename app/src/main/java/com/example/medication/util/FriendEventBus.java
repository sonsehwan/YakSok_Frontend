package com.example.medication.util;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FriendEventBus {

    public interface Listener {
        void onFriendChanged();
    }

    private static final FriendEventBus INSTANCE = new FriendEventBus();

    public static FriendEventBus get() {
        return INSTANCE;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private FriendEventBus() {}

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
                listener.onFriendChanged();
            }
        });
    }
}
