package com.netnovelreader.data.orm;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by yangbo on 18-1-27.
 */

@Entity
public class SearchRules {
    @Transient
    public static String SEARCH_NAME = "searchname";
    @Id
    private Long id;
    @Unique
    private String hostname;
    //搜索的网址,书名用searchname替代，http://hello.com/?searchname
    private String url;
    //书名encode,URLEncoder.encode(searchname, charset_encode)
    private String charset;
    private String is_redirect;
    private String redirect_field;
    private String url_selector_r;
    private String url_selector;
    private String name_selector_r;
    private String name_selector;
    private String image_selector_r;
    private String image_selector;

    @Generated(hash = 2134448244)
    public SearchRules(Long id,
                       String hostname,
                       String url,
                       String charset,
                       String is_redirect,
                       String redirect_field,
                       String url_selector_r,
                       String url_selector,
                       String name_selector_r,
                       String name_selector,
                       String image_selector_r,
                       String image_selector) {
        this.id = id;
        this.hostname = hostname;
        this.url = url;
        this.charset = charset;
        this.is_redirect = is_redirect;
        this.redirect_field = redirect_field;
        this.url_selector_r = url_selector_r;
        this.url_selector = url_selector;
        this.name_selector_r = name_selector_r;
        this.name_selector = name_selector;
        this.image_selector_r = image_selector_r;
        this.image_selector = image_selector;
    }

    @Generated(hash = 205245395)
    public SearchRules() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getIs_redirect() {
        return is_redirect;
    }

    public void setIs_redirect(String is_redirect) {
        this.is_redirect = is_redirect;
    }

    public String getRedirect_field() {
        return redirect_field;
    }

    public void setRedirect_field(String redirect_field) {
        this.redirect_field = redirect_field;
    }

    public String getUrl_selector_r() {
        return url_selector_r;
    }

    public void setUrl_selector_r(String url_selector_r) {
        this.url_selector_r = url_selector_r;
    }

    public String getUrl_selector() {
        return url_selector;
    }

    public void setUrl_selector(String url_selector) {
        this.url_selector = url_selector;
    }

    public String getName_selector_r() {
        return name_selector_r;
    }

    public void setName_selector_r(String name_selector_r) {
        this.name_selector_r = name_selector_r;
    }

    public String getName_selector() {
        return name_selector;
    }

    public void setName_selector(String name_selector) {
        this.name_selector = name_selector;
    }

    public String getImage_selector_r() {
        return image_selector_r;
    }

    public void setImage_selector_r(String image_selector_r) {
        this.image_selector_r = image_selector_r;
    }

    public String getImage_selector() {
        return image_selector;
    }

    public void setImage_selector(String image_selector) {
        this.image_selector = image_selector;
    }

}
