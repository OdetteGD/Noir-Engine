package com.noir.game.engine.scripting;

import java.io.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.*;

/** Compact versioned bytecode container used by the Noir .game compiler. */
public final class NoirGameBytecode {
    public static final int VERSION=1; private static final int MAGIC=0x4E47424B;
    public final int version;public final List<String> instructions;public final byte[] sourceHash;
    public NoirGameBytecode(List<String> instructions,byte[] sourceHash){this.version=VERSION;this.instructions=new ArrayList<>(instructions);this.sourceHash=sourceHash.clone();}
    public byte[] serialize(){try{ByteArrayOutputStream out=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(out);d.writeInt(MAGIC);d.writeInt(version);d.writeInt(instructions.size());for(String s:instructions)d.writeUTF(s);d.writeInt(sourceHash.length);d.write(sourceHash);d.flush();return out.toByteArray();}catch(IOException e){throw new IllegalStateException(e);}}
    public static NoirGameBytecode compileText(String source){try{List<String> ins=new ArrayList<>();for(String l:source.replace("\r","").split("\n")){String s=l.trim();if(s.isEmpty()||s.startsWith("#")||s.startsWith("//"))continue;ins.add("EXEC "+s);}MessageDigest md=MessageDigest.getInstance("SHA-256");return new NoirGameBytecode(ins,md.digest(source.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
