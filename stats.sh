#!/bin/bash

SRC_LINES=$(cat `find src -name '*.java' -o -name '*.xml'` | wc -l);
TEST_LINES=$(cat `find tests -name '*.java' -o -name '*.xml'` | wc -l);
echo "Application source code line count: ${SRC_LINES}";
echo "Test source code line count:        ${TEST_LINES}";
echo "Total source code line count:       $((SRC_LINES+TEST_LINES))";

