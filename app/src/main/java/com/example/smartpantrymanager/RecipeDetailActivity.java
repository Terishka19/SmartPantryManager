package com.example.smartpantrymanager;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
public class RecipeDetailActivity extends Activity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_detail);
  DatabaseHelper db=new DatabaseHelper(this);long id=getIntent().getLongExtra("recipe_id",-1);
  Recipe r=db.recipe(id);if(r==null){db.close();finish();return;}
  ((TextView)findViewById(R.id.recipeTitle)).setText(r.title);
  ((TextView)findViewById(R.id.recipeSteps)).setText(r.steps);
  LinearLayout host=findViewById(R.id.requirements);
  for(Ingredient item:db.requirements(id)){
    TextView row=Ui.text(this,Ui.qty(item.quantity)+" "+item.unit+"  ·  "+item.name,16,false);
    row.setPadding(Ui.dp(this,15),Ui.dp(this,14),Ui.dp(this,15),Ui.dp(this,14));row.setBackgroundResource(R.drawable.card);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=Ui.dp(this,9);host.addView(row,p);
  }
  db.close();findViewById(R.id.backButton).setOnClickListener(v->finish());
 }
}
