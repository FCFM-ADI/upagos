package cl.uchile.ing.adi.upagos;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class Messages {
    public static void snackbar(int msgId, View v, Context context){
        snackbar(context.getString(msgId), v, context);
    }
    public static void snackbar(String msg, View v, Context context){
        Snackbar sb = Snackbar.make(v, msg, Snackbar.LENGTH_SHORT);
        sb.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
        sb.show();
    }

    public static void alert(int msgId, int okId, DialogInterface.OnClickListener okListener, final Activity activity){
        alert(activity.getString(msgId),activity.getString(okId), okListener, activity);
    }

    public static void alert(String msg, String ok, DialogInterface.OnClickListener okListener, final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.app_name)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton(ok, okListener).show();
    }
}
