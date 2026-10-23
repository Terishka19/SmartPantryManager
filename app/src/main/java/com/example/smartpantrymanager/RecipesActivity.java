package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class RecipesActivity extends Activity {
    private DatabaseHelper db;private ListView list;private TextView status;
    private EditText recipeSearch;
    private String query="";
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_recipes);
        db=new DatabaseHelper(this);list=findViewById(R.id.recipeList);status=findViewById(R.id.recipeStatus);
        Ui.navigation(this,findViewById(R.id.bottomBar));
        findViewById(R.id.addFromRecipes).setOnClickListener(v->startActivity(new Intent(this,EditIngredientActivity.class)));
        recipeSearch=findViewById(R.id.recipeSearch);
        recipeSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                query=s.toString().toLowerCase(Locale.ROOT).trim();
                displayRecipes();
            }
            public void afterTextChanged(Editable e) {}
        });
    }
    @Override protected void onResume(){super.onResume();displayRecipes();}
    private void displayRecipes(){
        List<Recipe> eligible=db.suggestedRecipes();
        List<Recipe> matches=new ArrayList<>();
        for(Recipe r:eligible) if(r.title.toLowerCase(Locale.ROOT).contains(query)) matches.add(r);
        status.setText(eligible.size()+" recipes ready to cook");
        TextView emptyMessage=findViewById(R.id.emptyRecipes);
        emptyMessage.setText(eligible.isEmpty()?"No complete recipes yet.\nAdd ingredients to unlock meals.":"No recipes match that search.\nTry another keyword.");
        findViewById(R.id.emptyRecipes).setVisibility(matches.isEmpty()?View.VISIBLE:View.GONE);
        findViewById(R.id.addFromRecipes).setVisibility(eligible.isEmpty()?View.VISIBLE:View.GONE);
        list.setAdapter(new BaseAdapter(){
            @Override public int getCount(){return matches.size();}
            @Override public Object getItem(int p){return matches.get(p);}
            @Override public long getItemId(int p){return matches.get(p).id;}
            @Override public View getView(int p,View unused,ViewGroup parent){
                Recipe r=matches.get(p);LinearLayout card=new LinearLayout(RecipesActivity.this);card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(Ui.dp(RecipesActivity.this,19),Ui.dp(RecipesActivity.this,18),Ui.dp(RecipesActivity.this,19),Ui.dp(RecipesActivity.this,17));card.setBackgroundResource(R.drawable.card);
                TextView label=Ui.label(RecipesActivity.this,"✓  READY TO COOK",Ui.GREEN,0xFFE2F3E7);card.addView(label,new LinearLayout.LayoutParams(-2,-2));
                TextView title=Ui.text(RecipesActivity.this,r.title,20,true);title.setPadding(0,Ui.dp(RecipesActivity.this,11),0,Ui.dp(RecipesActivity.this,6));card.addView(title);
                TextView sub=Ui.text(RecipesActivity.this,db.requirements(r.id).size()+" ingredients  •  Everything is in your pantry",13,false);sub.setTextColor(Ui.MUTED);card.addView(sub);
                TextView arrow=Ui.text(RecipesActivity.this,"View recipe  →",14,true);arrow.setTextColor(Ui.GREEN);arrow.setPadding(0,Ui.dp(RecipesActivity.this,13),0,0);card.addView(arrow);
                LinearLayout outer=new LinearLayout(RecipesActivity.this);outer.setPadding(Ui.dp(RecipesActivity.this,4),Ui.dp(RecipesActivity.this,5),Ui.dp(RecipesActivity.this,4),Ui.dp(RecipesActivity.this,7));outer.addView(card,new LinearLayout.LayoutParams(-1,-2));return outer;
            }
        });
        list.setOnItemClickListener((parent,view,pos,id)->{Intent i=new Intent(this,RecipeDetailActivity.class);i.putExtra("recipe_id",matches.get(pos).id);startActivity(i);});
    }
    @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
