package cl.uchile.ing.adi.upagos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        new Checkbanco(context).execute(false);
    }
}