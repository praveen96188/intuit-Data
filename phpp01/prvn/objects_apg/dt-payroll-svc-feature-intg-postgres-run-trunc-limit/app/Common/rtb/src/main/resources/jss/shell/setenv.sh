#!/bin/bash
#
#  Environment settings for backend processes.
#
#  Maintained by: Rob Histing / Jim Newell / Ken Paul
#
#  Description: This script sets basic environment configurations
#               used by all other scripts.  This file should be
#               the single place for environment configuration
#               changes.
#

# save the current working directory so we can switch back to it later
SAVEDIR=$PWD

# grab the path of the script and assign to BE_HOME
# (note: dirname will always return a path, even if it is only '.')
# (      put another way, dirname will never return null)
RTB_HOME=`dirname $0`

# we want the full path of the script (no relative path info)
# (this resolves cases like: ./script, dir/script, etc. to their full path)
# we also want to move back one dir level so all app dirs are visible to BE_HOME
cd $RTB_HOME/..
RTB_HOME=$PWD

# so we play nice when we're imported into other scripts,
# change back to the directory we started at
cd $SAVEDIR

export JAVA_HOME=/usr/java/default/bin
export JAVA_OPTS="-server -Xms128m -Xmx4096m -XX:MaxPermSize=768m -Dmonitor-work-dir=/apps/batch/monitor/work/ -Dtomcat-status-file=/usr/local/tomcat/webapps/ROOT/status.html"
export RTB_ETC=$RTB_HOME/etc
export RTB_LIB=$RTB_HOME/lib
export RTB_LOG=$RTB_HOME/logs
export RTB_SHELL=$RTB_HOME/shell

#
# Set classpath using the BE_ETC and BE_LIB directories (all jars in the lib dir)
#
RTB_CLASSPATH=$RTB_LIB
RTB_CLASSPATH=$RTB_CLASSPATH:`echo \.:$RTB_ETC \`ls -1 $RTB_LIB/*.jar\` | sed 's/ /:/g'`
export RTB_CLASSPATH
