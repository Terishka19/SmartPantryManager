package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.util.Log;

/** On-device persistence: no account, Internet or location permission required. */
public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) { super(context,"pantry.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, qty REAL NOT NULL CHECK(qty>0), unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL UNIQUE, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, name TEXT NOT NULL, qty REAL NOT NULL CHECK(qty>0), unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seed(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { /* Version 1; add migrations when upgrading. */ }
    public List<Ingredient> pantry() {
        ArrayList<Ingredient> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name,qty,unit,COALESCE(expiry,'') FROM pantry_items ORDER BY name COLLATE NOCASE",null)) {
            while(c.moveToNext()) result.add(new Ingredient(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4)));
        } return result;
    }
    public Ingredient getIngredient(long id) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name,qty,unit,COALESCE(expiry,'') FROM pantry_items WHERE id=?",new String[]{String.valueOf(id)})) {
            if(c.moveToFirst()) return new Ingredient(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4));
        } return null;
    }
    public long saveIngredient(long id,String name,double qty,String unit,String expiry) {
        ContentValues v=new ContentValues();v.put("name",name);v.put("qty",qty);v.put("unit",unit);
        if(expiry.isEmpty()) v.putNull("expiry"); else v.put("expiry",expiry);
        SQLiteDatabase db=getWritableDatabase();
        if(id>0) { db.update("pantry_items",v,"id=?",new String[]{String.valueOf(id)});return id; }
        return db.insertOrThrow("pantry_items",null,v);
    }
    public void deleteIngredient(long id) { getWritableDatabase().delete("pantry_items","id=?",new String[]{String.valueOf(id)}); }
    public List<Recipe> allRecipes() {
        ArrayList<Recipe> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,title,steps FROM recipes ORDER BY title",null)) {
            while(c.moveToNext()) result.add(new Recipe(c.getLong(0),c.getString(1),c.getString(2)));
        } return result;
    }
    public Recipe recipe(long id) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,title,steps FROM recipes WHERE id=?",new String[]{String.valueOf(id)})) {
            if(c.moveToFirst()) return new Recipe(c.getLong(0),c.getString(1),c.getString(2));
        } return null;
    }
    public List<Ingredient> requirements(long id) {
        ArrayList<Ingredient> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name,qty,unit FROM recipe_ingredients WHERE recipe_id=? ORDER BY id",new String[]{String.valueOf(id)})) {
            while(c.moveToNext()) result.add(new Ingredient(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),""));
        }return result;
    }
    /** STRICT: every requirement must be satisfiable by non-expired stock in compatible units. */
    public List<Recipe> suggestedRecipes() {
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
        // Combine matching pantry rows after converting compatible units to their base unit.
        Map<String,Double> inventory=new HashMap<>();
        for(Ingredient item:pantry()) {
            Log.d("PANTRY_DEBUG",
                    "Name: " + item.name
                            + " | Normalized: " + IngredientMath.normalize(item.name)
                            + " | Quantity: " + item.quantity
                            + " | Unit: " + item.unit
                            + " | Expiry: " + item.expiry);
            if(!item.expiry.isEmpty() && item.expiry.compareTo(today)<0) continue;
            String key=IngredientMath.normalize(item.name)+"|"+IngredientMath.group(item.unit);
            inventory.put(key,inventory.containsKey(key)?inventory.get(key)+IngredientMath.base(item.quantity,item.unit):IngredientMath.base(item.quantity,item.unit));
        }
        List<Recipe> result=new ArrayList<>();
        for(Recipe recipe:allRecipes()) {
            Map<String,Double> needs=new HashMap<>();
            for(Ingredient required:requirements(recipe.id)) {
                String key=IngredientMath.normalize(required.name)+"|"+IngredientMath.group(required.unit);
                double qty=IngredientMath.base(required.quantity,required.unit);
                needs.put(key,needs.containsKey(key)?needs.get(key)+qty:qty);
            }
            boolean eligible=!needs.isEmpty();
            for(Map.Entry<String,Double> need:needs.entrySet()) {
                Log.d("PANTRY_DEBUG",
                        "Recipe: " + recipe.title
                                + " | Required: " + need.getKey()
                                + " | Required quantity: " + need.getValue()
                                + " | Available quantity: "
                                + inventory.getOrDefault(need.getKey(), 0.0));
                if(inventory.getOrDefault(need.getKey(),0.0)+0.000001<need.getValue()) {eligible=false;break;}
            }
            if(eligible) result.add(recipe);
        }
        return result;
    }
    private void seed(SQLiteDatabase db) {
        // ingredient token format: ingredient name | quantity | unit (semicolon separated).
        add(db,"Tomato Omelette","Beat eggs, chop tomato and cook together in a lightly oiled pan until set.","egg|2|pcs;tomato|1|pcs;oil|10|ml");
        add(db,"Cheese Omelette","Beat eggs, stir in cheese, then cook gently until set.","egg|2|pcs;cheese|40|g;oil|10|ml");
        add(db,"Scrambled Eggs","Beat eggs with milk and stir gently in a pan with butter.","egg|2|pcs;milk|30|ml;butter|10|g");
        add(db,"French Toast","Whisk egg with milk, dip bread and toast on both sides using butter.","egg|1|pcs;milk|50|ml;bread|2|pcs;butter|10|g");
        add(db,"Cheese Toastie","Place cheese between bread slices and toast until golden.","bread|2|pcs;cheese|40|g;butter|10|g");
        add(db,"Peanut Butter Toast","Toast bread and spread peanut butter evenly.","bread|2|pcs;peanut butter|30|g");
        add(db,"Banana Oats","Heat oats with milk and top with sliced banana.","oats|60|g;milk|200|ml;banana|1|pcs");
        add(db,"Apple Cinnamon Oats","Heat oats and milk, add chopped apple and cinnamon.","oats|60|g;milk|200|ml;apple|1|pcs;cinnamon|1|tsp");
        add(db,"Yoghurt Banana Bowl","Slice banana into yoghurt and serve.","yoghurt|150|g;banana|1|pcs");
        add(db,"Tomato Salad","Slice tomatoes and onion; dress with olive oil.","tomato|2|pcs;onion|1|pcs;olive oil|15|ml");
        add(db,"Carrot Salad","Grate carrots and mix with lemon juice and olive oil.","carrot|2|pcs;lemon juice|15|ml;olive oil|10|ml");
        add(db,"Simple Rice","Rinse rice and simmer in water until tender.","rice|150|g;water|300|ml");
        add(db,"Buttered Rice","Cook rice in water and finish with butter.","rice|150|g;water|300|ml;butter|15|g");
        add(db,"Garlic Pasta","Boil pasta, then toss with warm olive oil and garlic.","pasta|150|g;water|600|ml;olive oil|15|ml;garlic clove|2|pcs");
        add(db,"Tomato Pasta","Boil pasta and toss with chopped cooked tomato and oil.","pasta|150|g;water|600|ml;tomato|2|pcs;oil|15|ml");
        add(db,"Mashed Potatoes","Boil peeled potatoes then mash with milk and butter.","potato|3|pcs;milk|60|ml;butter|20|g");
        add(db,"Pan-Fried Potatoes","Slice potatoes and cook in oil until crisp and cooked through.","potato|3|pcs;oil|25|ml");
        add(db,"Egg Fried Rice","Cook rice, then stir-fry with egg and oil.","rice|150|g;water|300|ml;egg|1|pcs;oil|15|ml");
        add(db,"Banana Pancakes","Mash banana with eggs and flour, then cook small pancakes in oil.","banana|1|pcs;egg|2|pcs;flour|70|g;oil|10|ml");
        add(db,"Bean Wrap","Warm tortilla, fill with beans and diced tomato, then roll.","tortilla|1|pcs;beans|120|g;tomato|1|pcs");
    }
    private void add(SQLiteDatabase db,String title,String steps,String ingredients) {
        ContentValues recipe=new ContentValues(); recipe.put("title",title);recipe.put("steps",steps);
        long id=db.insertOrThrow("recipes",null,recipe);
        for(String token:ingredients.split(";")) {
            String[] p=token.split("\\|");
            ContentValues row=new ContentValues();row.put("recipe_id",id);row.put("name",p[0]);row.put("qty",Double.parseDouble(p[1]));row.put("unit",p[2]);
            db.insertOrThrow("recipe_ingredients",null,row);
        }
    }
}
