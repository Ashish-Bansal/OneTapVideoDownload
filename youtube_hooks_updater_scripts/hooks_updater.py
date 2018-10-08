#!/usr/bin/env python
from __future__ import print_function
from bs4 import BeautifulSoup
import click
from hooks_extractor import extractor
import json
import os
from pipes import quote
import requests
import subprocess
import sys

YOUTUBE_APK_URL = 'https://www.androidapksfree.com/apk/youtube-apk-latest-version-download/download/'
HOOKS_DIRECTORY_PATH = 'app/src/main/assets'
HOOKS_FILENAME = 'HookClassnames.json'


def addNewHookClassnames(create_pr):
    app_details = apkDetailsFetcher()
    app_version = app_details['version']
    print('Latest Youtube version', app_version)
    if isAlreadySupported(app_version):
        print('Version already supported!')
        return

    direct_url = app_details['direct_url']
    class_name = extractor(direct_url)

    json_data = json.loads(getJsonString())
    json_data['Youtube'][app_version] = class_name
    with open(getHookFilePath(), 'w') as f:
        json.dump(json_data, f, sort_keys=True,
                  indent=4, separators=(',', ':'))
    print('Updated HookClassnames.json for version : ', app_version)
    f.close()
    if create_pr:
        print('Creating Pull Request')
        createGitPullRequest(app_version)
    else:
        print('Not creating Pull Request')


def apkDetailsFetcher():
    result = {}
    data = requests.get(YOUTUBE_APK_URL).text

    soup = BeautifulSoup(data, 'html.parser')
    links = soup.find_all('a')

    for link in links:
        x = link.get('href')
        if 'com.google.android.youtube_' in x:
            result['direct_url'] = x
            break

    version = result['direct_url'].split('/')[-1].split('-')[1]
    result['version'] = version[:6]
    return result


def isAlreadySupported(version):
    json_data = json.loads(getJsonString())
    youtube = json_data['Youtube']
    return version in youtube


def getHookFilePath():
    script_path = os.path.dirname(os.path.abspath(__file__))
    hook_file_path = os.path.join(
        script_path, '..', HOOKS_DIRECTORY_PATH, HOOKS_FILENAME)
    return hook_file_path


def getJsonString():
    return open(getHookFilePath()).read()


def directoryExists(directory_name):
    return os.path.isdir(directory_name)


def repoRootPath():
    script_path = os.path.dirname(os.path.abspath(__file__))
    repo_root_path = os.path.join(script_path, '..')
    return repo_root_path


def pullLatestChanges():
    repo_root_path = repoRootPath()
    try:
        subprocess.call(['git reset HEAD~100 --hard'],
                        shell=True, cwd=repo_root_path)
        subprocess.call(['git pull upstream master'],
                        shell=True, cwd=repo_root_path)
        subprocess.call(['git push origin master --force'],
                        shell=True, cwd=repo_root_path)
    except Exception as e:
        print(e)


def createGitPullRequest(version):
    repo_root_path = repoRootPath()
    comment = 'Added class names for Youtube version : {0}'.format(version)
    comment = quote(comment)
    try:
        subprocess.call(['git', 'add', '.'], shell=True, cwd=repo_root_path)
        subprocess.call(['git', 'commit',
                         '--author="Automation Daemon <me@ashishbansal.in>"',
                         '-m', comment], shell=True, cwd=repo_root_path)
        subprocess.call(['git', 'push', 'origin', 'master'],
                        cwd=repo_root_path)
        subprocess.call(['hub', 'pull-request', '-m', comment,
                         '-fb', 'Ashish-Bansal:master'], cwd=repo_root_path)
    except Exception as e:
        print(e)


@click.command()
@click.option('--youtube-apk-url', type=str, default=YOUTUBE_APK_URL,
              help='URL to download latest Youtube APKs')
@click.option('--create-pr', type=bool, default=False,
              help='Create a PR against master')
def main(youtube_apk_url, create_pr):
    YOUTUBE_APK_URL = youtube_apk_url
    pullLatestChanges()
    addNewHookClassnames(create_pr)


if __name__ == '__main__':
    main()
