package com.example.hw9.model;

public class AutoCompleteItem {
    private String ticker;
    private String name;

    public AutoCompleteItem(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
