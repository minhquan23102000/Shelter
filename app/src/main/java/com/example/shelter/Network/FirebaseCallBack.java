package com.example.shelter.Network;

import java.util.List;

public interface FirebaseCallBack<T> {
    void onCallBack(List<T> items);
}
