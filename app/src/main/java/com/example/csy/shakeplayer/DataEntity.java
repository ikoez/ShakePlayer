package com.example.csy.shakeplayer;

/**
 * Created by Owner on 2017/2/9.
 */

import java.io.Serializable;


public class DataEntity implements Serializable {
    private int bookmarktime;
    public DataEntity()
    {
        bookmarktime=0;
    }

    public int getBookmarktime() {
        return bookmarktime;
    }

    public void setBookmarktime(int bookmarktime) {
        this.bookmarktime = bookmarktime;
    }


}
