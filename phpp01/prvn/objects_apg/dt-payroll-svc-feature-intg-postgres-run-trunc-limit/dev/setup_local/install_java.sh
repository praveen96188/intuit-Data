#!/bin/sh

if [ `java -version 2>&1 | grep -o "Unable to locate a Java Runtime." | wc -l` = "1" ]
then
  read -p "Unable to locate a Java Runtime. Do you wish to install java 8 (amazon-corretto-8) (y/n)? " install_java_flag
    if [ "$install_java_flag" = "n" ]
    then
      echo "Skipping java installation..."
      exit 0
    fi
elif [ `java -version 2>&1 | grep -o "1.8." | wc -l` = "0" ]
then
  read -p "Java 8 was not found. Do you wish to install Java 8 (amazon-corretto-8) (y/n)? " install_java_flag
      if [ "$install_java_flag" = "n" ]
      then
        echo "Skipping java installation..."
        exit 0
      fi
fi
if [[ ! -d /Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk ]]
then
  sudo curl -o ~/Downloads/binaries/amazon-corretto-8.312.07.1-macosx-x64.tar.gz https://corretto.aws/downloads/resources/8.312.07.1/amazon-corretto-8.312.07.1-macosx-x64.tar.gz
  sudo tar -xzvf ~/Downloads/binaries/amazon-corretto-8.312.07.1-macosx-x64.tar.gz -C ~/Downloads/binaries/
  sudo rm ~/Downloads/binaries/amazon-corretto-8.312.07.1-macosx-x64.tar.gz
  sudo cp -R ~/Downloads/binaries/amazon-corretto-8.jdk /Library/Java/JavaVirtualMachines/
  sudo rm -rf ~/Downloads/binaries/amazon-corretto-8.jdk
fi

if [ "$JAVA_HOME" != "/Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk/Contents/Home" ]
then
  echo "export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk/Contents/Home" >>$RC_FILE
fi

case ":$PATH:" in
  *:$JAVA_HOME:*) echo "PATH already has $JAVA_HOME";;
  *) echo "export PATH=\$PATH:\$JAVA_HOME" >>$RC_FILE ;;
esac

source $RC_FILE

if [ `java -version 2>&1 | grep -o "OpenJDK Runtime Environment Corretto-8" | wc -l` = "0" ]
then
  echo "[ERROR] Java installation failed."
  sleep 5
fi
