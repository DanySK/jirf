#!/bin/bash
openssl aes-256-cbc -K $encrypted_ae2fe9b21dcc_key -iv $encrypted_ae2fe9b21dcc_iv -in prepare_environment.sh.enc -out prepare_environment.sh -d
bash prepare_environment.sh
./gradlew
./gradlew uploadArchives
