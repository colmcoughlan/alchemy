package com.colmcoughlan.colm.alchemy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StaticState {
    private StaticState(){

    }

    private static String category = "All";
    private static List<Charity> charityList = new ArrayList<>();
    private static long lastRefresh = 0;
    private static final long minRefreshIntervalMillis = 60 * 60 * 1000; // one hour

    public static String getCategory(){
        return category;
    }

    public static void setCurrentCategory(String newCategory){
        category = newCategory;
    }

    public static List<Charity> getCharities(){
        return new ArrayList<>(charityList);
    }

    public static void setCharities(List<Charity> newCharities){
        lastRefresh = System.currentTimeMillis();
        charityList = newCharities;
    }

    public static boolean shouldRefreshCharities(){
        return charityList == null || charityList.isEmpty()
                || (System.currentTimeMillis() - lastRefresh) > minRefreshIntervalMillis;
    }
}
