package com.noir.game.engine.encryption;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;

/**
 * Noir encrypted .game container. This is NOT Base64 and does not invent a new cryptographic
 * primitive: it uses AES-256-GCM and PBKDF2-HMAC-SHA256 inside a Noir-specific binary envelope.
 * The format is versioned, authenticated and intentionally opaque to casual source readers.
 */
public final class NoirGameCrypto {
    private static final byte[] MAGIC={'N','O','I','R','G','A','M','E'};
    private static final byte VERSION=1;
    private static final int SALT=16, IV=12, ITER=120_000, KEY=256;
    private NoirGameCrypto(){}
    public static byte[] encrypt(String source,char[] password)throws GeneralSecurityException{return encrypt(source.getBytes(StandardCharsets.UTF_8),password);}
    public static byte[] encrypt(byte[] plain,char[] password)throws GeneralSecurityException{
        if(password==null||password.length<8)throw new GeneralSecurityException("Noir key must contain at least 8 characters");
        SecureRandom r=new SecureRandom();byte[] salt=new byte[SALT],iv=new byte[IV];r.nextBytes(salt);r.nextBytes(iv);
        byte[] key=derive(password,salt);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));c.updateAAD(header(salt,iv));byte[] enc=c.doFinal(plain);
        ByteBuffer b=ByteBuffer.allocate(8+1+4+1+1+salt.length+iv.length+enc.length).order(ByteOrder.BIG_ENDIAN);b.put(MAGIC).put(VERSION).putInt(ITER).put((byte)SALT).put((byte)IV).put(salt).put(iv).put(enc);return b.array();
    }
    public static byte[] decrypt(byte[] blob,char[] password)throws GeneralSecurityException{
        ByteBuffer b=ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN);byte[] magic=new byte[8];b.get(magic);if(!Arrays.equals(magic,MAGIC))throw new GeneralSecurityException("Not a Noir encrypted .game file");
        if(b.get()!=VERSION)throw new GeneralSecurityException("Unsupported Noir .game encryption version");int iter=b.getInt();int sl=b.get()&255,il=b.get()&255;if(sl<12||il<12||b.remaining()<sl+il+17)throw new GeneralSecurityException("Corrupt Noir encrypted file");byte[] salt=new byte[sl],iv=new byte[il];b.get(salt).get(iv);byte[] enc=new byte[b.remaining()];b.get(enc);
        byte[] key=derive(password,salt,iter);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));c.updateAAD(header(salt,iv));return c.doFinal(enc);
    }
    public static boolean isEncrypted(byte[] b){return b!=null&&b.length>=9&&Arrays.equals(Arrays.copyOf(b,8),MAGIC);}
    public static String decryptText(byte[] blob,char[] password)throws GeneralSecurityException{return new String(decrypt(blob,password),StandardCharsets.UTF_8);}
    private static byte[] derive(char[] p,byte[] s)throws GeneralSecurityException{return derive(p,s,ITER);}
    private static byte[] derive(char[] p,byte[] s,int i)throws GeneralSecurityException{SecretKeyFactory f=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");KeySpec spec=new PBEKeySpec(p,s,i,KEY);return f.generateSecret(spec).getEncoded();}
    private static byte[] header(byte[] salt,byte[] iv){ByteBuffer b=ByteBuffer.allocate(8+1+4+1+1+salt.length+iv.length);b.put(MAGIC).put(VERSION).putInt(ITER).put((byte)salt.length).put((byte)iv.length).put(salt).put(iv);return b.array();}
}
