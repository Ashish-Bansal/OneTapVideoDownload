package com.phantom.onetapvideodownload.UriMediaChecker;

import com.phantom.onetapvideodownload.Video.Video;

interface AbstractUriChecker {
    Video checkUrl(String url);
}
