package com.example.smartpantrymanager;

public class Recipe {
    public final long id;
    public final String title, steps;
    public Recipe(long id, String title, String steps) { this.id=id; this.title=title; this.steps=steps; }
}
