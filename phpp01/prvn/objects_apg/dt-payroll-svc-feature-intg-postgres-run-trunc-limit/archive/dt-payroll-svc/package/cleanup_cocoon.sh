#!/bin/sh
CleanupCocoon=$1

if [ "$CleanupCocoon" == "Yes" ]
 then
    echo "Cleaning up .m2 data"
    rm -rf /root/.m2/
fi
