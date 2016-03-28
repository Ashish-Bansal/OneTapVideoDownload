package com.phantom.onetapvideodownload.utils.enums;

import android.Manifest;

import java.util.HashMap;
import java.util.Map;

public enum AppPermissions {
    External_Storage_Permission(0, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private final int mValue;
    private final String mName;
    AppPermissions(final int value, final String name) {
        mValue = value;
        mName = name;
    }

    private static Map<Integer, AppPermissions> map = new HashMap<>();

    static {
        for (AppPermissions legEnum : AppPermissions.values()) {
            map.put(legEnum.mValue, legEnum);
        }
    }

    public int getPermissionCode() {
        return mValue;
    }

    public String getPermissionName() {
        return mName;
    }

    public static AppPermissions getPermission(int value) {
        return map.get(value);
    }
}
