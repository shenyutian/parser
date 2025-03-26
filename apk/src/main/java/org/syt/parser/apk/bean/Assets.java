package org.syt.parser.apk.bean;


import org.syt.parser.entry.BaseBean;

import java.util.Objects;

/*
 * zhulei 2025/3/11-16:37
 */
public class Assets implements BaseBean {
    private final String name;
    private final long size;

    public Assets(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getFileName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Assets{" +
                "name=\"" + name + '\"' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assets assets = (Assets) o;
        return size == assets.size && Objects.equals(name, assets.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }
}
