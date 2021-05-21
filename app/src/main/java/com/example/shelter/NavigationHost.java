package com.example.shelter;


import androidx.fragment.app.Fragment;

public interface NavigationHost {
    public void navigateTo(Fragment fragment, boolean addToBackstack);
}
