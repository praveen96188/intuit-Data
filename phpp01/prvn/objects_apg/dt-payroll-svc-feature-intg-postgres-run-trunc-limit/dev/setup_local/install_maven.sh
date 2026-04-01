#!/bin/sh

if [[ ! -f $HOME/.m2/settings.xml ]]
then
  cp $WORKING_DIR/dev/setup_local/settings.xml ~/.m2
else
  echo "settings.xml already exists in ~/.m2 folder."
  if [ `diff -wB "$HOME/.m2/settings.xml" "$WORKING_DIR/dev/setup_local/settings.xml" | wc -l` = "0" ]
  then
    echo "settings.xml verified."
  else
    read -p "~/.m2/settings.xml differs from the expected file. Do you wish to replace it? (y/n) (default is y) " replace_settings_xml
    if [ "$install_maven_flag" != "n" ]
      then
        cp $WORKING_DIR/dev/setup_local/settings.xml ~/.m2
    fi
  fi
fi

if [ `mvn -version 2>&1 | grep -o "command not found: mvn" | wc -l` = "1" ]
then
  read -p "Maven is not installed, Do you wish to install Apache Maven 3.6.3 (y/n)? " install_maven_flag
    if [ "$install_maven_flag" = "n" ]
    then
      echo "Skipping maven installation..."
      exit 0
    fi
elif [ `mvn -version 2>&1 | grep -o "Apache Maven 3.[6-9]" | wc -l` = "0" ] && [ `mvn -version 2>&1 | grep -o "Apache Maven [4-9]" | wc -l` = "0" ]
then
  read -p "Maven version 3.6.0 or above was not found. Do you wish to install  Apache Maven 3.6.3 (y/n)? " install_maven_flag
      if [ "$install_maven_flag" = "n" ]
      then
        echo "Skipping maven installation..."
        exit 0
      fi
fi

if [[ ! -d ~/Downloads/binaries/apache-maven-3.6.3 ]]
then
  echo "maven-3.6.3 not found at ~/Downloads/binaries"
  sudo curl -o ~/Downloads/binaries/apache-maven-3.6.3-bin.zip https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip
  sudo unzip ~/Downloads/binaries/apache-maven-3.6.3-bin.zip -d ~/Downloads/binaries/
  sudo rm ~/Downloads/binaries/apache-maven-3.6.3-bin.zip
fi

if [ "$MAVEN_HOME" != "$HOME/Downloads/binaries/apache-maven-3.6.3" ]
then
  echo "export MAVEN_HOME=$HOME/Downloads/binaries/apache-maven-3.6.3" >>$RC_FILE
fi

case ":$PATH:" in
  *:$MAVEN_HOME/bin:*) echo "PATH already has $MAVEN_HOME/bin";;
  *) echo "export PATH=\$PATH:\$MAVEN_HOME/bin" >>$RC_FILE ;;
esac

source $RC_FILE
mkdir -p ~/.m2

if [ `mvn -version | grep -o "Apache Maven 3.6.3" | wc -l` = "0" ]
then
  echo "[ERROR] Maven installation failed."
  sleep 5
fi