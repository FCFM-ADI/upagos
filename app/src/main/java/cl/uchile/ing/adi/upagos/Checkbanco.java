package cl.uchile.ing.adi.upagos;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

class Checkbanco extends AsyncTask<Boolean, Void, String> {
    private static String URL_BANCO = "http://www.empresas.bancochile.cl/cgi-bin/cgi_cpf?canal=BCW&tipo=2&BEN_DIAS=90&rut1=60910000&dv1=1&rut2=%s&dv2=%s&mediopago=99&externo=1";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private Context context;
    private boolean install = false;


    public Checkbanco( Context context ) {
        Log.d("log_upagos", "check banco!");
        this.context = context;
    }



    @Override
    protected String doInBackground(Boolean... install) {
        String response = "";
        HttpURLConnection urlConnection = null;

        String rut = Storage.get(context, "rut");
        String dv = Storage.get(context, "dv");
        if(TextUtils.isEmpty(rut) || TextUtils.isEmpty(dv) ) return "";

        if(install!=null&&install.length>0) this.install = install[0];
        try {
            URL_BANCO = bankURL(rut, dv);
            URL url = new URL(URL_BANCO);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader isw = new InputStreamReader(in);
            for( int d = isw.read(); d != -1; d = isw.read()) response += (char) d;

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (urlConnection != null) urlConnection.disconnect();
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        if( result.equals("") ) return;
        MainActivity ma = null;
        if(context instanceof MainActivity) ma = (MainActivity) context;
        if( result.contains("Para clientes del Banco de Chile")){
            if(ma!=null && ma.isForeground()) ma.showAlreadyClient();
            return;
        }
        if(ma!=null && this.install) ma.install();

        Storage.set(context, "last_check", DATE_FORMAT.format(new Date()));
        if(ma!=null && ma.isForeground()) ma.updateView();

        result = result.replace("\n", "").replace("\r", "").replace("  ", " ");
        String[] rs = result.split("<table cellspacing=1 cellpadding=2 width=\"100%\" border=0>");
        if( rs.length <= 1 ) return;
        result = rs[1];

        String saved = Storage.get(context, "saved");
        Storage.set(context, "upagos", result);

        if(TextUtils.isEmpty(saved)) return;
        if(result.equals( saved ) ) {
            if(ma!=null && ma.isForeground()) ma.updateView();
            return;
        }
        Storage.set(context, "last_found", DATE_FORMAT.format(new Date()));
        if(ma!=null && ma.isForeground()) ma.updateView();

        // Notificacion
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.got_change))
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(URL_BANCO));


        builder.setContentIntent( PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT ) );
        ( (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE) ).notify(1, builder.build());
    }

    public static String bankURL(String rut, String dv){
        return String.format(URL_BANCO, rut, dv);
    }
}
