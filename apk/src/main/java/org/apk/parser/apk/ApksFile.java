package org.apk.parser.apk;

import org.jetbrains.annotations.NotNull;
import org.apk.parser.apk.bean.ApkSignStatus;
import org.apk.parser.apk.utils.ApkSignatureUtil;
import org.apk.parser.apk.utils.Inputs;

import org.jetbrains.annotations.Nullable;
import org.apk.parser.json.JSONException;
import org.apk.parser.json.JSONObject;
import org.apk.parser.log.Log;
import org.apk.parser.util.MD5;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * ApkFile, for parsing apk file info.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
public class ApksFile extends AbstractApkFile implements Closeable {

    private final ZipFile zf;
    private File apksFile;
    @Nullable
    private FileChannel fileChannel;
    private List<ByteArrayApkFile> apksFiles = new ArrayList<>();

    public ApksFile(File apkFile) throws IOException {
        this.apksFile = apkFile;
        // create zip file cost time, use one zip file for apk parser life cycle
        this.zf = new ZipFile(apkFile);
        List<String> allFile = getZipAllFile();
        for (String s : allFile) {
            if (s.endsWith(".apk")) {
                byte[] zipFileData = getZipFileData(s);
                if (zipFileData != null) {
                    // 二种解析方式 这种方式内存占用过大 todo 优化
                    apksFiles.add(new ByteArrayApkFile(zipFileData, s));
                }
                // 如果直接解压，然后读取，内存会好一点
            }
        }
        // 让 base.apk 排在最前：AndroidManifest.xml / resources.arsc 等通过 getFileData 取第一个匹配，
        // 若 config 分片排在前面会读到分片的残缺 manifest（包名有、label/version 缺失），故基准包置顶。
        ByteArrayApkFile base = getBaseApkFile();
        if (base != null && apksFiles.remove(base)) {
            apksFiles.add(0, base);
        }
    }

    public ApksFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    private List<String> getZipAllFile() throws IOException{
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

    public byte[] getZipFileData(String path) throws IOException {
        ZipEntry entry = zf.getEntry(path);
        if (entry == null) {
            return null;
        }

        InputStream inputStream = zf.getInputStream(entry);
        return Inputs.readAllAndClose(inputStream);
    }

    @Override
    protected List<CertificateFile> getAllCertificateData() throws IOException {
        List<CertificateFile> list = new ArrayList<>();
        for (ByteArrayApkFile file : apksFiles) {
            list.addAll(file.getAllCertificateData());
        }
        return list;
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        for (ByteArrayApkFile file : apksFiles) {
            byte[] fileData = file.getFileData(path);
            if (fileData != null) {
                return fileData;
            }
        }
        return null;
    }

    @Override
    public long getFileSize(String path) throws IOException {
        // 这样会比较慢，用内存换时间
        for (ByteArrayApkFile file : apksFiles) {
            long fileSize = file.getFileSize(path);
            if (fileSize > 0) {
                return fileSize;
            }
        }
        return -1;
    }

    @Override
    protected ByteBuffer fileData() throws IOException {
        for (ByteArrayApkFile file : apksFiles) {
            try {
                file.fileData();
            } catch (Exception e) {

            }
        }
        fileChannel = new FileInputStream(apksFile).getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    @Override
    protected List<String> getAllFile() throws IOException{
        List<String> list = new ArrayList<>();
        for (ByteArrayApkFile file : apksFiles) {
            List<String> allFile = file.getAllFile();
            list.addAll(allFile);
        }
        return list;
    }

    @Override
    protected Map<String, Long> getAllFileSize() throws IOException {
        Map<String, Long> map = new HashMap<>();
        for (ByteArrayApkFile file : apksFiles) {
            Map<String, Long> allFileSize = file.getAllFileSize();
            // 小心名称重复
            for (String s : allFileSize.keySet()) {
                if (!map.containsKey(s)) {
                    map.put(s, allFileSize.get(s));
                } else {
                    // todo 通过对文件区分 目前重复名称 AndroidManifest.xml
                    // 如果大小不变，先保留大文件
                    Long old = map.get(s);
                    if (old < allFileSize.get(s)) {
                        map.put(s, allFileSize.get(s));
                    } else {
                        System.out.println("重复文件名称 = " + s + " " + old + " -> " + allFileSize.get(s));
                    }
                }
            }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        for (ByteArrayApkFile file : apksFiles) {
            file.close();
        }
        try (Closeable superClosable = new Closeable() {
            @Override
            public void close() throws IOException {
                ApksFile.super.close();
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
    protected ApkSignatureUtil.ApkSignatureResult readSignatureInfo() throws Exception {
        // apks 外层包不含 APK Signing Block，签名信息取自内部的 base.apk 字节
        ByteArrayApkFile baseApk = getBaseApkFile();
        byte[] baseData = baseApk == null ? null : baseApk.getApkData();
        if (baseData == null) {
            return null;
        }
        return ApkSignatureUtil.readApkSignatureInfo(baseData);
    }

    @Override
    @NotNull
    public JSONObject getInfo() {
        JSONObject jsonObject = super.getInfo();

        try {
            // 优先取其中 base.apk 的文件参数；找不到时回退到整个 apks 包
            ByteArrayApkFile baseApk = getBaseApkFile();
            byte[] baseData = baseApk == null ? null : baseApk.getApkData();
            if (baseData != null) {
                jsonObject.putOpt("apkSize", baseData.length);
                jsonObject.putOpt("apkFileMd5", MD5.toMd5(baseData));
                jsonObject.putOpt("apkFileSha256", MD5.toSha256Hex(baseData));
            } else {
                jsonObject.putOpt("apkSize", apksFile.length());
                jsonObject.putOpt("apkFileMd5", MD5.toMd5(apksFile));
                jsonObject.putOpt("apkFileSha256", MD5.toSha256(apksFile));
            }
        } catch (JSONException e) {
            Log.e(e);
        }

        return jsonObject;
    }

    /**
     * 查找 apks 包中的 base.apk。
     * 依次匹配：文件名等于 base.apk（如 xapk）、包含 base-master（如 bundletool 拆分包）、包含 base；
     * 都找不到时回退到体积最大的 .apk 文件。
     */
    @Nullable
    private ByteArrayApkFile getBaseApkFile() {
        ByteArrayApkFile containsBase = null;
        ByteArrayApkFile largest = null;
        for (ByteArrayApkFile file : apksFiles) {
            String name = file.getFileName();
            if (name == null) {
                continue;
            }
            String simpleName = name.substring(name.lastIndexOf('/') + 1).toLowerCase();
            if ("base.apk".equals(simpleName) || simpleName.contains("base-master")) {
                return file;
            }
            if (containsBase == null && simpleName.contains("base")) {
                containsBase = file;
            }
            byte[] data = file.getApkData();
            if (data == null) {
                continue;
            }
            byte[] largestData = largest == null ? null : largest.getApkData();
            if (largestData == null || data.length > largestData.length) {
                largest = file;
            }
        }
        return containsBase != null ? containsBase : largest;
    }
}
