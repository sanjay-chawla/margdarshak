#!/bin/sh
pwd
# --batch to prevent interactive command --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$DECRYPT_PP" \
--output $HOME/app/google-services.json $HOME/app/google-services.json.gpg

