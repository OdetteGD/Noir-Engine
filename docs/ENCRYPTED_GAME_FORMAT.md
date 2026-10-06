# Noir Encrypted `.game` Format

Noir does not use Base64 as encryption. Base64 is only an encoding and provides no secrecy.

## Container

```text
NOIRGAME | version | PBKDF2 iterations | salt length | IV length | salt | IV | AES-GCM ciphertext+tag
```

## Cryptography

- Key derivation: PBKDF2-HMAC-SHA256.
- Derived key: 256-bit.
- Default work factor: 120,000 iterations.
- Random salt: 16 bytes.
- Random GCM IV: 12 bytes.
- Authentication tag: 128-bit.
- Cipher: AES-GCM.
- Associated data: the Noir container header.

The format is proprietary to Noir, but the underlying cryptography intentionally uses established standards instead of a home-made cipher. This gives Noir a custom `.game` packaging layer without pretending a novel cipher is automatically secure.

## Tooling

`tools/NoirGamePack.java` reads `NOIR_GAME_KEY` from the environment and produces an encrypted `.game` binary. The password is deliberately not accepted on the command line.

```bash
export NOIR_GAME_KEY='your-development-key'
javac -cp engine/build/libs/noir-engine.jar tools/NoirGamePack.java
java -cp engine/build/libs/noir-engine.jar:tools NoirGamePack encrypt player.game player.encrypted.game
```

For production builds, the key should be injected by the application's secure build/signing system rather than committed to Git.
