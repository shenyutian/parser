package org.syt.parser.aab;


import org.jetbrains.annotations.NotNull;
import org.syt.parser.base.BaseApkFile;
import org.syt.parser.entry.*;
import org.syt.parser.apk.bean.ApkSigner;
import org.syt.parser.apk.bean.CertificateMeta;
import org.syt.parser.apk.exception.ParserException;
import org.syt.parser.apk.parser.CertificateParser;
import org.syt.parser.apk.parser.DexParser;
import org.syt.parser.apk.utils.Inputs;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;
import org.syt.parser.util.MD5;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/*
 * zhulei 2024/8/7-上午9:58
 */
public class AabFile extends BaseApkFile {

    private final ZipFile zf;
    private File aabFile;

    private String manifestXml;
    private ApkMeta apkMeta;
    private List<ApkSigner> apkSigners;

    private boolean manifestParsed;

    private DexClass[] dexClasses;

    public AabFile(File aabFile) throws IOException {
        this.aabFile = aabFile;
        this.zf = new ZipFile(aabFile);
    }

    public AabFile(String aabPath) throws IOException {
        this(new File(aabPath));
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    @Override
    public ApkMeta getApkMeta() {
        parseManifest();
        return this.apkMeta;
    }

    private void parseManifest() {
        if (manifestParsed) {
            return;
        }
        apkMeta = AabMetaTranslator.INSTANCE.parse(aabFile);
        manifestParsed = true;
    }

    protected List<CertificateFile> getAllCertificateData() throws CertificateException, IOException {
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

    public List<ApkSigner> getApkSingers() throws IOException, CertificateException {
        if (apkSigners == null) {
            parseCertificates();
        }
        return this.apkSigners;
    }

    private void parseCertificates() throws IOException, CertificateException {
        this.apkSigners = new ArrayList<>();
        for (CertificateFile file : getAllCertificateData()) {
            CertificateParser parser = CertificateParser.getInstance(file.getData());
            List<CertificateMeta> certificateMetas = parser.parse();
            apkSigners.add(new ApkSigner(file.getPath(), certificateMetas));
        }
    }

    @Override
    public DexClass[] getDexClasses() throws IOException {
        if (this.dexClasses == null) {
            this.parseDexFiles();
        }
        return this.dexClasses;
    }

    @Override
    @NotNull
    public JSONObject getInfo() {
        JSONObject info = super.getInfo();
        try {
            info.putOpt("aabSize", aabFile.length()); // apk大小
            info.putOpt("aabFileMd5", MD5.toMd5(aabFile));
        } catch (JSONException e) {
            Log.e(e);
        }

        try {
            byte[] sign_data = getApkSingers().get(0).getCertificateMetas().get(0).getData();
            Certificate certificate = java.security.cert.CertificateFactory.getInstance("X.509")
                    .generateCertificate(new java.io.ByteArrayInputStream(sign_data));

            Base64.Encoder encoder = Base64.getEncoder();

            PublicKey publicKey = certificate.getPublicKey();
            info.putOpt("sign", MD5.toHexString(sign_data).replace("\n", ""));
            info.putOpt("SHA1", encoder.encodeToString(MD5.toSha1(publicKey.getEncoded())));
            info.putOpt("SHA256", encoder.encodeToString(MD5.toSha256(publicKey.getEncoded())));
            info.putOpt("fbkey", encoder.encodeToString(MD5.toSha1(certificate.getEncoded())));
        } catch (Exception e) {
            Log.e(e);
        }

        return info;
    }

    private void parseDexFiles() throws IOException {
        this.dexClasses = this.parseDexFile("base/dex/classes.dex");

        for (int i = 2; i < 1000; ++i) {
            String path = String.format("base/dex/classes%d.dex", i);

            try {
                DexClass[] classes = this.parseDexFile(path);
                this.dexClasses = this.mergeDexClasses(this.dexClasses, classes);
            } catch (Exception var4) {
                break;
            }
        }

    }

    private DexClass[] parseDexFile(String path) throws IOException {
        byte[] data = this.getFileData(path);
        if (data == null) {
            String msg = String.format("Dex file %s not found", path);
            throw new RuntimeException(msg);
        } else {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            DexParser dexParser = new DexParser(buffer);
            return dexParser.parse();
        }
    }

    public byte[] getFileData(String path) throws IOException {
        ZipEntry entry = this.zf.getEntry(path);
        if (entry == null) {
            return null;
        } else {
            InputStream inputStream = this.zf.getInputStream(entry);
            return Inputs.readAllAndClose(inputStream);
        }
    }

    private DexClass[] mergeDexClasses(DexClass[] first, DexClass[] second) {
        DexClass[] result = new DexClass[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
