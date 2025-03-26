package org.syt.parser.apk.bean;

import org.syt.parser.apk.ApkDiff;
import org.syt.parser.entry.BaseBean;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * zhulei 2025/3/11-16:36
 */
public class NativeLibs implements BaseBean {

    private String name;
    private List<Libs> libs = new ArrayList<>();

    public NativeLibs(String name) {
        this.name = name;
    }

    @Override
    public String getFileName() {
        return name;
    }

    public List<Libs> getLibs() {
        return libs;
    }

    public void addLib(String name, long size) {
        this.libs.add(new Libs(name, size));
    }

    static class Libs implements BaseBean {
        private String name;
        private long size;

        public Libs(String name, long size) {
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
            return "Libs{" +
                    "name=\"" + name + '\"' +
                    ", size=" + size +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Libs libs = (Libs) o;
            return size == libs.size && Objects.equals(name, libs.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, size);
        }
    }

    @Override
    public String toString() {
        return "NativeLibs{" +
                "name=\"" + name + '\"' +
                ", libs=" + libs +
                '}';
    }

    /**
     * 判断是否存在
     */
    public static JSONObject compare(List<NativeLibs> componentLists1, List<NativeLibs> componentLists2) throws JSONException {

        JSONObject diffs = ApkDiff.compare(componentLists1, componentLists2);

        for (NativeLibs component1 : componentLists1) {
            for (NativeLibs component2 : componentLists2) {
                if (component1.getFileName().equals(component2.getFileName())) {
                    JSONObject compare = ApkDiff.compare(component1.getLibs(), component2.getLibs());
                    if (compare != null && compare.length() > 0) {
                        diffs.putOpt(component1.getFileName(), compare);
                    }
                }
            }
        }

        return diffs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NativeLibs that = (NativeLibs) o;
        return Objects.equals(name, that.name) && Objects.equals(libs, that.libs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, libs);
    }
}


