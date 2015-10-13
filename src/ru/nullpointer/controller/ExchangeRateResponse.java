package ru.nullpointer.controller;

public class ExchangeRateResponse {

    private String code;
    private String rate;
    private String date;

    public ExchangeRateResponse(String code, String rate, String date) {
        this.code = code;
        this.rate = rate;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public String getRate() {
        return rate;
    }

    public String getDate() {
        return date;
    }
}
