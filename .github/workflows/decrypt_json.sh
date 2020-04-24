#!/bin/sh

cd $HOME/app

# --batch to prevent interactive command --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$DECRYPT_PP" \
--output google-services.json google-services.json.gpg

cd ..
