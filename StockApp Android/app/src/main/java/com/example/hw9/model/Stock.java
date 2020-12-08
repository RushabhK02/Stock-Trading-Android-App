package com.example.hw9.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

final public class Stock implements Serializable {
    public String ticker;
    public String name;
    public String price;
    public String changePrice;
    public String description;
    public String currentPrice;
    public String openPrice;
    public String high;
    public String low;
    public String mid;
    public String volume;
    public String bidPrice;
    public List<ArticleItem.Article> articles;
    public double stocksOwned = 0;
    public double buyValue = 0;

    public Stock(@NonNull final String ticker, @NonNull final String name, @NonNull final String price, @NonNull final String change) {
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.changePrice = change;
    }

    public Stock(@NonNull final String ticker, @NonNull final double shares, @NonNull final double buyVal) {
        this.ticker = ticker;
        this.stocksOwned = shares;
        this.buyValue = buyVal;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getChangePrice() {
        return changePrice;
    }
}
