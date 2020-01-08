#! /bin/bash
#$1 <Jar_path>    eg : /home/user/.m2/repository/org/jsoup/jsoup/1.12.1/jsoup-1.12.1.jar
#$2 <module_name> eg : org.jsoup
# if you are not sure, module name can be found after executing the line below, as the folder created is module name.

declare -A module_folders
module_folders=( ["org/jsoup/jsoup"]="org.jsoup" )
repo="$HOME/.m2/repository"
echo $repo
for i in "${!module_folders[@]}"; do
    echo "remove module folder $repo/$i"
    rm -rf "$repo/$i"
done
mvn compile

for key in "${!module_folders[@]}"; do
    module_path="$repo/$key"
    jar_path=`ls $module_path/**/*.jar`
    module_name=${module_folders[$key]}
    echo $jar_path $key ${module_folders[$key]}
    jdeps --generate-module-info . $jar_path
    javac --patch-module $module_name=$jar_path $module_name/module-info.java
    jar uf $jar_path -C $module_name module-info.class
done

mvn javafx:jlink
