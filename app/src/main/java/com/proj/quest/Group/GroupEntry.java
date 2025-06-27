package com.proj.quest.Group;

public class GroupEntry {
    private String name;
    private int score;

    public GroupEntry(String name, int score){
        this.name = name;
        this.score = score;
    }

    public String getName(){ return name; }
    public int getScore(){ return score; }
}
