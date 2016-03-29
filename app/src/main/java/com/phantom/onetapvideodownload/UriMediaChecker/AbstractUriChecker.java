package com.phantom.onetapvideodownload.UriMediaChecker;

import com.phantom.onetapvideodownload.Video.Video;

public interface AbstractUriChecker {
    Video checkUrl(String url);
}
