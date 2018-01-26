package com.netnovelreader.data.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by yangbo on 18-1-27.
 */

@Entity
public class BookShelf {
    @Id
    private Long id;
    @Unique
    private String bookname;
    private String record;
    private String catalog_url;
    private String latest_chapter;
    private String book_dir;

    @Generated(hash = 1286750310)
    public BookShelf(Long id, String bookname, String record, String catalog_url,
                     String latest_chapter, String book_dir) {
        this.id = id;
        this.bookname = bookname;
        this.record = record;
        this.catalog_url = catalog_url;
        this.latest_chapter = latest_chapter;
        this.book_dir = book_dir;
    }

    @Generated(hash = 547688644)
    public BookShelf() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getCatalog_url() {
        return catalog_url;
    }

    public void setCatalog_url(String catalog_url) {
        this.catalog_url = catalog_url;
    }

    public String getLatest_chapter() {
        return latest_chapter;
    }

    public void setLatest_chapter(String latest_chapter) {
        this.latest_chapter = latest_chapter;
    }

    public String getBook_dir() {
        return book_dir;
    }

    public void setBook_dir(String book_dir) {
        this.book_dir = book_dir;
    }
}
