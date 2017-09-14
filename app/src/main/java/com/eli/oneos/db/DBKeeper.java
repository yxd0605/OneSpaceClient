package com.eli.oneos.db;

import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/7/12.
 */
public abstract class DBKeeper<T> {
    public abstract DBKeeper creator();

    public abstract List<T> all();

    public abstract T query(Object key);

    public abstract boolean insert(T t);

    public abstract boolean update(T t);

    public abstract boolean delete(T t);

}
