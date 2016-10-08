#!/usr/bin/python2
from bs4 import BeautifulSoup
from hooks_extractor import extractor
import json
import requests
import subprocess
import os

YOUTUBE_APK_URL = 'http://www.androidapksfree.com/apk/youtube-apk-latest-version-download/'
HOOKS_DIRECTORY_PATH = 'app/src/main/assets'
HOOKS_FILENAME = 'HookClassnames.json'


def addNewHookClassnames():
    app_details = apkDetailsFetcher()
    app_version = app_details['version']
    print "Latest Youtube version", app_version
    if isAlreadySupported(app_version):
        print "Version already supported!"
        return

    direct_url = app_details['direct_url']
    class_name_pair = extractor(direct_url)
    main_class_name = class_name_pair[0]
    sub_class_name = class_name_pair[1]

    json_data = json.loads(getJsonString())
    json_data['Youtube'][app_version] = dict()
    json_data['Youtube'][app_version][main_class_name] = sub_class_name
    with open(getHookFilePath(), 'w') as f:
        json.dump(json_data, f, sort_keys=True, indent=4, separators=(',', ':'))
    print 'Updated HookClassnames.json for version : ', app_version
    f.close()
    createGitPullRequest(app_version)


def apkDetailsFetcher():
    result = {}
    data = requests.get(YOUTUBE_APK_URL).text

    soup = BeautifulSoup(data, "html.parser")
    links = soup.find_all('a')

    for link in links:
        x = link.get('href')
        if 'com.google.android.youtube_' in x:
            result['direct_url'] = x
            break

    title = soup.title.text.split()
    version = title[2].zfill(6)[1:7]

    result['version'] = version[:6]
    return result


def isAlreadySupported(version):
    json_data = json.loads(getJsonString())
    youtube = json_data['Youtube']
    return version in youtube


def getHookFilePath():
    script_path = os.path.dirname(os.path.abspath(__file__))
    hook_file_path = os.path.join(script_path, '..', HOOKS_DIRECTORY_PATH, HOOKS_FILENAME)
    return hook_file_path


def getJsonString():
    return open(getHookFilePath()).read()


def directoryExists(directory_name):
    return os.path.isdir(directory_name)


def createGitPullRequest(version):
    script_path = os.path.dirname(os.path.abspath(__file__))
    repo_root_path = os.path.join(script_path, '..')
    comment = '"Added class names for Youtube versions {0}"'.format(version)
    try:
        subprocess.call(["git pull && git add ."], shell=True, cwd=repo_root_path)
        subprocess.call(['git', 'commit', '-m', comment], cwd=repo_root_path)
        subprocess.call(['hub', 'pull-request', '-m', comment, '-fb' 'Ashish-Bansal:master'], cwd=repo_root_path)
    except Exception as e:
        print e


if __name__ == '__main__':
    addNewHookClassnames()
