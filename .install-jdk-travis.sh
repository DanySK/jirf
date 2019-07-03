#!/bin/bash

install_jdk () {
    if ! $jabba use $JDK; then
        $jabba install "$JDK"
    fi
}

unix_pre () {
    curl -sL https://github.com/shyiko/jabba/raw/master/install.sh | bash && . ~/.jabba/jabba.sh
    unset _JAVA_OPTIONS
    export jabba=jabba
}

linux () {
    unix_pre
    export JAVA_HOME="$HOME/.jabba/jdk/$JDK"
    export PATH="$JAVA_HOME/bin:$PATH"
}

osx () {
    unix_pre
    export JAVA_HOME="$HOME/.jabba/jdk/$JDK/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
}

windows () {
    PowerShell -ExecutionPolicy Bypass -Command '[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-Expression (Invoke-WebRequest https://github.com/shyiko/jabba/raw/master/install.ps1 -UseBasicParsing).Content'
    export jabba="$HOME/.jabba/bin/jabba.exe"
    # Apparently exported variables are ignored in subseguent phases on Windows. Go through .bashrc
    echo this was bashrc:
    cat ~/.bashrc
    echo --------end of bashrc-----------
    echo 'export JAVA_HOME="$HOME/.jabba/jdk/$JDK"' >> ~/.bashrc
    echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
    echo 'echo bashrc has been sourced' >> ~/.bashrc
    echo this is now bashrc:
    cat ~/.bashrc
    echo --------end of bashrc-----------
    source ~/.bashrc
}

echo "running ${TRAVIS_OS_NAME}-specific configuration"
$TRAVIS_OS_NAME
echo "installing / switching the JDK"
install_jdk
echo "checking if Java works:"
java -Xmx32m -version
