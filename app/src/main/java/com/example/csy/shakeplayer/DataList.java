package com.example.csy.shakeplayer;

/**
 * Created by Owner on 2017/2/9.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataList implements Serializable {
    private List<DataEntity> array;
    private String filename;
    public DataList(){
        array=new ArrayList<DataEntity>();
        filename="temple";
    }
    public void additem(DataEntity item){
        array.add(item);
    }
    public void setFilename(String fname)
    {
        filename=fname;
    }
    public String getFilename()
    {
        return filename;
    }
    public DataEntity getitem(int i){
        return array.get(i);
    }
    public void remove(int i){
        array.remove(i);
    }
    public int size()
    {
        return array.size();
    }
    public boolean isempty(){
        return array.isEmpty();
    }
    public void destroy()
    {
        array.clear();
    }
}