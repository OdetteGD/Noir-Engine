package com.noir.game.engine;

import android.content.Context;
import java.io.*;
import java.util.*;

/** Installs the C# SDK bundle from APK assets into the app-private runtime cache. */
public final class NoirCSharpRuntime {
    private NoirCSharpRuntime(){}

    public static File ensureInstalled(Context context) throws IOException {
        File root=new File(context.getFilesDir(),"noir-csharp/sdk");
        if(!root.exists() && !root.mkdirs()) throw new IOException("Unable to create C# SDK directory");
        String[] names=context.getAssets().list("csharp/sdk");
        if(names==null) names=new String[0];
        int copied=0;
        for(String name:names){
            if(name==null || !name.endsWith(".dll")) continue;
            File target=new File(root,name);
            copyAsset(context,"csharp/sdk/"+name,target);
            copied++;
        }
        if(copied==0) throw new IOException("No C# SDK DLLs were packaged in the APK");
        return root;
    }

    private static void copyAsset(Context context,String asset,File target) throws IOException {
        try(InputStream in=context.getAssets().open(asset);
            OutputStream out=new BufferedOutputStream(new FileOutputStream(target))){
            byte[] buffer=new byte[16384];
            int n;
            while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
        }
    }
}
