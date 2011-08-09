#!/bin/bash

#
# Displays the error message and exits.
#
error() {
	echo "ERROR: $1";
	shift 1;
	while -n "$1"; do
		echo "       $1";
		shift 1;
	done
	exit 99;
}

#
# Get the java executable
#
JAVA=$(which java);
if [[ -z "${JAVA}" ]] && [[ -z "${JAVA_HOME}" ]]; then
	error "No 'java' executable on the PATH and no JAVA_HOME is set.";
fi
[[ -n "${JAVA}" ]] || JAVA="${JAVA_HOME}/bin/java";
[[ -x "${JAVA}" ]] || error "File \"${JAVA}\" does not exist or is not executable.";

LIGRETO_BIN=$(dirname "$0")
if [[ "${LIGRETO_BIN}" = '.' ]]; then
	LIGRETO_BIN="$(pwd)";
fi

LIGRETO_HOME=$(dirname "$LIGRETO_BIN");
LIGRETO_LOG="${LIGRETO_HOME}/log";
LIGRETO_LIB="${LIGRETO_HOME}/lib";
LIGRETO_DIST="${LIGRETO_HOME}/dist";

CLASSPATH=$(find "${LIGRETO_LIB}" -type f -name '*.jar' | tr '\n' ':');
CLASSPATH="${CLASSPATH}:$(find "${LIGRETO_DIST}" -type f -name '*.jar' | tr '\n' ':')";

"${JAVA}" -cp "${CLASSPATH}" net.ligreto.Ligreto "$@";
