#!/usr/bin/python
from __future__ import print_function
from bs4 import BeautifulSoup
import json
import requests
import subprocess
import os


url = "http://www.androidapksfree.com/apk/youtube-apk-latest-version-download/"

jsonString = open('HookClassnames.json').read()


def process(key_val_pair):

    key = ""
    val = ""

    for k in key_val_pair:
        key, val = k, key_val_pair[k]

    app = apkDetailsFetcher(url)  # Printable print(app)
    app_url = app['url']
    app_version = app['version']
    print(">> Latest Version:" + str(app_version))
    app_found = app['found']

    if(app_found == 0):
        json_data = json.loads(jsonString)
        json_data['Youtube'][str(app_version)] = dict()
        # Key_val pair changes later
        json_data['Youtube'][str(app_version)][key] = val
        with open('HookClassnames.json', 'w') as f:
            json.dump(json_data, f, sort_keys=True, indent=4)
        print("\n>> HookClassnames.json Updated with " + str(app_version))
        f.close()

        # Downloading APK
        print("\nDownloading APK")
        if(checkDir('bin')):
            subprocess.call("rm -rf bin", shell=True)
            subprocess.call("mkdir bin", shell=True)
            subprocess.call(
                ["wget --quiet {0} -P bin/".format(app_url)], shell=True)
        else:
            subprocess.call(
                ["mkdir bin && wget {0} -P bin/".format(app_url)],
                shell=True)

    else:
        print(">> Found " + str(app_version) + " in HookClassnames.json")

    # Returning app_version for git-auto commits
    return app_version


def apkDetailsFetcher(url):
    result = {}
    data = requests.get(url).text

    soup = BeautifulSoup(data, "html.parser")
    links = soup.find_all('a')

    for link in links:
        x = link.get('href')
        if('com.google.android.youtube_' in x):
            result['url'] = x
            break

    title = soup.title.text.split()
    version = title[2].zfill(6)[1:7]

    result['version'] = int(version)
    result['found'] = isAlreadySupported(version)

    return result


def isAlreadySupported(version):
    json_data = json.loads(jsonString)
    youtube = json_data['Youtube']
    if (str(version) not in youtube):
        return 0
    else:
        return 1


def checkDir(directoryName):
    if(os.path.isdir(directoryName)):
        return 1
    else:
        return 0
