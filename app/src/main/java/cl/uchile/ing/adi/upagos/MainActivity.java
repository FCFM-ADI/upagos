package cl.uchile.ing.adi.upagos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String rut;
    private String dv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        final EditText edittext = (EditText) findViewById(R.id.edittext);
        Button button = (Button) findViewById(R.id.button);

        rut = getSharedPreferences("upagos", MODE_PRIVATE).getString("rut", "");
        dv = getSharedPreferences("upagos", MODE_PRIVATE).getString("dv", "");

        if( ! rut.equals("") ) edittext.setText( rut+"-"+dv );

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String r = String.valueOf(edittext.getText());
                String[] rs = r.split("-");
                if( rs.length != 2 ) return;
                rut = rs[0];
                dv = rs[1];
                edittext.setText( rut+"-"+dv);

                getSharedPreferences("upagos", MODE_PRIVATE).edit()
                    .putString("rut", rut)
                    .putString("dv", dv)
                    .commit();

                instalarAlarm();
                new checkBanco( MainActivity.this ).execute();
                Toast.makeText(MainActivity.this, "U-Pagos Instalado!", Toast.LENGTH_SHORT).show();
                finish();
           }
        });
    }

    private void instalarAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, (int) Math.floor( 60.0*Math.random() ) ); // minuto al azar
        am.setInexactRepeating( AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, p );
    }


}
