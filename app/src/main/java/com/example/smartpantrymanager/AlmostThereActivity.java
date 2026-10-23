package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Separate suggestions: never changes strict Ready to Cook eligibility. */
public class AlmostThereActivity extends Activity {
    private DatabaseHelper pantryDb;
    private ShoppingDb shoppingDb;
    private LinearLayout results;
    private TextView summary;
    static class Missing {
        String name,unit;
        double amount;
        Missing(String name,double amount,String unit){this.name=name;this.amount=amount;this.unit=unit;}
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_almost_there);
        pantryDb=new DatabaseHelper(this);
        shoppingDb=new ShoppingDb(this);
        results=findViewById(R.id.almostResults);
        summary=findViewById(R.id.almostSummary);
        findViewById(R.id.openShopping).setOnClickListener(v->startActivity(new Intent(this,ShoppingListActivity.class)));
        Ui.navigation(this,findViewById(R.id.bottomBar));
    }
    @Override protected void onResume(){super.onResume();refresh();}
    private void refresh() {
        results.removeAllViews();
        Map<String,Double> stock=new HashMap<>();
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
        for(Ingredient item:pantryDb.pantry()) {
            if(!item.expiry.isEmpty() && item.expiry.compareTo(today)<0)continue;
            String key=IngredientMath.normalize(item.name)+"|"+IngredientMath.group(item.unit);
            stock.put(key,stock.getOrDefault(key,0.0)+IngredientMath.base(item.quantity,item.unit));
        }
        int visible=0;
        for(Recipe recipe:pantryDb.allRecipes()) {
            // Aggregate requirements before comparing (handles duplicate ingredient rows).
            Map<String,Double> needs=new HashMap<>();
            Map<String,Ingredient> exemplar=new HashMap<>();
            for(Ingredient need:pantryDb.requirements(recipe.id)) {
                String key=IngredientMath.normalize(need.name)+"|"+IngredientMath.group(need.unit);
                exemplar.put(key,need);
                needs.put(key,needs.getOrDefault(key,0.0)+IngredientMath.base(need.quantity,need.unit));
            }
            List<Missing> missing=new ArrayList<>();
            for(Map.Entry<String,Double> entry:needs.entrySet()) {
                double shortfall=entry.getValue()-stock.getOrDefault(entry.getKey(),0.0);
                if(shortfall>0.000001) {
                    Ingredient example=exemplar.get(entry.getKey());
                    double multiplier=IngredientMath.base(1,example.unit);
                    missing.add(new Missing(example.name,shortfall/multiplier,example.unit));
                }
            }
            if(missing.size()<1 || missing.size()>2)continue;
            visible++;
            addRecipeCard(recipe,missing);
        }
        summary.setText(visible==0?"No almost-ready recipes. Try adding more pantry ingredients.":visible+" recipes need just 1 or 2 ingredients");
    }
    private void addRecipeCard(Recipe recipe,List<Missing> missing){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this,18),Ui.dp(this,16),Ui.dp(this,18),Ui.dp(this,16));
        card.setBackground(Ui.rounded(this,0xFFFFFFFF,16));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
        cp.setMargins(0,0,0,Ui.dp(this,12));
        results.addView(card,cp);
        TextView badge=Ui.label(this,"ALMOST THERE  •  "+missing.size()+" missing",0xFF93631F,0xFFFFF0D7);
        card.addView(badge,new LinearLayout.LayoutParams(-2,-2));
        TextView title=Ui.text(this,recipe.title,20,true);
        card.addView(title);
        for(Missing need:missing) {
            TextView line=Ui.text(this,"Need "+Ui.qty(need.amount)+" "+need.unit+" "+need.name,14,false);
            line.setTextColor(Ui.MUTED);
            card.addView(line);
        }
        Button add=new Button(this);
        add.setText("Add missing items to shopping list");add.setAllCaps(false);
        add.setTextColor(0xFFFFFFFF);add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.GREEN));
        card.addView(add);
        add.setOnClickListener(v->{
            for(Missing m:missing)shoppingDb.add(m.name,m.amount,m.unit);
            Toast.makeText(this,"Items added to shopping list",Toast.LENGTH_SHORT).show();
        });
    }
    @Override protected void onDestroy(){pantryDb.close();shoppingDb.close();super.onDestroy();}
}
