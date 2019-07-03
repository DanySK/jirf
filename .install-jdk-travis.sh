#!/bin/bash

install_jdk () {
    if ! jabba use $JDK; then
        jabba install "$JDK"
    fi
}

unix_pre () {
    curl -sL https://github.com/shyiko/jabba/raw/master/install.sh | bash && . ~/.jabba/jabba.sh
    unset _JAVA_OPTIONS
}

linux () {
    unix_pre
    export JAVA_HOME="$HOME/.jabba/jdk/$JDK"
}

osx () {
    unix_pre
    ls -ahl JAVA_HOME="$HOME/.jabba/jdk/$JDK/Contents/Home"
}

windows () {
    PowerShell -ExecutionPolicy Bypass -Command '[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-Expression (Invoke-WebRequest https://github.com/shyiko/jabba/raw/master/install.ps1 -UseBasicParsing).Content'
    alias jabba="$HOME/.jabba/bin/jabba.exe"
    export JAVA_HOME="$HOME/.jabba/jdk/$JDK"
}

$1
install_jdk
export PATH="$JAVA_HOME/bin:$PATH"
java -Xmx32m -version
