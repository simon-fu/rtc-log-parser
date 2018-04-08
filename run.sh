

SHELL_PATH=$(cd "$(dirname "$0")";pwd)
CLASSPATH="${SHELL_PATH}/bin/"
CLASSPATH+=":${SHELL_PATH}/lib/logback-classic-1.2.3.jar"
CLASSPATH+=":${SHELL_PATH}/lib/logback-core-1.2.3.jar"
CLASSPATH+=":${SHELL_PATH}/lib/slf4j-api-1.7.25.jar"
ENTRY_CLASS="rtclog.parser.Main"
RUN_CMD="java -cp ${CLASSPATH} ${ENTRY_CLASS} $@"

# echo SHELL_PATH=${SHELL_PATH}
# echo RUN_CMD=${RUN_CMD}

${RUN_CMD}