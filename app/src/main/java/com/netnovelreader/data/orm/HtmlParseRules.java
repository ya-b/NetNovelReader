package com.netnovelreader.data.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by yangbo on 18-1-27.
 */

@Entity
public class HtmlParseRules {
    @Id
    private Long id;
    @Unique
    private String hostname;
    private String catalog_rule;
    private String chapter_rule;

    //    private String charset;
//    private String cover_rule;
    @Generated(hash = 51807711)
    public HtmlParseRules(Long id, String hostname, String catalog_rule, String chapter_rule) {
        this.id = id;
        this.hostname = hostname;
        this.catalog_rule = catalog_rule;
        this.chapter_rule = chapter_rule;
    }

    @Generated(hash = 1037403369)
    public HtmlParseRules() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCatalog_rule() {
        return this.catalog_rule;
    }

    public void setCatalog_rule(String catalog_rule) {
        this.catalog_rule = catalog_rule;
    }

    public String getChapter_rule() {
        return this.chapter_rule;
    }

    public void setChapter_rule(String chapter_rule) {
        this.chapter_rule = chapter_rule;
    }
}
