#!/usr/bin/python

import subprocess
import javalang

DECOMPILED_DIRECTORY = "decompiled"
JAR_NAME = "youtube.jar"
REQUIRED_METHOD_PARAMETERS = ["Uri", "String", "long"]
APK_NAME = "latest_youtube.apk"

def extractor(apk_url):
    subprocess.call(['mkdir', "-p", DECOMPILED_DIRECTORY])
    subprocess.call(['wget', '-O', APK_NAME, apk_url], cwd=DECOMPILED_DIRECTORY)
    subprocess.call(['dex2jar', "-f", "-o", JAR_NAME, APK_NAME], cwd=DECOMPILED_DIRECTORY)
    subprocess.call(['jd-core-java', JAR_NAME, "."], cwd=DECOMPILED_DIRECTORY)
    file_name = subprocess.check_output(['grep', "-irl", 'application/x-mpegURL";'], cwd=DECOMPILED_DIRECTORY)
    file_name = file_name.strip()

    java_source = subprocess.check_output(['cat', file_name], cwd=DECOMPILED_DIRECTORY)
    parse_tree = javalang.parse.parse(java_source)

    class_name = parse_tree.types[0].name
    subprocess.call(['rm', '-rf', DECOMPILED_DIRECTORY])

    print class_name
    return class_name
