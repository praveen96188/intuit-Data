#!/bin/bash
#
#  Environment settings for backend processes.
#
#  Description: This script sets basic environment configurations
#               used by all other scripts.  This file should be
#               the single place for environment configuration
#               changes.
#

# save the current working directory so we can switch back to it later
SAVEDIR=$PWD

# grab the path of the script and assign to BE_HOME
BE_HOME=/apps/batch/jss

# we want the full path of the script (no relative path info)
# (this resolves cases like: ./script, dir/script, etc. to their full path)
# we also want to move back one dir level so all app dirs are visible to BE_HOME
cd $BE_HOME/
BE_HOME=$PWD

# so we play nice when we're imported into other scripts,
# change back to the directory we started at
cd $SAVEDIR

export JAVA_HOME=/usr/java/default/bin
# if the configuration service client is being used the system property app.system.env should be set for the specific environment
export JAVA_OPTS="-Dapp.system.env=dev -server -Xms100m -Xmx1024m -Djava.security.egd=file:/dev/../dev/urandom"

#NewRelic
#NEW_RELIC_JAR=/usr/local/newrelicmm/newrelic.jar
#if [ -e "$NEW_RELIC_JAR" ]
#then
#   JAVA_OPTS="$JAVA_OPTS -javaagent:$NEW_RELIC_JAR"
#fi


#YourKit
#export JAVA_OPTS="-client -Xms100m -Xmx1024m -agentpath:/usr/local/yourkit/libyjpagent.so=port=10001,dir=/tmp/yourkit"

export BE_ETC=$BE_HOME/etc
export BE_LIB=$BE_HOME/lib
export BE_LOG=$BE_HOME/logs
export BE_SHELL=$BE_HOME/shell

#
# Set classpath using the BE_ETC and BE_LIB directories (all jars in the lib dir)
#
#BE_CLASSPATH=`echo \`ls -1 $BE_LIB/spcf/*.jar\` | sed 's/ /:/g'`
BE_CLASSPATH=$BE_LIB
BE_CLASSPATH=$BE_CLASSPATH:`echo \.:$BE_ETC \`ls -1 $BE_LIB/*.jar\` | sed 's/ /:/g'`
export BE_CLASSPATH