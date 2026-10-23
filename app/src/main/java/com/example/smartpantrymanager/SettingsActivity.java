package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Premium preferences page; preserves the existing 'expiry_alerts' preference. */
public class SettingsActivity extends Activity {
    private CheckBox alerts;
    private TextView summary;
    private DatabaseHelper db;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_settings);
        db=new DatabaseHelper(this);
        Ui.navigation(this,findViewById(R.id.bottomBar));
        alerts=findViewById(R.id.alertCheck);
        summary=findViewById(R.id.expirySummary);
        SharedPreferences prefs=getSharedPreferences("settings",MODE_PRIVATE);
        alerts.setChecked(prefs.getBoolean("expiry_alerts",true));
        alerts.setOnCheckedChangeListener((button,checked)->{
            prefs.edit().putBoolean("expiry_alerts",checked).apply();
            summary.setVisibility(checked?View.VISIBLE:View.GONE);
        });
        findViewById(R.id.settingsAlmost).setOnClickListener(v->startActivity(new Intent(this,AlmostThereActivity.class)));
        findViewById(R.id.settingsShopping).setOnClickListener(v->startActivity(new Intent(this,ShoppingListActivity.class)));
        ((TextView)findViewById(R.id.alertNote)).setText("Shows the on-screen expiry summary. Background notifications are not enabled.");
    }
    @Override protected void onResume(){super.onResume();refresh();}
    private void refresh(){
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
        Calendar calendar=Calendar.getInstance();calendar.add(Calendar.DAY_OF_YEAR,3);
        String threshold=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(calendar.getTime());
        int expired=0,soon=0,total=0;
        for(Ingredient item:db.pantry()){
            total++;
            if(item.expiry.isEmpty())continue;
            if(item.expiry.compareTo(today)<0)expired++;
            else if(item.expiry.compareTo(threshold)<=0)soon++;
        }
        summary.setText("Pantry items: "+total+"    •    Expired: "+expired+"    •    Expiring within 3 days: "+soon);
        summary.setVisibility(alerts.isChecked()?View.VISIBLE:View.GONE);
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
