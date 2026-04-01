#!/bin/sh

APACHE_ZIP_FILE=$HOME/Downloads/binaries/apache-tomcat-9.0.63.zip
if [ -f "$APACHE_ZIP_FILE" ]
then
  sudo rm $APACHE_ZIP_FILE
fi
echo "Downloading apache-tomcat-9.0.63.zip"
sudo curl -o $APACHE_ZIP_FILE https://artifact.intuit.com/artifactory/maven-proxy/org/apache/tomcat/tomcat/9.0.63/tomcat-9.0.63.zip
CATALINA_HOME=$HOME/Downloads/binaries/apache-tomcat-9.0.63
if [ -d  "$CATALINA_HOME" ]
then
  sudo rm -rf $CATALINA_HOME
fi
sudo unzip $APACHE_ZIP_FILE -d "$HOME/Downloads/binaries"
sudo rm $APACHE_ZIP_FILE
if [ ! -d  "$CATALINA_HOME" ]
then
  echo "[ERROR] Tomcat installation failed"
  exit 1
fi
echo "export CATALINA_HOME=$CATALINA_HOME">> $RC_FILE
