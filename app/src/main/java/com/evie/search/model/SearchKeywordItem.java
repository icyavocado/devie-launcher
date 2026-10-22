package com.evie.search.model;

/* JADX INFO: loaded from: classes.dex */
public class SearchKeywordItem {
    String keyword;
    String keywordType;

    public SearchKeywordItem(String str, String str2) {
        this.keyword = str;
        this.keywordType = str2;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SearchKeywordItem)) {
            return false;
        }
        SearchKeywordItem searchKeywordItem = (SearchKeywordItem) obj;
        return this.keyword.equals(searchKeywordItem.keyword) && this.keywordType.equals(searchKeywordItem.keywordType);
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String getKeywordType() {
        return this.keywordType;
    }

    public int hashCode() {
        return (this.keyword.hashCode() * 31) + this.keywordType.hashCode();
    }
}
