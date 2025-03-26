package org.syt.parser.apk;

import org.jetbrains.annotations.NotNull;
import org.syt.parser.apk.bean.*;
import org.syt.parser.apk.exception.ParserException;
import org.syt.parser.apk.parser.*;
import org.syt.parser.apk.struct.AndroidConstants;
import org.syt.parser.apk.struct.resource.Densities;
import org.syt.parser.apk.struct.resource.ResourceTable;
import org.syt.parser.apk.struct.signingv2.ApkSigningBlock;
import org.syt.parser.apk.struct.signingv2.SignerBlock;
import org.syt.parser.apk.struct.zip.EOCD;
import org.syt.parser.apk.utils.Buffers;
import org.syt.parser.apk.utils.Unsigned;
import org.syt.parser.base.BaseApkFile;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.entry.DexClass;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;
import org.syt.parser.util.MD5;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static java.lang.System.arraycopy;

/**
 * Common Apk Parser methods.
 * This Class is not thread-safe.
 *
 * @author Liu Dong
 */
public abstract class AbstractApkFile extends BaseApkFile implements Closeable {
    private DexClass[] dexClasses;

    private boolean resourceTableParsed;
    private ResourceTable resourceTable;
    private Set<Locale> locales;

    private boolean manifestParsed;
    private String manifestXml;
    private ApkMeta apkMeta;
    private List<IconPath> iconPaths;

    private List<ApkSigner> apkSigners;
    private List<ApkV2Signer> apkV2Signers;

    // 原生库信息
    private List<Assets> assetsList;
    private List<MetaInf> metaInfs;
    private List<NativeLibs> nativeLibsList;
    private List<Res> resList;
    private List<DexClassAll> dexClassesAll;

    private static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * default use empty locale
     */
    private Locale preferredLocale = DEFAULT_LOCALE;

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public String getManifestXml() throws IOException {
        parseManifest();
        return this.manifestXml;
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    @Override
    public ApkMeta getApkMeta() {
        try {
            parseManifest();
        } catch (IOException e) {
            Log.e(e);
        }
        return this.apkMeta;
    }

    /**
     * get locales supported from resource file
     *
     * @return decoded AndroidManifest.xml
     * @throws IOException
     */
    public Set<Locale> getLocales() throws IOException {
        parseResourceTable();
        return this.locales;
    }

    public List<NativeLibs> getNativeLibs() throws IOException {
        parseOther();
        return nativeLibsList;
    }

    public List<Res> getResList() throws IOException {
        parseOther();
        return resList;
    }

    public List<Assets> getAssetsList() throws IOException {
        parseOther();
        return assetsList;
    }

    public List<MetaInf> getMetaInfs() throws IOException {
        parseOther();
        return metaInfs;
    }

    public List<DexClassAll> getDexClassesAll() throws IOException {
        if (this.dexClassesAll == null) {
            parseOther();
        }
        return this.dexClassesAll;
    }

    /**
     * Get the apk's certificate meta. If have multi signer, return the certificate the first signer used.
     *
     * @deprecated use {{@link #getApkSingers()}} instead
     */
    @Deprecated
    public List<CertificateMeta> getCertificateMetaList() throws IOException, CertificateException {
        if (apkSigners == null) {
            parseCertificates();
        }
        if (apkSigners.isEmpty()) {
            throw new ParserException("ApkFile certificate not found");
        }
        return apkSigners.get(0).getCertificateMetas();
    }

    /**
     * Get the apk's all certificates.
     * For each entry, the key is certificate file path in apk file, the value is the certificates info of the certificate file.
     *
     * @deprecated use {{@link #getApkSingers()}} instead
     */
    @Deprecated
    public Map<String, List<CertificateMeta>> getAllCertificateMetas() throws IOException, CertificateException {
        List<ApkSigner> apkSigners = getApkSingers();
        Map<String, List<CertificateMeta>> map = new LinkedHashMap<>();
        for (ApkSigner apkSigner : apkSigners) {
            map.put(apkSigner.getPath(), apkSigner.getCertificateMetas());
        }
        return map;
    }

    /**
     * Get the apk's all cert file info, of apk v1 signing.
     * If cert faile not exist, return empty list.
     */
    public List<ApkSigner> getApkSingers() throws IOException, CertificateException {
        if (apkSigners == null) {
            parseCertificates();
        }
        return this.apkSigners;
    }

    @Override
    @NotNull
    public JSONObject getInfo() {
        JSONObject jsonObject = super.getInfo();

        try {
            byte[] sign_data;

            try {
                sign_data = getApkSingers().get(0).getCertificateMetas().get(0).getData();
            } catch (Exception e) {
                sign_data = getApkV2Singers().get(0).getCertificateMetas().get(0).getData();
            }

            Certificate certificate = java.security.cert.CertificateFactory.getInstance("X.509")
                    .generateCertificate(new java.io.ByteArrayInputStream(sign_data));

            Base64.Encoder encoder = Base64.getEncoder();


            PublicKey publicKey = certificate.getPublicKey();
            jsonObject.putOpt("sign", MD5.toHexString(sign_data).replace("\n", ""));
            jsonObject.putOpt("sign", MD5.toHexString(sign_data).replace("\n", ""));
            jsonObject.putOpt("SHA1", encoder.encodeToString(MD5.toSha1(publicKey.getEncoded())));
            jsonObject.putOpt("SHA256", encoder.encodeToString(MD5.toSha256(publicKey.getEncoded())));
            jsonObject.putOpt("fbkey", encoder.encodeToString(MD5.toSha1(certificate.getEncoded())));

        } catch (Exception e) {
            Log.e(e);
        }
        return jsonObject;
    }

    /**
     * 获取全部数据，解析数据
     */
    public void parseAll() {
        try {
            parseManifest();
            parseDexFiles();
            parseApkSigningBlock();
//            parseResourceTable();
            parseOther();
            parseCertificates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseCertificates() throws IOException, CertificateException {
        this.apkSigners = new ArrayList<>();
        for (CertificateFile file : getAllCertificateData()) {
            CertificateParser parser = CertificateParser.getInstance(file.getData());
            List<CertificateMeta> certificateMetas = parser.parse();
            apkSigners.add(new ApkSigner(file.getPath(), certificateMetas));
        }
    }

    /**
     * Get the apk's all signer in apk sign block, using apk singing v2 scheme.
     * If apk v2 signing block not exists, return empty list.
     */
    public List<ApkV2Signer> getApkV2Singers() throws IOException, CertificateException {
        if (apkV2Signers == null) {
            parseApkSigningBlock();
        }
        return this.apkV2Signers;
    }

    private void parseApkSigningBlock() throws IOException, CertificateException {
        List<ApkV2Signer> list = new ArrayList<>();
        ByteBuffer apkSignBlockBuf = findApkSignBlock();
        if (apkSignBlockBuf != null) {
            ApkSignBlockParser parser = new ApkSignBlockParser(apkSignBlockBuf);
            ApkSigningBlock apkSigningBlock = parser.parse();
            for (SignerBlock signerBlock : apkSigningBlock.getSignerBlocks()) {
                List<X509Certificate> certificates = signerBlock.getCertificates();
                List<CertificateMeta> certificateMetas = CertificateMetas.from(certificates);
                ApkV2Signer apkV2Signer = new ApkV2Signer(certificateMetas);
                list.add(apkV2Signer);
            }
        }
        this.apkV2Signers = list;
    }


    protected abstract List<CertificateFile> getAllCertificateData() throws IOException;

    public static class CertificateFile {
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

    private void parseManifest() throws IOException {
        if (manifestParsed) {
            return;
        }
        parseResourceTable();
        XmlTranslator xmlTranslator = new XmlTranslator();
        ApkMetaTranslator apkTranslator = new ApkMetaTranslator(this.resourceTable, this.preferredLocale);
        XmlStreamer xmlStreamer = new CompositeXmlStreamer(xmlTranslator, apkTranslator);

        byte[] data = getFileData(AndroidConstants.MANIFEST_FILE);
        if (data == null) {
            throw new ParserException("Manifest file not found");
        }
        transBinaryXml(data, xmlStreamer);
        this.manifestXml = xmlTranslator.getXml();
        this.apkMeta = apkTranslator.getApkMeta();
        this.iconPaths = apkTranslator.getIconPaths();
        manifestParsed = true;
    }

    private void parseOther() throws IOException {
        resList = new ArrayList<>();
        nativeLibsList = new ArrayList<>();
        assetsList = new ArrayList<>();
        metaInfs = new ArrayList<>();
        dexClassesAll = new ArrayList<>();
        Map<String, Long> allFileSize = getAllFileSize();
        int i = 0;
        for (String path : allFileSize.keySet()) {
            // 内存优化
//            System.out.println(i++ + "/" + allFileSize.size() + " " + path);
            if (path.startsWith(AndroidConstants.RES_PREFIX)) {
                resList.add(new Res(path.replace(AndroidConstants.RES_PREFIX, ""), allFileSize.get(path)));
            } else if (path.startsWith(AndroidConstants.ASSETS_PREFIX)) {
                assetsList.add(new Assets(path.replace(AndroidConstants.ASSETS_PREFIX, ""), allFileSize.get(path)));
            } else if (path.startsWith(AndroidConstants.META_PREFIX)) {
                metaInfs.add(new MetaInf(path.replace(AndroidConstants.META_PREFIX, ""), allFileSize.get(path)));
            } else if (path.startsWith(AndroidConstants.LIB_PREFIX)) {
                String[] split = path.replace(AndroidConstants.LIB_PREFIX, "").split("/", 2);
                if (split.length == 2) {
                    String libName = split[0];
                    String libPath = split[1];
                    NativeLibs nativeLibs = null;
                    for (NativeLibs n : nativeLibsList) {
                        if (n.getFileName().equals(libName)) {
                            nativeLibs = n;
                            break;
                        }
                    }
                    if (nativeLibs == null) {
                        nativeLibs = new NativeLibs(libName);
                        nativeLibsList.add(nativeLibs);
                    }
                    nativeLibs.addLib(libPath, allFileSize.get(path));
                }
            } else if (path.endsWith(AndroidConstants.DEX_SUFFIX)) {
                dexClassesAll.add(new DexClassAll(path, allFileSize.get(path)));
            }
        }
    }

    /**
     * read file in apk into bytes
     */
    public abstract byte[] getFileData(String path) throws IOException;

    /**
     * 读取文件大小
     */
    public abstract long getFileSize(String path) throws IOException;

    /**
     * return the whole apk file as ByteBuffer
     */
    protected abstract ByteBuffer fileData() throws IOException;

    /**
     * 获取所有的文件
     */
    protected abstract List<String> getAllFile() throws IOException;

    /**
     * 获取所有文件和大小
     */
    protected abstract Map<String, Long> getAllFileSize() throws IOException;

    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     * @throws IOException
     */
    public String transBinaryXml(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            return null;
        }
        parseResourceTable();

        XmlTranslator xmlTranslator = new XmlTranslator();
        transBinaryXml(data, xmlTranslator);
        return xmlTranslator.getXml();
    }

    private void transBinaryXml(byte[] data, XmlStreamer xmlStreamer) throws IOException {
        parseResourceTable();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer, resourceTable);
        binaryXmlParser.setLocale(preferredLocale);
        binaryXmlParser.setXmlStreamer(xmlStreamer);
        binaryXmlParser.parse();
    }

    /**
     * This method return icons specified in android manifest file, application.
     * The icons could be file icon, color icon, or adaptive icon, etc.
     *
     * @return icon files.
     */
    public List<IconFace> getAllIcons() throws IOException {
        List<IconPath> iconPaths = getIconPaths();
        if (iconPaths.isEmpty()) {
            return Collections.emptyList();
        }
        List<IconFace> iconFaces = new ArrayList<>(iconPaths.size());
        for (IconPath iconPath : iconPaths) {
            String filePath = iconPath.getPath();
            if (filePath.endsWith(".xml")) {
                // adaptive icon?
                byte[] data = getFileData(filePath);
                if (data == null) {
                    continue;
                }
                parseResourceTable();

                AdaptiveIconParser iconParser = new AdaptiveIconParser();
                transBinaryXml(data, iconParser);
                Icon backgroundIcon = null;
                if (iconParser.getBackground() != null) {
                    backgroundIcon = newFileIcon(iconParser.getBackground(), iconPath.getDensity());
                }
                Icon foregroundIcon = null;
                if (iconParser.getForeground() != null) {
                    foregroundIcon = newFileIcon(iconParser.getForeground(), iconPath.getDensity());
                }
                AdaptiveIcon icon = new AdaptiveIcon(foregroundIcon, backgroundIcon);
                iconFaces.add(icon);
            } else {
                Icon icon = newFileIcon(filePath, iconPath.getDensity());
                iconFaces.add(icon);
            }
        }
        return iconFaces;
    }

    private Icon newFileIcon(String filePath, int density) throws IOException {
        return new Icon(filePath, density, getFileData(filePath));
    }

    /**
     * Get the default apk icon file.
     *
     * @deprecated use {@link #getAllIcons()}
     */
    @Deprecated
    public Icon getIconFile() throws IOException {
        ApkMeta apkMeta = getApkMeta();
        String iconPath = apkMeta.getIcon();
        if (iconPath == null) {
            return null;
        }
        return new Icon(iconPath, Densities.DEFAULT, getFileData(iconPath));
    }

    /**
     * Get all the icon paths, for different densities.
     *
     * @deprecated using {@link #getAllIcons()} instead
     */
    @Deprecated
    public List<IconPath> getIconPaths() throws IOException {
        parseManifest();
        return this.iconPaths;
    }

    /**
     * Get all the icons, for different densities.
     *
     * @deprecated using {@link #getAllIcons()} instead
     */
    @Deprecated
    public List<Icon> getIconFiles() throws IOException {
        List<IconPath> iconPaths = getIconPaths();
        List<Icon> icons = new ArrayList<>(iconPaths.size());
        for (IconPath iconPath : iconPaths) {
            Icon icon = newFileIcon(iconPath.getPath(), iconPath.getDensity());
            icons.add(icon);
        }
        return icons;
    }

    /**
     * get class infos form dex file. currently only class name
     */
    @Override
    public DexClass[] getDexClasses() throws IOException {
        if (this.dexClasses == null) {
            parseDexFiles();
        }
        return this.dexClasses;
    }

    private DexClass[] mergeDexClasses(DexClass[] first, DexClass[] second) {
        DexClass[] result = new DexClass[first.length + second.length];
        arraycopy(first, 0, result, 0, first.length);
        arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private DexClass[] parseDexFile(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            String msg = String.format("Dex file %s not found", path);
            throw new ParserException(msg);
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        DexParser dexParser = new DexParser(buffer);
        return dexParser.parse();
    }

    private void parseDexFiles() throws IOException {
        this.dexClasses = parseDexFile(AndroidConstants.DEX_FILE);
        for (int i = 2; i < 1000; i++) {
            String path = String.format(AndroidConstants.DEX_ADDITIONAL, i);
            try {
                DexClass[] classes = parseDexFile(path);
                this.dexClasses = mergeDexClasses(this.dexClasses, classes);
            } catch (ParserException e) {
                break;
            }
        }
    }

    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        if (resourceTableParsed) {
            return;
        }
        resourceTableParsed = true;
        byte[] data = getFileData(AndroidConstants.RESOURCE_FILE);
        if (data == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            this.locales = Collections.emptySet();
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
        resourceTableParser.parse();
        this.resourceTable = resourceTableParser.getResourceTable();
        this.locales = resourceTableParser.getLocales();
    }

    /**
     * Check apk sign. This method only use apk v1 scheme verifier
     *
     * @deprecated using google official ApkVerifier of apksig lib instead.
     */
    @Deprecated
    public abstract ApkSignStatus verifyApk() throws IOException;

    @Override
    public void close() throws IOException {
        this.apkSigners = null;
        this.resourceTable = null;
        this.iconPaths = null;
    }

    /**
     * The local used to parse apk
     */
    public Locale getPreferredLocale() {
        return preferredLocale;
    }


    /**
     * The locale preferred. Will cause getManifestXml / getApkMeta to return different values.
     * The default value is from os default locale setting.
     */
    public void setPreferredLocale(Locale preferredLocale) {
        if (!Objects.equals(this.preferredLocale, preferredLocale)) {
            this.preferredLocale = preferredLocale;
            this.manifestXml = null;
            this.apkMeta = null;
            this.manifestParsed = false;
        }
    }

    /**
     * Create ApkSignBlockParser for this apk file.
     *
     * @return null if do not have sign block
     */
    protected ByteBuffer findApkSignBlock() throws IOException {
        ByteBuffer buffer = fileData().order(ByteOrder.LITTLE_ENDIAN);
        int len = buffer.limit();

        // first find zip end of central directory entry
        if (len < 22) {
            // should not happen
            throw new RuntimeException("Not zip file");
        }
        int maxEOCDSize = 1024 * 100;
        EOCD eocd = null;
        for (int i = len - 22; i > Math.max(0, len - maxEOCDSize); i--) {
            int v = buffer.getInt(i);
            if (v == EOCD.SIGNATURE) {
                Buffers.position(buffer, i + 4);
                eocd = new EOCD();
                eocd.setDiskNum(Buffers.readUShort(buffer));
                eocd.setCdStartDisk(Buffers.readUShort(buffer));
                eocd.setCdRecordNum(Buffers.readUShort(buffer));
                eocd.setTotalCDRecordNum(Buffers.readUShort(buffer));
                eocd.setCdSize(Buffers.readUInt(buffer));
                eocd.setCdStart(Buffers.readUInt(buffer));
                eocd.setCommentLen(Buffers.readUShort(buffer));
            }
        }

        if (eocd == null) {
            return null;
        }

        int magicStrLen = 16;
        long cdStart = eocd.getCdStart();
        // find apk sign block
        Buffers.position(buffer, cdStart - magicStrLen);
        String magic = Buffers.readAsciiString(buffer, magicStrLen);
        if (!magic.equals(ApkSigningBlock.MAGIC)) {
            return null;
        }
        Buffers.position(buffer, cdStart - 24);
        int blockSize = Unsigned.ensureUInt(buffer.getLong());
        Buffers.position(buffer, cdStart - blockSize - 8);
        long size2 = Unsigned.ensureULong(buffer.getLong());
        if (blockSize != size2) {
            return null;
        }
        // now at the start of signing block
        return Buffers.sliceAndSkip(buffer, blockSize - magicStrLen);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"dexClasses\":").append(Arrays.toString(dexClasses))
                .append(", \"dexClassesAll\":").append(dexClassesAll)
                .append(", \"resourceTableParsed\":").append(resourceTableParsed)
//                .append(", \"resourceTable\":").append(resourceTable)
                .append(", \"locales\":").append(locales)
                .append(", \"manifestParsed\":").append(manifestParsed)
                .append(", \"apkMeta\":").append(apkMeta)
                .append(", \"iconPaths\":").append(iconPaths)
                .append(", \"apkSigners\":").append(apkSigners)
                .append(", \"apkV2Signers\":").append(apkV2Signers)
                .append(", \"assetsList\":").append(assetsList)
                .append(", \"metaInfs\":").append(metaInfs)
                .append(", \"nativeLibsList\":").append(nativeLibsList)
                .append(", \"resList\":").append(resList)
                .append(", \"preferredLocale\":").append(preferredLocale)
                .append('}');
        return sb.toString();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("dexClasses", dexClasses);
        jsonObject.putOpt("dexClassesAll", dexClassesAll);
        jsonObject.putOpt("resourceTable", resourceTable);
        jsonObject.putOpt("locales", locales);
        jsonObject.putOpt("apkMeta", apkMeta.toString());
        jsonObject.putOpt("iconPaths", iconPaths);
        jsonObject.putOpt("apkSigners", apkSigners);
        jsonObject.putOpt("apkV2Signers", apkV2Signers);
        jsonObject.putOpt("assetsList", assetsList);
        jsonObject.putOpt("metaInfs", metaInfs);
        jsonObject.putOpt("nativeLibsList", nativeLibsList);
        jsonObject.putOpt("resList", resList);
        jsonObject.putOpt("preferredLocale", preferredLocale);
        return jsonObject;
    }
}
