#!/usr/bin/env python
from __future__ import print_function
import javalang
import os
import shutil
import subprocess

DECOMPILED_DIRECTORY = 'decompiled'
JAR_NAME = 'youtube.jar'
REQUIRED_METHOD_PARAMETERS = ['Uri', 'String', 'long']
APK_NAME = 'latest_youtube.apk'


def extractor(apk_url):
    os.mkdir(DECOMPILED_DIRECTORY)
    subprocess.call(['wget', '-O', APK_NAME, apk_url],
                    cwd=DECOMPILED_DIRECTORY)
    subprocess.call(['dex2jar', '-f', '-o', JAR_NAME,
                     APK_NAME], cwd=DECOMPILED_DIRECTORY)
    subprocess.call(['jd-core-java', JAR_NAME, '.'], cwd=DECOMPILED_DIRECTORY)
    file_name = subprocess.check_output(
        ['grep', '-irl', 'application/x-mpegURL";', '.'],
        cwd=DECOMPILED_DIRECTORY)
    file_name = file_name.strip()

    with open(os.path.join(DECOMPILED_DIRECTORY, file_name), 'r') as _file:
        java_source = _file.read()
    parse_tree = javalang.parse.parse(java_source)

    class_name = parse_tree.types[0].name
    shutil.rmtree(DECOMPILED_DIRECTORY, ignore_errors=True)

    print(class_name)
    return class_name
