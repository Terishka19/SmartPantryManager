package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/** Separate local database: the existing pantry.db schema and recipes are untouched. */
public class ShoppingDb extends SQLiteOpenHelper {
    public static class Entry {
        public final long id;
        public final String name, unit;
        public final double quantity;
        public final boolean bought;
        Entry(long id, String name, double quantity, String unit, boolean bought) {
            this.id=id; this.name=name; this.quantity=quantity; this.unit=unit; this.bought=bought;
        }
    }
    public ShoppingDb(Context context){ super(context,"shopping.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE shopping (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, qty REAL NOT NULL, unit TEXT NOT NULL, bought INTEGER NOT NULL DEFAULT 0)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { }
    public List<Entry> all() {
        List<Entry> result=new ArrayList<>();
        try (Cursor c=getReadableDatabase().rawQuery("SELECT id,name,qty,unit,bought FROM shopping ORDER BY bought,name COLLATE NOCASE",null)) {
            while(c.moveToNext())result.add(new Entry(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getInt(4)==1));
        }
        return result;
    }
    /** Merge equivalent units so repeated clicks do not make duplicate list entries. */
    public void add(String name,double qty,String unit) {
        String normal=IngredientMath.normalize(name);
        String group=IngredientMath.group(unit);
        double base=IngredientMath.base(qty,unit);
        SQLiteDatabase db=getWritableDatabase();
        for(Entry e:all()) {
            if(!e.bought && IngredientMath.normalize(e.name).equals(normal) && IngredientMath.group(e.unit).equals(group)) {
                ContentValues values=new ContentValues();
                double total=IngredientMath.base(e.quantity,e.unit)+base;
                // Convert back into the existing row's unit.
                double factor=IngredientMath.base(1,e.unit);
                values.put("qty",total/factor);
                db.update("shopping",values,"id=?",new String[]{String.valueOf(e.id)});
                return;
            }
        }
        ContentValues v=new ContentValues(); v.put("name",name);v.put("qty",qty);v.put("unit",unit);v.put("bought",0);
        db.insertOrThrow("shopping",null,v);
    }
    public void setBought(long id,boolean bought) {
        ContentValues v=new ContentValues();v.put("bought",bought?1:0);
        getWritableDatabase().update("shopping",v,"id=?",new String[]{String.valueOf(id)});
    }
    public void delete(long id){getWritableDatabase().delete("shopping","id=?",new String[]{String.valueOf(id)});}
    public void clearBought(){getWritableDatabase().delete("shopping","bought=1",null);}
}
