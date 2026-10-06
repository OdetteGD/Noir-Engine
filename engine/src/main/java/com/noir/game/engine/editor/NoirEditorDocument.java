package com.noir.game.engine.editor;

import com.noir.game.engine.encryption.NoirGameCrypto;
import com.noir.game.engine.scripting.NoirGameBytecode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

/** Editor-side .game document with source, compiled bytecode and optional encrypted storage. */
public final class NoirEditorDocument {
    public String path="untitled.game";public String source="";public NoirGameBytecode bytecode;public boolean encrypted;
    public List<String> diagnostics=new ArrayList<>();
    public void compile(){bytecode=NoirGameBytecode.compileText(source);diagnostics.clear();}
    public byte[] saveEncrypted(char[] password)throws GeneralSecurityException{compile();encrypted=true;return NoirGameCrypto.encrypt(source,password);}
    public void loadEncrypted(byte[] data,char[] password)throws GeneralSecurityException{source=NoirGameCrypto.decryptText(data,password);encrypted=true;compile();}
    public byte[] savePlain(){encrypted=false;return source.getBytes(StandardCharsets.UTF_8);}
}
