package com.noir.game.engine.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

public final class NoirGameCrypto {
    private static final byte[] MAGIC={'N','O','I','R','G','A','M','E'};
    private static final byte VERSION_1=1;
    private static final byte VERSION_2=2;
    private static final int SALT_BYTES=16;
    private static final int IV_BYTES=12;
    private static final int KEY_BITS=256;
    private static final int PBKDF2_V1=120_000;
    private static final int PBKDF2_V2=600_000;
    private static final int MAX_ITERATIONS=2_000_000;

    private NoirGameCrypto(){}

    public static byte[] encrypt(String source,char[] password)throws GeneralSecurityException{
        return encrypt(source.getBytes(StandardCharsets.UTF_8),password);
    }

    public static byte[] encrypt(byte[] plain,char[] password)throws GeneralSecurityException{
        requirePassword(password);
        SecureRandom random=new SecureRandom();
        byte[] salt=new byte[SALT_BYTES],iv=new byte[IV_BYTES];
        random.nextBytes(salt);random.nextBytes(iv);
        byte[] key=null;
        try{
            key=derive(password,salt,PBKDF2_V2);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            cipher.updateAAD(header(VERSION_2,PBKDF2_V2,salt,iv));
            byte[] encrypted=cipher.doFinal(plain);
            ByteBuffer out=ByteBuffer.allocate(8+1+4+1+1+salt.length+iv.length+encrypted.length).order(ByteOrder.BIG_ENDIAN);
            out.put(MAGIC).put(VERSION_2).putInt(PBKDF2_V2).put((byte)salt.length).put((byte)iv.length).put(salt).put(iv).put(encrypted);
            return out.array();
        }finally{
            wipe(key);wipe(salt);wipe(iv);
        }
    }

    public static byte[] decrypt(byte[] blob,char[] password)throws GeneralSecurityException{
        requirePassword(password);
        if(blob==null||blob.length<8+1+4+1+1+12+12+17)throw new GeneralSecurityException("Corrupt Noir encrypted file");
        ByteBuffer in=ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN);
        byte[] magic=new byte[8];in.get(magic);
        if(!Arrays.equals(magic,MAGIC))throw new GeneralSecurityException("Not a Noir encrypted .game file");
        byte version=in.get();
        if(version!=VERSION_1&&version!=VERSION_2)throw new GeneralSecurityException("Unsupported Noir .game encryption version");
        int iterations=in.getInt();
        if(iterations<10_000||iterations>MAX_ITERATIONS)throw new GeneralSecurityException("Invalid key-derivation cost");
        int saltLength=in.get()&255,ivLength=in.get()&255;
        if(saltLength<12||saltLength>64||ivLength<12||ivLength>32||in.remaining()<saltLength+ivLength+17)throw new GeneralSecurityException("Corrupt Noir encrypted file");
        byte[] salt=new byte[saltLength],iv=new byte[ivLength];in.get(salt).get(iv);
        byte[] encrypted=new byte[in.remaining()];in.get(encrypted);
        byte[] key=null;
        try{
            key=derive(password,salt,iterations);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            cipher.updateAAD(header(version,iterations,salt,iv));
            return cipher.doFinal(encrypted);
        }finally{
            wipe(key);wipe(salt);wipe(iv);wipe(encrypted);
        }
    }

    public static String decryptText(byte[] blob,char[] password)throws GeneralSecurityException{
        return new String(decrypt(blob,password),StandardCharsets.UTF_8);
    }

    public static boolean isEncrypted(byte[] data){
        return data!=null&&data.length>=9&&Arrays.equals(Arrays.copyOf(data,8),MAGIC);
    }

    public static int encryptionVersion(byte[] data){
        if(!isEncrypted(data))return 0;
        return data[8]&255;
    }

    private static void requirePassword(char[] password)throws GeneralSecurityException{
        if(password==null||password.length<8)throw new GeneralSecurityException("Noir key must contain at least 8 characters");
    }

    private static byte[] derive(char[] password,byte[] salt,int iterations)throws GeneralSecurityException{
        SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec=new PBEKeySpec(password,salt,iterations,KEY_BITS);
        return factory.generateSecret(spec).getEncoded();
    }

    private static byte[] header(byte version,int iterations,byte[] salt,byte[] iv){
        ByteBuffer b=ByteBuffer.allocate(8+1+4+1+1+salt.length+iv.length).order(ByteOrder.BIG_ENDIAN);
        return b.put(MAGIC).put(version).putInt(iterations).put((byte)salt.length).put((byte)iv.length).put(salt).put(iv).array();
    }

    private static void wipe(byte[] data){if(data!=null)Arrays.fill(data,(byte)0);}
}
