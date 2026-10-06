package com.noir.game.engine.assets;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Binary GLB intake validator. It parses the GLB container header/chunks and exposes
 * JSON/BIN payloads to a future mesh/material decoder. This is intentionally safer than
 * pretending an arbitrary model was imported: malformed containers return diagnostics. */
public final class GltfAssetImporter {
    public static final class Result { public boolean valid; public String version=""; public byte[] json; public byte[] bin; public final List<String> diagnostics=new ArrayList<>(); }
    public Result inspect(byte[] data){
        Result r=new Result(); if(data==null||data.length<12){r.diagnostics.add("GLB too small");return r;}
        ByteBuffer b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);int magic=b.getInt(),version=b.getInt(),length=b.getInt();
        if(magic!=0x46546C67){r.diagnostics.add("Invalid GLB magic");return r;} if(version!=2){r.diagnostics.add("Unsupported glTF binary version: "+version);return r;} if(length>data.length){r.diagnostics.add("Declared GLB length exceeds file");return r;}
        r.version="2.0";int offset=12;while(offset+8<=length){int chunkLength=b.getInt(offset),chunkType=b.getInt(offset+4);offset+=8;if(chunkLength<0||offset+chunkLength>length){r.diagnostics.add("Invalid GLB chunk length");return r;}byte[] chunk=Arrays.copyOfRange(data,offset,offset+chunkLength);if(chunkType==0x4E4F534A)r.json=chunk;else if(chunkType==0x004E4942)r.bin=chunk;else r.diagnostics.add("Unknown GLB chunk 0x"+Integer.toHexString(chunkType));offset+=chunkLength;}
        if(r.json==null)r.diagnostics.add("Missing JSON chunk");else r.valid=true;return r;
    }
    public String jsonText(Result r){return r.json==null?"":new String(r.json,StandardCharsets.UTF_8).trim();}
    public Result inspect(InputStream in)throws IOException{try(InputStream x=in){return inspect(readAll(x));}}
    private byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[]buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);return out.toByteArray();}
}
