package com.phantom.onetapvideodownload;

public class YouTubePackage {
    public String mMainClass;
    public String mMethodParameterClass;

    public YouTubePackage(String mainClass, String methodParameterClass) {
        mMainClass = mainClass;
        mMethodParameterClass = methodParameterClass;
    }

    public String getMainClass() {
        return mMainClass;
    }

    public String getMethodParameterClass() {
        return mMethodParameterClass;
    }
}
