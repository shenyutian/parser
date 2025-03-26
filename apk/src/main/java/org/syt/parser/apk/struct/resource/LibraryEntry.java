package org.syt.parser.apk.struct.resource;

/**
 * Library chunk entry
 *
 * @author Liu Dong
 */
public class LibraryEntry {
    // uint32. The package-id this shared library was assigned at build time.
    private int packageId;

    //The package name of the shared library. \0 terminated. max 128
    private String name;

    public LibraryEntry(int packageId, String name) {
        this.packageId = packageId;
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"packageId\":").append(packageId)
                .append(", \"name\":").append(name)
                .append('}');
        return sb.toString();
    }
}
