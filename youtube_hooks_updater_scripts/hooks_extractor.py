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

    main_class_name = parse_tree.types[0].name
    sub_class_name = ""

    for declaration in parse_tree.types[0].body:
        if type(declaration) is javalang.tree.MethodDeclaration:
            method = declaration
            current_method_parameters = list()
            for parameter in method.parameters:
                current_method_parameters.append(parameter.type.name)
                if current_method_parameters == REQUIRED_METHOD_PARAMETERS :
                    for statement in method.body :
                        if type(statement) is javalang.tree.LocalVariableDeclaration:
                            local_variable_declaration = statement
                            sub_class_name = local_variable_declaration.declarators[0].initializer.type.name

    subprocess.call(['rm', '-rf', DECOMPILED_DIRECTORY])

    print main_class_name, sub_class_name
    return [main_class_name, sub_class_name]
