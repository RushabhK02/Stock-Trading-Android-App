package com.example.hw9.functionalities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hw9.model.Stock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BookmarkCache implements Serializable {
    private static BookmarkCache INSTANCE = null;
    private static boolean refreshFlag = false; //false - need to refresh, true - refreshed
    private static boolean commitFlag = false;
    private HashMap<String, Stock> portfolio;
    private HashSet<String> favorites;
    private JSONObject articleData;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private double remainingAmount=0.0;

    @NonNull
    @Override
    public String toString() {
        Log.i("Portfolio hashmap", String.valueOf(portfolio));
        return portfolio.toString();
    }

    private BookmarkCache(Activity activity) {
        try {
            preferences = activity.getApplicationContext().getSharedPreferences("cache", 0);
            editor = preferences.edit();

//          Portfolio
            String obj = preferences.getString("portfolio", "");
            portfolio = new HashMap<>();
            if(obj.length()>0) {
                articleData = new JSONObject(obj);
                for (int i = 0; i < articleData.length(); i++) {
                    String key = articleData.names().getString(i);
                    Stock article = getStock(articleData.getJSONObject(key));
                    if(!key.equalsIgnoreCase("Net Worth")) portfolio.put(key, article);
                }
            }

//          Favorites
            String favs = preferences.getString("favorites", "");
            favorites = new HashSet<String>();
            String[] tickers = favs.split(",");
            if(favs.length()>0) favorites.addAll(Arrays.asList(tickers));
//            if(favorites.contains("MSFTAAPL")) favorites.remove("MSFTAAPL");

            String amt = preferences.getString("remaining", "20,000");
            remainingAmount = Double.parseDouble(amt);
            refreshFlag = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static BookmarkCache getInstance(Activity activity) {
        if (INSTANCE == null) {
            INSTANCE = new BookmarkCache(activity);
        }
        return(INSTANCE);
    }

    public double getAmount(){
        return remainingAmount;
    }
    public void updateAmount(Double amount){
        refreshFlag = false;
        commitFlag = false;
        remainingAmount = amount;
    }

    public boolean favoriteStock(Stock obj){
        return INSTANCE.favorites.contains(obj.ticker);
    }

    public boolean toggleFavorite(Stock stock){
        refreshFlag = false;
        commitFlag = false;
        if(favorites.contains(stock.ticker)) {
            INSTANCE.favorites.remove(stock.ticker);
            return false;
        } else {
            INSTANCE.favorites.add(stock.ticker);
            return true;
        }
    }

    public boolean updatePortfolio(Stock stock){
        refreshFlag = false;
        commitFlag = false;
        if(portfolio.containsKey(stock.ticker)) {
            if(stock.stocksOwned <= 0) {
                INSTANCE.portfolio.remove(stock.ticker);
                return false;
            } else {
                INSTANCE.portfolio.put(stock.ticker, stock);
                return true;
            }
        } else {
            INSTANCE.portfolio.put(stock.ticker, stock);
            return true;
        }
    }

    public boolean commitChanges(){
        if(commitFlag) return true;

        INSTANCE.articleData = new JSONObject();

        for(String id : INSTANCE.portfolio.keySet()) {
            JSONObject json = new JSONObject();
            Stock stock = INSTANCE.portfolio.get(id);
            if(stock.ticker.equalsIgnoreCase("Net Worth")) continue;
            try {
                json.put("ticker", stock.ticker);
                json.put("shares", stock.stocksOwned);
                json.put("buyPrice", stock.buyValue);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            try {
                INSTANCE.articleData.put(id, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("portfolio", INSTANCE.articleData.toString());
//        editor.apply();
        String favs = "";
        for(String ticker:favorites) favs += ticker + ",";
        if(favs.length()>0) favs = favs.substring(0, favs.length()-1);
        editor.putString("favorites", favs);

        editor.putString("remaining", String.valueOf(remainingAmount));
        editor.commit();
        commitFlag = true;
        return true;
    }

    public List<Stock> getPortfolio() {
        return new ArrayList<Stock>(portfolio.values());
    }

    public Stock getPortfolioStock(String ticker) {
        return portfolio.getOrDefault(ticker, null);
    }

    public List<String> getFavorites() {
        List<String> favs = new ArrayList<>();
        favs.addAll(favorites);
        return favs;
    }

    public void refresh() {
        if(refreshFlag) return;
        try {
//          Portfolio
            String obj = preferences.getString("portfolio", "");
            portfolio = new HashMap<>();
            if(obj.length()>0) {
                articleData = new JSONObject(obj);

                for (int i = 0; i < articleData.names().length(); i++) {
                    String key = articleData.names().getString(i);
                    Stock article = getStock(articleData.getJSONObject(key));
                    if(!key.equalsIgnoreCase("Net Worth")) portfolio.put(key, article);
                }
            }

//          Favorites
            String favs = preferences.getString("favorites", "");
            favorites = new HashSet<String>();
            String[] tickers = favs.split(",");
            if(favs.length()>0) favorites.addAll(Arrays.asList(tickers));

//          amount
            String amt = preferences.getString("remaining", "20000");
            remainingAmount = Double.parseDouble(amt);

            refreshFlag = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Stock getStock(JSONObject object){
        try {
        return new Stock(object.getString("ticker"), object.optDouble("shares", 0.0), object.optDouble("buyPrice", 0.0));
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
