package com.nanda.bookslibrary.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GooglsBooksResponse {

    @SerializedName("kind")
    private String kind;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("items")
    private List<BooksModel> booksModelList;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<BooksModel> getBooksModelList() {
        return booksModelList;
    }

    public void setBooksModelList(List<BooksModel> booksModelList) {
        this.booksModelList = booksModelList;
    }
}
