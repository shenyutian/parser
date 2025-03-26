package org.syt.parser.entry;

/*
 * zhulei 2024/8/7-上午10:15
 */
public class CertificateFile {
    private String path;
    private byte[] data;

    public CertificateFile(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }
}