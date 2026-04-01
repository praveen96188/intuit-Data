#!/bin/sh
CleanupCocoon=$1

if [ "${CleanupCocoon}" = "YES" ]; then
    echo "Cleaning up Cocoon data"
    rm -rf /root/.m2/
fi
