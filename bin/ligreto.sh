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

[[ -n "${JAVA_HOME}" ]] || error "JAVA_HOME have to be set.";

LIGRETO_BIN=$(dirname "$0")
if [[ "$LIGRETO_BIN" = './' ]]; then
	LIGRETO_BIN="$(pwd)";
fi

LIGRETO_HOME=$(dirname "$LIGRETO_BIN");
LIGRETO_LOG="${LIGRETO_HOME}/log";
LIGRETO_LIB="${LIGRETO_HOME}/lib";
LIGRETO_DIST="${LIGRETO_HOME}/dist";

CLASSPATH=$(find "${LIGRETO_LIB}" -type f -name '*.jar' | tr '\n' ':');
CLASSPATH="${CLASSPATH}:$(find "${LIGRETO_DIST}" -type f -name '*.jar' | tr '\n' ':')";

"${JAVA_HOME}/bin/java" -cp "${CLASSPATH}" net.ligreto.Ligreto "$@";
