package com.example.shelter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class TermPrivacyFragment extends Fragment {

    static public final String TAG = TermPrivacyFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_term_privacy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView termPrivacy = (WebView) view.findViewById(R.id.webview);
        termPrivacy.setWebViewClient(new WebViewClient());
        termPrivacy.loadUrl("file:///android_asset/app_term_privacy.html");
    }
}