package cl.uchile.ing.adi.upagos;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class checkBanco extends AsyncTask<String, Void, String> {
    private String url_banco;
    private Context context;

    public checkBanco( Context context ) {
        Log.d("log_upagos", "check banco!");
        this.context = context;
    }

    @Override
    protected String doInBackground(String... data) {
        String response = "";
        HttpURLConnection urlConnection = null;

        String rut = context.getSharedPreferences("upagos", Context.MODE_PRIVATE).getString("rut", "");
        String dv = context.getSharedPreferences("upagos", Context.MODE_PRIVATE).getString("dv", "");
        if( rut.equals("") || dv.equals("") ) return "";

        try {
            url_banco = "http://www.empresas.bancochile.cl/cgi-bin/cgi_cpf?canal=BCW&tipo=2&BEN_DIAS=90&rut1=60910000&dv1=1&rut2="+rut+"&dv2="+dv+"&mediopago=99&externo=1";
            URL url = new URL(url_banco);
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

        result = result.replace("\n", "").replace("\r", "").replace("  ", " ");
        String[] rs = result.split("<table cellspacing=1 cellpadding=2 width=\"100%\" border=0>");
        if( rs.length <= 1 ) return;
        result = rs[1];

        String saved = context.getSharedPreferences("upagos", Context.MODE_PRIVATE).getString("saved", "");
        context.getSharedPreferences("upagos", Context.MODE_PRIVATE).edit().putString("saved", result).commit();

        if( result.equals( saved ) ) return;

        // Notificacion
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_upagos)
                        .setContentTitle("U-Pagos")
                        .setContentText("Algo cambió en la página del banco")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(url_banco));

        builder.setContentIntent( PendingIntent.getActivity( context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT ) );
        ( (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE) ).notify(1, builder.build());
    }
}
