#!/bin/bash

# 1. Check if Bookkeeper.jar exists
if [ ! -f "cli/target/Bookkeeper.jar" ]; then
    echo "Building Bookkeeper.jar..."
    mvn package
    if [ $? -ne 0 ]; then
        echo "Build failed."
        exit 1
    fi
fi

# 2. Run the jar file
java -jar cli/target/Bookkeeper.jar "$@"
