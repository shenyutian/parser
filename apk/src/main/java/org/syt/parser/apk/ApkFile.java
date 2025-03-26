package org.syt.parser.apk;

import org.jetbrains.annotations.NotNull;
import org.syt.parser.apk.bean.ApkSignStatus;
import org.syt.parser.apk.utils.Inputs;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.entry.DexClass;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;
import org.syt.parser.util.MD5;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * ApkFile, for parsing apk file info.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
public class ApkFile extends AbstractApkFile implements Closeable {

    private final ZipFile zf;
    private File apkFile;
    private FileChannel fileChannel;

    public ApkFile(File apkFile) throws IOException {
        this.apkFile = apkFile;
        // create zip file cost time, use one zip file for apk parser life cycle
        this.zf = new ZipFile(apkFile);
    }

    public ApkFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    @Override
    protected List<CertificateFile> getAllCertificateData() throws IOException {
        Enumeration<? extends ZipEntry> enu = zf.entries();
        List<CertificateFile> list = new ArrayList<>();
        while (enu.hasMoreElements()) {
            ZipEntry ne = enu.nextElement();
            if (ne.isDirectory()) {
                continue;
            }
            String name = ne.getName().toUpperCase();
            if (name.endsWith(".RSA") || name.endsWith(".DSA")) {
                list.add(new CertificateFile(name, Inputs.readAllAndClose(zf.getInputStream(ne))));
            }
        }
        return list;
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = zf.getEntry(path);
        if (entry == null) {
            return null;
        }

        InputStream inputStream = zf.getInputStream(entry);
        return Inputs.readAllAndClose(inputStream);
    }

    @Override
    public long getFileSize(String path) throws IOException {
        ZipEntry entry = zf.getEntry(path);
        if (entry == null) {
            return -1;
        }
        return entry.getSize();
    }

    @Override
    protected ByteBuffer fileData() throws IOException {
        fileChannel = new FileInputStream(apkFile).getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    @Override
    protected List<String> getAllFile() throws IOException{
        Enumeration<? extends ZipEntry> enu = zf.entries();
        List<String> list = new ArrayList<>();
        while (enu.hasMoreElements()) {
            ZipEntry ne = enu.nextElement();
            if (ne.isDirectory()) {
                continue;
            }
            list.add(ne.getName());
        }
        return list;
    }

    @Override
    protected Map<String, Long> getAllFileSize() throws IOException {
        Enumeration<? extends ZipEntry> enu = zf.entries();
        Map<String, Long> map = new HashMap<>();
        while (enu.hasMoreElements()) {
            ZipEntry ne = enu.nextElement();
            if (ne.isDirectory()) {
                continue;
            }
            map.put(ne.getName(), ne.getSize());
        }
        return map;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated using google official ApkVerifier of apksig lib instead.
     */
    @Override
    @Deprecated
    public ApkSignStatus verifyApk() throws IOException {
        ZipEntry entry = zf.getEntry("META-INF/MANIFEST.MF");
        if (entry == null) {
            // apk is not signed;
            return ApkSignStatus.notSigned;
        }

        try (JarFile jarFile = new JarFile(this.apkFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            byte[] buffer = new byte[8192];

            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (e.isDirectory()) {
                    continue;
                }
                try (InputStream in = jarFile.getInputStream(e)) {
                    // Read in each jar entry. A security exception will be thrown if a signature/digest check fails.
                    int count;
                    while ((count = in.read(buffer, 0, buffer.length)) != -1) {
                        // Don't care
                    }
                } catch (SecurityException se) {
                    return ApkSignStatus.incorrect;
                }
            }
        }
        return ApkSignStatus.signed;
    }

    @Override
    public void close() throws IOException {
        try (Closeable superClosable = new Closeable() {
            @Override
            public void close() throws IOException {
                ApkFile.super.close();
            }
        };
             Closeable zipFileClosable = zf;
             Closeable fileChannelClosable = fileChannel) {

        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    @NotNull
    public JSONObject getInfo() {
        JSONObject jsonObject = super.getInfo();

        try {
            jsonObject.putOpt("apkSize", apkFile.length());
            jsonObject.putOpt("apkFileMd5", MD5.toMd5(apkFile));
        } catch (JSONException e) {
            Log.e(e);
        }

        return jsonObject;
    }
}
