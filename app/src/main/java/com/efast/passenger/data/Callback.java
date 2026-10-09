package com.efast.passenger.data;

/** Result of an async call. The fake repository uses it today; the real API client will use it later. */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(String message);
}
