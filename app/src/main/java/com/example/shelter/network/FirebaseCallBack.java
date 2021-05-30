package com.example.shelter.network;

import java.util.List;

public interface FirebaseCallBack<T> {
    void onCallBack(List<T> items);
}
