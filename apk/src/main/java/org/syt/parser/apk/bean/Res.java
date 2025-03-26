package org.syt.parser.apk.bean;


import org.syt.parser.entry.BaseBean;

import java.util.Objects;

/*
 * zhulei 2025/3/11-16:47
 */
public class Res implements BaseBean {

    private String path;
    private long size;

    public Res(String path, long size) {
        this.path = path;
        this.size = size;
    }

    @Override
    public String getFileName() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "{" +
                "path=\"" + path + '\"' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Res res = (Res) o;
        return size == res.size && Objects.equals(path, res.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, size);
    }
}
