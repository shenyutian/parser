package org.syt.parser.apk.bean;


import org.syt.parser.entry.BaseBean;

import java.util.Objects;

/*
 * zhulei 2025/3/11-16:38
 */
public class MetaInf implements BaseBean {

    private String name;
    private long size;

    public MetaInf(String name, long size) {
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
        return "MetaInf{" +
                "name=\"" + name + '\"' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaInf metaInf = (MetaInf) o;
        return size == metaInf.size && Objects.equals(name, metaInf.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }
}
