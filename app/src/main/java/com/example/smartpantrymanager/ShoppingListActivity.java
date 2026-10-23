package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShoppingListActivity extends Activity {
    private ShoppingDb db;
    private LinearLayout rows;
    private TextView summary;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.activity_shopping_list);
        db=new ShoppingDb(this);
        rows=findViewById(R.id.shoppingRows);
        summary=findViewById(R.id.shoppingSummary);
        findViewById(R.id.clearPurchased).setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Clear purchased items?").setMessage("Only checked shopping items will be deleted.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Clear",(d,which)->{db.clearBought();refresh();}).show());
        findViewById(R.id.goAlmost).setOnClickListener(v->startActivity(new Intent(this,AlmostThereActivity.class)));
        Ui.navigation(this,findViewById(R.id.bottomBar));
    }
    @Override protected void onResume(){super.onResume();refresh();}
    private void refresh(){
        rows.removeAllViews();
        java.util.List<ShoppingDb.Entry> all=db.all();
        int bought=0;
        for(ShoppingDb.Entry entry:all){
            if(entry.bought)bought++;
            LinearLayout line=new LinearLayout(this);line.setGravity(android.view.Gravity.CENTER_VERTICAL);
            line.setPadding(Ui.dp(this,10),Ui.dp(this,7),Ui.dp(this,10),Ui.dp(this,7));
            line.setBackground(Ui.rounded(this,0xFFFFFFFF,12));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,Ui.dp(this,10));
            rows.addView(line,lp);
            CheckBox box=new CheckBox(this);
            box.setText(entry.name+"  •  "+Ui.qty(entry.quantity)+" "+entry.unit);
            box.setTextSize(15);box.setTextColor(Ui.DARK_GREEN);box.setChecked(entry.bought);
            line.addView(box,new LinearLayout.LayoutParams(0,-2,1));
            box.setOnCheckedChangeListener((v,checked)->{db.setBought(entry.id,checked);refresh();});
            Button remove=new Button(this);remove.setText("Delete");remove.setAllCaps(false);
            line.addView(remove,new LinearLayout.LayoutParams(-2,-2));
            remove.setOnClickListener(v->{db.delete(entry.id);refresh();});
        }
        summary.setText(all.isEmpty()?"Your shopping list is empty. Browse Almost There recipes to add ingredients.":all.size()+" items  •  "+bought+" purchased");
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
