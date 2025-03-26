package org.syt.parser.apk;

import org.jetbrains.annotations.NotNull;
import org.syt.parser.apk.AbstractApkFile;
import org.syt.parser.apk.bean.ApkSignStatus;
import org.syt.parser.apk.utils.Inputs;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;
import org.syt.parser.util.MD5;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parse apk file from byte array.
 * This class is not thread-safe
 *
 * @author Liu Dong
 */
public class ByteArrayApkFile extends AbstractApkFile implements Closeable {

    private byte[] apkData;
    private String fileName = "";

    public ByteArrayApkFile(byte[] apkData) {
        this.apkData = apkData;
    }

    public ByteArrayApkFile(byte[] apkData, String fileName) {
        this.apkData = apkData;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    protected List<CertificateFile> getAllCertificateData() throws IOException {
        List<CertificateFile> list = new ArrayList<>();
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.toUpperCase().endsWith(".RSA") || name.toUpperCase().endsWith(".DSA")) {
                    list.add(new CertificateFile(name, Inputs.readAll(zis)));
                }
            }
        }
        return list;
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (path.equals(entry.getName())) {
                    return Inputs.readAll(zis);
                }
            }
        }
        return null;
    }

    @Override
    public long getFileSize(String path) throws IOException {
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (path.equals(entry.getName())) {
                    return entry.getSize();
                }
            }
        }
        return -1;
    }

    @Override
    protected List<String> getAllFile() throws IOException {
        List<String> list = new ArrayList<>();
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                list.add(entry.getName());
            }
        }
        return list;
    }

    @Override
    protected Map<String, Long> getAllFileSize() throws IOException {
        Map<String, Long> map = new HashMap<>();
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                map.put(entry.getName(), entry.getSize());
            }
        }
        return map;
    }

    @Override
    protected ByteBuffer fileData() {
        return ByteBuffer.wrap(apkData).asReadOnlyBuffer();
    }

    @Deprecated
    @Override
    public ApkSignStatus verifyApk() {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public JSONObject getInfo() {
        JSONObject jsonObject = super.getInfo();

        try {
            jsonObject.putOpt("apkSize", apkData.length);
        } catch (JSONException e) {
            Log.e(e);
        }

        return jsonObject;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.apkData = null;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
