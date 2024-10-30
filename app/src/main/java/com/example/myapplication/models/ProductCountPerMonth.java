package com.example.myapplication.models;

import androidx.room.ColumnInfo;

public class ProductCountPerMonth {
    @ColumnInfo(name = "month")
    public String month;
    @ColumnInfo(name = "productCount")
    public int productCount;

    public ProductCountPerMonth(String month, int productCount) {
        this.month = month;
        this.productCount = productCount;
    }

    public String getMonth() {
        return month;
    }

    public int getProductCount() {
        return productCount;
    }
}

