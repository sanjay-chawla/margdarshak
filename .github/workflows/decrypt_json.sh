#!/bin/sh

PWD=$(pwd)
# --batch to prevent interactive command --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$DECRYPT_PP" \
--output $PWD/app/google-services.json $PWD/app/google-services.json.gpg

