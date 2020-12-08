package com.example.hw9.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public final class LoadHomePageData {

    public Map<String, List<Stock>> execute(@NonNull final Context context) {
        final Map<String, List<Stock>> map = new LinkedHashMap<>();

//        Populate favorites and portfolio
        List<Stock> portfolio = new ArrayList<>();
        List<Stock> favorites = new ArrayList<>();
        portfolio.add(new Stock("MSFT", "Microsoft", "1000.00", "10"));
        portfolio.add(new Stock("AMZN", "Amazoncom Inc", "100.00", "-10.33"));
        portfolio.add(new Stock("AAPL", "Apple", "100.56", "22.27"));
        portfolio.add(new Stock("MSFT", "Microsoft", "1000.00", "0"));
        portfolio.add(new Stock("AMZN", "Amazoncom Inc", "100.00", "10.33"));
        portfolio.add(new Stock("AAPL", "Apple", "100.56", "22.27"));

        favorites.add(new Stock("TSLA", "Tesla", "345.30", "42.42"));
        favorites.add(new Stock("ADBE", "Adobe Inc.", "1000.00", "10"));
        favorites.add(new Stock("GOOG", "Google", "1000.00", "10"));
        favorites.add(new Stock("TSLA", "Tesla", "345.30", "42.42"));
        favorites.add(new Stock("ADBE", "Adobe Inc.", "1000.00", "10"));
        favorites.add(new Stock("GOOG", "Google", "1000.00", "10"));
        map.put("PORTFOLIO", portfolio);
        map.put("FAVORITES", favorites);
        return map;
    }
}
