package cl.uchile.ing.adi.upagos;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
    private static SharedPreferences SHARED;

    private static void start(Context context){
        SHARED = context.getSharedPreferences("upagos", Context.MODE_PRIVATE);
    }

    public static String get(Context context, String key){
        return Storage.get(context, key, null);
    }

    public static String get(Context context, String key, String def){
        if(SHARED==null) start(context);
        return  SHARED.getString(key, def);
    }
    public static void set(Context context, String key, String val){
        if(SHARED==null) start(context);
        SHARED.edit().putString(key, val).commit();
    }
    public static void rm(Context context, String key){
        if(SHARED==null) start(context);
        SHARED.edit().remove(key).commit();
    }
}
