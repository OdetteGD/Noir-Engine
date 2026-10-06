# Noir 3D Engine signing

Never commit a release keystore or passwords.

Local builds can use:
NOIR_KEYSTORE=/absolute/path/noir-release.jks
NOIR_STORE_PASSWORD=...
NOIR_KEY_ALIAS=...
NOIR_KEY_PASSWORD=...

Or create an untracked root file named keystore.properties:

storeFile=/absolute/path/noir-release.jks
storePassword=...
keyAlias=NoirEngine
keyPassword=...

GitHub Actions should store these as repository secrets and provide them to the release job. The release build falls back to the Android debug key only when no release keystore is configured; this is for development/testing and is not a production signing identity.

The editor's project package contains the engine runtime library as:
runtime/lib/<ABI>/libnoir3d.so
and the project files/assets under:
project/
