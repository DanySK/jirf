#!/bin/bash
$1
java -Xmx32m -version

linux () {
    unix_pre
    export JAVA_HOME="$HOME/.jabba/jdk/$JDK"
    export PATH="$JAVA_HOME/bin:$PATH"
}

osx () {
    unix_pre
    ls -ahl JAVA_HOME="$HOME/.jabba/jdk/$JDK/Content"
}

windows () {
    PowerShell -Command '[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-Expression ( Invoke-WebRequest https://github.com/shyiko/jabba/raw/master/install.ps1 -UseBasicParsing).Content'
}

unix_pre () {
    curl -sL https://github.com/shyiko/jabba/raw/master/install.sh | bash && . ~/.jabba/jabba.sh
    if ! jabba use $JDK; then
        jabba install "$JDK"
    fi
    unset _JAVA_OPTIONS
}
