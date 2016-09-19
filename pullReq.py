from __future__ import print_function
import subprocess


def pullReq(ver):
    comment = '"Added class names for Youtube versions ' + str(ver) + '"'
    try:
        subprocess.call("git pull && git add .", shell=True)
        subprocess.call(["git commit -m {0}".format(comment)], shell=True)
        subprocess.call("git push snbk97 master", shell=True)
        subprocess.call(
            ["hub pull-request -m {0} -fb Ashish-Bansal:master".format(comment)], shell=True)

    except Exception as e:
        print("Error occured")
        print(e)
