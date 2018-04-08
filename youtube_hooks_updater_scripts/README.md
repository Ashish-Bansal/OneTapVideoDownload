# Youtube hooks extractor

Simple python scripts to extract hooks from the latest Youtube APK

## Usage

Use the `hooks_updater.py` to download the latest Youtube APK and extract hooks from it

```sh
$ ./hooks_updater.py --help
Usage: hooks_updater.py [OPTIONS]

Options:
  --youtube-apk-url TEXT    URL to download latest Youtube APKs
  --dont-create-pr BOOLEAN  Create a PR against master
  --help                    Show this message and exit.
```

## Requirements

- Python 2/3
- Packages from `requirements.txt`
- [dex2jar](https://github.com/pxb1988/dex2jar)
    - Expects a script called `dex2jar` in the `PATH` that would decompile the apk.
    - The `d2j-dex2jar.sh` should do the trick but it needs to be named as `dex2jar`.
- [jd-core-java](https://github.com/nviennot/jd-core-java)
    - Expects a script called `jd-core-java` in the `PATH` which would execute the `jd-core-java` 
    jar with the specified parameters.

