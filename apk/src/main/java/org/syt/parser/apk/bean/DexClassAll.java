package org.syt.parser.apk.bean;


import org.syt.parser.entry.BaseBean;

import java.util.Objects;

/*
 * zhulei 2025/3/11-16:51
 */
public class DexClassAll implements BaseBean {

    private String name;
    private Long size;

    public DexClassAll(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getFileName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "DexClassAll{" +
                "name=\"" + name + '\"' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DexClassAll that = (DexClassAll) o;
        return Objects.equals(name, that.name) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }
}
