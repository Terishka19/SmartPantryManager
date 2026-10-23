package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.Locale;

public class MainActivity extends Activity {
    private DatabaseHelper db; private ListView list; private TextView status;
    private TextView pantryStats, sortButton;
    private EditText searchInput;
    private String searchText = "";
    private int sortMode = 0; // 0: name, 1: expiry, 2: quantity
    @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);
        db=new DatabaseHelper(this);list=findViewById(R.id.pantryList);status=findViewById(R.id.status);
        findViewById(R.id.addButton).setOnClickListener(v->startActivity(new Intent(this,EditIngredientActivity.class)));
        Ui.navigation(this,findViewById(R.id.bottomBar));
        pantryStats = findViewById(R.id.pantryStats);
        searchInput = findViewById(R.id.pantrySearch);
        sortButton = findViewById(R.id.sortButton);
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                searchText = s.toString().trim().toLowerCase(Locale.ROOT);
                refresh();
            }
            public void afterTextChanged(Editable e) {}
        });
        sortButton.setOnClickListener(v -> {
            sortMode = (sortMode + 1) % 3;
            sortButton.setText(sortMode == 0 ? "Sort: A–Z  ⇅" : sortMode == 1 ? "Sort: Expiry  ⇅" : "Sort: Quantity  ⇅");
            refresh();
        });
    }
    @Override protected void onResume(){super.onResume();refresh();}
    private void refresh(){
        List<Ingredient> all = db.pantry();
        List<Ingredient> items = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
        java.util.Calendar soonCal=java.util.Calendar.getInstance(); soonCal.add(java.util.Calendar.DAY_OF_YEAR,3);
        String soonDate=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(soonCal.getTime());
        int expired=0, soon=0;
        for (Ingredient ingredient : all) {
            String exp = ingredient.expiry == null ? "" : ingredient.expiry;
            if (!exp.isEmpty()) {
                if (exp.compareTo(today)<0) expired++;
                else if (exp.compareTo(soonDate)<=0) soon++;
            }
            if (ingredient.name.toLowerCase(Locale.ROOT).contains(searchText)) items.add(ingredient);
        }
        if (sortMode == 1) Collections.sort(items,(a,b)->{
            String ea=a.expiry==null||a.expiry.isEmpty()?"9999-12-31":a.expiry;
            String eb=b.expiry==null||b.expiry.isEmpty()?"9999-12-31":b.expiry;
            return ea.compareTo(eb);
        });
        if (sortMode == 2) Collections.sort(items,(a,b)->Double.compare(b.quantity,a.quantity));
        status.setText(all.size()+" ingredient"+(all.size()==1?"":"s")+" saved");
        pantryStats.setText("●  "+Math.max(0,all.size()-expired-soon)+" fresh       ◷  "+soon+" use soon       !  "+expired+" expired");
        TextView empty=findViewById(R.id.emptyPantry);empty.setVisibility(items.isEmpty()?View.VISIBLE:View.GONE);
        empty.setText(all.isEmpty()?"Your pantry is empty.\nAdd your first ingredient to begin.":"No ingredients match your search.\nTry another name.");
        list.setAdapter(new BaseAdapter(){
            @Override public int getCount(){return items.size();}
            @Override public Object getItem(int p){return items.get(p);}
            @Override public long getItemId(int p){return items.get(p).id;}
            @Override public View getView(int p,View unused,ViewGroup parent){
                Ingredient item=items.get(p);LinearLayout card=new LinearLayout(MainActivity.this);card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(Ui.dp(MainActivity.this,18),Ui.dp(MainActivity.this,17),Ui.dp(MainActivity.this,18),Ui.dp(MainActivity.this,15));card.setBackgroundResource(R.drawable.card);
                LinearLayout top=new LinearLayout(MainActivity.this);top.setGravity(Gravity.CENTER_VERTICAL);card.addView(top);
                TextView name=Ui.text(MainActivity.this,item.name,18,true);top.addView(name,new LinearLayout.LayoutParams(0,-2,1));
                String today=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
                java.util.Calendar cal=java.util.Calendar.getInstance();cal.add(java.util.Calendar.DAY_OF_YEAR,3);
                String soon=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(cal.getTime());
                boolean dated=item.expiry!=null&&!item.expiry.isEmpty();
                String state=!dated?"NO EXPIRY":item.expiry.compareTo(today)<0?"EXPIRED":item.expiry.compareTo(soon)<=0?"USE SOON":"FRESH";
                int background=state.equals("EXPIRED")?0xFFFBE1DD:state.equals("USE SOON")?0xFFFFF0CF:0xFFE2F3E7;
                int foreground=state.equals("EXPIRED")?0xFF9D3C32:state.equals("USE SOON")?0xFF8B6318:Ui.GREEN;
                TextView pill=Ui.label(MainActivity.this,state,foreground,background);top.addView(pill);
                TextView amount=Ui.text(MainActivity.this,Ui.qty(item.quantity)+" "+item.unit,15,false);amount.setTextColor(Ui.MUTED);
                amount.setPadding(0,Ui.dp(MainActivity.this,7),0,dated?Ui.dp(MainActivity.this,5):Ui.dp(MainActivity.this,15));card.addView(amount);
                if(dated){TextView date=Ui.text(MainActivity.this,"Use by  "+item.expiry,13,false);date.setTextColor(Ui.MUTED);date.setPadding(0,0,0,Ui.dp(MainActivity.this,14));card.addView(date);}
                View line=new View(MainActivity.this);line.setBackgroundColor(0xFFE9EEE9);card.addView(line,new LinearLayout.LayoutParams(-1,Ui.dp(MainActivity.this,1)));
                LinearLayout actions=new LinearLayout(MainActivity.this);actions.setPadding(0,Ui.dp(MainActivity.this,12),0,0);card.addView(actions);
                TextView edit=action("✎  Edit",Ui.GREEN);TextView delete=action("Delete",0xFFAF4B41);
                LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,Ui.dp(MainActivity.this,38),1);ep.setMargins(0,0,Ui.dp(MainActivity.this,8),0);actions.addView(edit,ep);
                actions.addView(delete,new LinearLayout.LayoutParams(0,Ui.dp(MainActivity.this,38),1));
                edit.setOnClickListener(v->{Intent i=new Intent(MainActivity.this,EditIngredientActivity.class);i.putExtra("id",item.id);startActivity(i);});
                delete.setOnClickListener(v->new AlertDialog.Builder(MainActivity.this).setTitle("Delete ingredient?")
                    .setMessage("Remove "+item.name+" from your pantry? This cannot be undone.")
                    .setNegativeButton("Cancel",null).setPositiveButton("Delete",(dialog,which)->{db.deleteIngredient(item.id);refresh();}).show());
                LinearLayout outer=new LinearLayout(MainActivity.this);outer.setPadding(Ui.dp(MainActivity.this,4),Ui.dp(MainActivity.this,5),Ui.dp(MainActivity.this,4),Ui.dp(MainActivity.this,7));
                outer.addView(card,new LinearLayout.LayoutParams(-1,-2));return outer;
            }
        });
    }
    private TextView action(String title,int color){TextView t=Ui.text(this,title,14,true);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setBackground(Ui.background(0xFFEDF3ED,12,this));return t;}
    @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
