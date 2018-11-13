package com.learning.aman.mapapi.PrefrenceManager;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "Location";
    private static final String CURRENT_USER = "CurrentUser";

    public PrefManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public String getCurrentUser() {
        return pref.getString(CURRENT_USER, null);
    }

    public void setCurrentUser(String uid){
        editor.putString(CURRENT_USER,uid);
        editor.commit();
    }
}
