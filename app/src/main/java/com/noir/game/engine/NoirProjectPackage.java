package com.noir.game.engine;

import android.content.Context;
import android.net.Uri;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

public final class NoirProjectPackage {
    private NoirProjectPackage() {}

    public static void export(Context context, File project, OutputStream output) throws IOException {
        ZipOutputStream zip=new ZipOutputStream(new BufferedOutputStream(output));
        addDirectory(zip, project, project);
        addRuntimeLibrary(context, zip);
        String manifest="{\n" +
                "  \"format\": \"noir-project-package-v1\",\n" +
                "  \"engine\": \"Noir 3D Game Engine\",\n" +
                "  \"nativeLibrary\": \"libnoir3d.so\",\n" +
                "  \"assetsRoot\": \"assets/\",\n" +
                "  \"androidTarget\": 35\n" +
                "}\n";
        zip.putNextEntry(new ZipEntry("runtime/noir-package.json"));
        zip.write(manifest.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
        zip.finish();
    }

    private static void addDirectory(ZipOutputStream zip, File root, File file) throws IOException {
        File[] children=file.listFiles();
        if(children==null) return;
        for(File child:children) {
            if(child.isDirectory()) {
                addDirectory(zip,root,child);
            } else if(!child.getName().equals(".gitkeep")) {
                String rel=root.toURI().relativize(child.toURI()).getPath();
                zip.putNextEntry(new ZipEntry("project/"+rel));
                copy(child,zip);
                zip.closeEntry();
            }
        }
    }

    private static void addRuntimeLibrary(Context context, ZipOutputStream zip) throws IOException {
        File nativeDir=new File(context.getApplicationInfo().nativeLibraryDir);
        File lib=new File(nativeDir,"libnoir3d.so");
        if(!lib.isFile()) throw new FileNotFoundException("libnoir3d.so is not installed in this build");
        String abi=android.os.Build.SUPPORTED_ABIS.length>0?android.os.Build.SUPPORTED_ABIS[0]:"unknown";
        zip.putNextEntry(new ZipEntry("runtime/lib/"+abi+"/libnoir3d.so"));
        copy(lib,zip);
        zip.closeEntry();
    }

    private static void copy(File file,OutputStream out) throws IOException {
        try(InputStream in=new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer=new byte[8192]; int n;
            while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
        }
    }

    public static File importPackage(Context context, Uri uri, File destination) throws IOException {
        if(destination.exists()) throw new IOException("Destination already exists");
        destination.mkdirs();
        File temp=File.createTempFile("noir-import-",".zip",context.getCacheDir());
        try(InputStream in=context.getContentResolver().openInputStream(uri);
            OutputStream out=new FileOutputStream(temp)) {
            if(in==null) throw new IOException("Unable to open selected package");
            byte[] b=new byte[8192]; int n;
            while((n=in.read(b))!=-1) out.write(b,0,n);
        }
        try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(new FileInputStream(temp)))) {
            ZipEntry e;
            while((e=zip.getNextEntry())!=null) {
                String name=e.getName();
                if(name.contains("..") || name.startsWith("/") || name.startsWith("\\")) throw new IOException("Unsafe archive path");
                if(!name.startsWith("project/")) continue;
                File out=new File(destination,name.substring("project/".length()));
                String base=destination.getCanonicalPath()+File.separator;
                if(!out.getCanonicalPath().startsWith(base)) throw new IOException("Archive path escapes project");
                if(e.isDirectory()) out.mkdirs();
                else {
                    File parent=out.getParentFile(); if(parent!=null) parent.mkdirs();
                    try(FileOutputStream fos=new FileOutputStream(out)) {
                        byte[] b=new byte[8192]; int n;
                        while((n=zip.read(b))!=-1) fos.write(b,0,n);
                    }
                }
            }
        } finally { temp.delete(); }
        if(!new File(destination,"project.game").isFile()) throw new IOException("Invalid Noir project package");
        return destination;
    }
}
