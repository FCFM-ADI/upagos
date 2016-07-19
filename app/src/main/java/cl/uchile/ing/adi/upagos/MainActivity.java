package cl.uchile.ing.adi.upagos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textRut;
    private TextView textLastCheck;
    private TextView textLastFound;
    private TextView textDV;
    private TextView textExplanation;
    private Button buttonCheck;
    private SwipeRefreshLayout swipeLayout;

    private String rut;
    private String dv;
    private boolean isForeground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe);
        textRut                 = (EditText) findViewById(R.id.edittext);
        buttonCheck             = (Button) findViewById(R.id.button);
        textLastCheck           = (TextView) findViewById(R.id.last_check);
        textLastFound           = (TextView) findViewById(R.id.last_found);
        textDV                  = (TextView) findViewById(R.id.dv);
        textExplanation         = (TextView) findViewById(R.id.text_explanation);

        rut = Storage.get(this, "rut");
        dv = Storage.get(this, "dv");

        if( !TextUtils.isEmpty(rut) ) {
            textRut.setText( rut );
            textDV.setText( "- "+Rut.getDV(rut));
            textExplanation.setVisibility(View.VISIBLE);
        }
        else{
            buttonCheck.setText(R.string.button_register);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            textExplanation.setVisibility(View.GONE);
        }
        textRut.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String r = s.toString();
                char cdv = Rut.getDV(r);
                if(cdv!='\0') {
                    dv = new String(cdv+"");
                    textDV.setText(dv);
                }
                if(r.equals(rut)) {
                    textExplanation.setVisibility(View.VISIBLE);
                    buttonCheck.setText(R.string.button_verify);
                }
                else {
                    textExplanation.setVisibility(View.GONE);
                    buttonCheck.setText(R.string.button_register);
                }
            }
        });

        textRut.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    handleAction();
                    return true;
                }
                return false;
            }
        });
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                handleAction();
            }
        });
    }


    @Override protected void onResume() {
        super.onResume();
        isForeground = true;
        updateView();
    }

    @Override protected void onPause() {
        super.onPause();
        isForeground = false;
    }

    private void installAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, (int) Math.floor( 60.0*Math.random() ) ); // minuto al azar
        am.setInexactRepeating( AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, p );
    }

    private void handleAction(){
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            String r = String.valueOf(textRut.getText());
            String d = String.valueOf(textDV.getText());

            if(TextUtils.isEmpty(r)) {
                Messages.snackbar(R.string.bad_rut, findViewById(R.id.main_activity_layout), getApplicationContext());
                return;
            }
            String savedRut = Storage.get(MainActivity.this, "rut");

            boolean install = false;
            if(!TextUtils.isEmpty(savedRut)) { //There's a previous install
                if(!savedRut.equals(r)){ //New rut to save
                    rut = r;
                    dv = d;
                    Storage.set(MainActivity.this, "rut", rut);
                    Storage.set(MainActivity.this, "dv", dv);
                    Storage.rm(getApplicationContext(), "last_check");
                    Storage.rm(getApplicationContext(),"last_found");
                    Storage.rm(getApplicationContext(),"saved");
                    updateView();
                    Messages.snackbar(R.string.installed, findViewById(R.id.main_activity_layout), getApplicationContext());
                }
                else{
                    openBankURL();
                }
            }
            else{//Never saved a RUT -> New install
                rut = r;
                dv = d;
                Storage.set(MainActivity.this, "rut", rut);
                Storage.set(MainActivity.this, "dv", dv);
                install = true;
            }
        setRefresh(true);
        new Checkbanco( MainActivity.this ).execute(install);
    }

    private void openBankURL(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(Checkbanco.bankURL(rut,dv)));
        startActivity(i);
    }
    public void install(){
        buttonCheck.setText(R.string.button_verify);
        textExplanation.setVisibility(View.VISIBLE);
        updateView();
        installAlarm();
        Messages.snackbar(R.string.installed, findViewById(R.id.main_activity_layout), getApplicationContext());
    }

    public boolean isForeground(){return this.isForeground;}
    public void showAlreadyClient(){
        String msg = String.format(Locale.getDefault(), getString(R.string.msg_alert_bank_customer), rut+"-"+dv);
        Storage.rm(getApplicationContext(), "rut");
        Storage.rm(getApplicationContext(), "dv");
        updateView();
        Messages.alert(msg, getString(R.string.msg_customer_visit_bank), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://ww3.bancochile.cl/wps/wcm/connect/personas/portal/beneficios/campanas/campanas-banco-en-linea/dav"));
                startActivity(i);
            }
        }, this);
    }

    public void updateView() {
        textRut.setText(Storage.get(getApplicationContext(), "rut"));
        textDV.setText(Storage.get(getApplicationContext(), "dv", getString(R.string.hint_dv)));
        textLastCheck.setText(Storage.get(MainActivity.this, "last_check", getString(R.string.no_last_check)));
        textLastFound.setText(Storage.get(MainActivity.this, "last_found", getString(R.string.no_last_found)));
        setRefresh(false);
    }

    public void setRefresh(final boolean refresh){
        swipeLayout.post(new Runnable() {
            @Override public void run() { swipeLayout.setRefreshing(refresh); }
        });
    }
}
