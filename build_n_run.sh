
# java -cp ./bin/:./lib/logback-classic-1.2.3.jar:./lib/logback-core-1.2.3.jar:./lib/slf4j-api-1.7.25.jar rtclog.parser.Main 

SHELL_PATH=$(cd "$(dirname "$0")";pwd)
CLASSPATH="${SHELL_PATH}/bin/"
CLASSPATH+=":${SHELL_PATH}/lib/logback-classic-1.2.3.jar"
CLASSPATH+=":${SHELL_PATH}/lib/logback-core-1.2.3.jar"
CLASSPATH+=":${SHELL_PATH}/lib/slf4j-api-1.7.25.jar"

RUN_CMD="${SHELL_PATH}/run.sh $@"

BIN_PATH="${SHELL_PATH}/bin"
SRC_PATH="${SHELL_PATH}/rtclog"
SRC_FILES+=" ${SRC_PATH}/rtclog/parser/Main.java"
SRC_FILES+=" ${SRC_PATH}/rtclog/parser/Analyzer1To1.java"
SRC_FILES+=" ${SRC_PATH}/rtclog/parser/StringHelper.java"
# SRC_FILES+=" ${SRC_PATH}/logback.xml"
BUILD_CMD="javac -d ${BIN_PATH}  ${SRC_FILES}  -cp l${CLASSPATH}" 

echo SHELL_PATH=${SHELL_PATH}
echo BUILD_CMD=${BUILD_CMD}
echo RUN_CMD=${RUN_CMD}

mkdir -p ${BIN_PATH} \
 && cp ${SRC_PATH}/logback.xml ${BIN_PATH}/ \
 && ${BUILD_CMD} \
 && echo "build ok" \
 && echo "runing..." \
 && ${RUN_CMD}
