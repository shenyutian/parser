package org.apk.parser.apk.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * APK 签名信息解析工具：一次性读取 v1（JAR）、v2、v3 各方案的签名（签名 hex + 签名主体 + 发行者），
 * 以及 apk 文件大小、MD5、SHA-256。
 *
 * <p>同时支持从 {@link File}（v1 用 ZIP 签名块、v2/v3 用内存映射）与从 byte[]（v1 用
 * ZIP 签名块、v2/v3 用 {@link ByteBuffer}）两种数据源解析，v2/v3 的签名块查找核心统一基于
 * {@link ByteBuffer} 随机访问，与数据来源解耦。
 */
public final class ApkSignatureUtil {
    private static final int APK_SIGNATURE_SCHEME_V2_BLOCK_ID = 0x7109871a;
    private static final int APK_SIGNATURE_SCHEME_V3_BLOCK_ID = 0xf05368c0;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 0x20676953204b5041L;
    private static final long APK_SIG_BLOCK_MAGIC_HI = 0x3234206b636f6c42L;
    private static final int ZIP_EOCD_REC_MIN_SIZE = 22;
    private static final int ZIP_EOCD_REC_SIG = 0x06054b50;
    private static final int UINT16_MAX_VALUE = 0xffff;

    private ApkSignatureUtil() {
    }

    /**
     * 单个签名证书的信息：签名方案、证书 DER 的 hex 字符串、签名主体（Subject DN）与发行者（Issuer DN）。
     */
    public static final class SignatureInfo {
        /** 签名方案：v1（JAR）、v2、v3。 */
        public final String scheme;
        /** 证书 DER 编码的 hex 字符串。 */
        public final String signature;
        /** 签名主体（证书 Subject DN）。 */
        public final String principal;
        /** 发行者（证书 Issuer DN）。 */
        public final String issuer;

        private SignatureInfo(String scheme, X509Certificate certificate) throws Exception {
            this.scheme = scheme;
            this.signature = bytesToHex(certificate.getEncoded());
            this.principal = certificate.getSubjectX500Principal().getName();
            this.issuer = certificate.getIssuerX500Principal().getName();
        }

        @Override
        public String toString() {
            return "SignatureInfo{scheme=" + scheme
                    + ", principal=" + principal
                    + ", issuer=" + issuer
                    + ", signature=" + signature + '}';
        }
    }

    /**
     * APK 的整体签名信息：文件大小、MD5、SHA-256，以及 v1/v2/v3 各方案的签名列表。
     */
    public static final class ApkSignatureResult {
        /** APK 文件大小（字节）。 */
        public final long apkLength;
        /** APK 文件 MD5（hex 字符串）。 */
        public final String apkMd5;
        /** APK 文件 SHA-256（hex 字符串）。 */
        public final String apkSha256;
        /** v1/v2/v3 各方案的签名信息列表。 */
        public final List<SignatureInfo> signatures;

        private ApkSignatureResult(long apkLength, String apkMd5, String apkSha256, List<SignatureInfo> signatures) {
            this.apkLength = apkLength;
            this.apkMd5 = apkMd5;
            this.apkSha256 = apkSha256;
            this.signatures = signatures;
        }

        @Override
        public String toString() {
            return "ApkSignatureResult{apkLength=" + apkLength
                    + ", apkMd5=" + apkMd5
                    + ", apkSha256=" + apkSha256
                    + ", signatures=" + signatures + '}';
        }
    }

    // ==================== File 入口 ====================

    /**
     * 一次性读取 APK 的全部签名信息：文件大小、MD5、SHA-256，以及 v1（JAR）、v2、v3 各方案的
     * 签名（签名 hex + 签名主体 + 发行者）。某个签名方案不存在时会被跳过，不会抛异常。
     */
    public static ApkSignatureResult readApkSignatureInfo(File apkFile) throws Exception {
        long apkLength = apkFile.length();
        String apkMd5 = fileDigestHex(apkFile, "MD5");
        String apkSha256 = fileDigestHex(apkFile, "SHA-256");
        return new ApkSignatureResult(apkLength, apkMd5, apkSha256, readAllSignatures(apkFile));
    }

    /**
     * 读取 ZIP 容器自身以及容器内 APK 条目的全部签名信息。
     * 容器自身按 v1 解析，内部 APK 继续解析 v1/v2/v3，并按方案和证书去重。
     */
    public static ApkSignatureResult readArchiveSignatureInfo(File archiveFile) throws Exception {
        long archiveLength = archiveFile.length();
        String archiveMd5 = fileDigestHex(archiveFile, "MD5");
        String archiveSha256 = fileDigestHex(archiveFile, "SHA-256");
        List<SignatureInfo> signatures = new ArrayList<>(readAllSignatures(archiveFile));
        Set<String> seen = new HashSet<>();
        for (SignatureInfo signature : signatures) {
            seen.add(signature.scheme + ':' + signature.signature);
        }

        try (ZipFile zipFile = new ZipFile(archiveFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
                    continue;
                }
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    for (SignatureInfo signature : readAllSignatures(readAll(inputStream))) {
                        if (seen.add(signature.scheme + ':' + signature.signature)) {
                            signatures.add(signature);
                        }
                    }
                } catch (Exception ignored) {
                    // 单个 APK 条目损坏时继续解析其他条目。
                }
            }
        }
        return new ApkSignatureResult(archiveLength, archiveMd5, archiveSha256, signatures);
    }

    /**
     * 一次性读取 APK 中 v1（JAR）、v2、v3 全部方案的签名信息（签名 hex + 签名主体）。
     * 某个方案不存在时会被跳过，不会抛异常；三个方案都没有则返回空列表。
     */
    public static List<SignatureInfo> readAllSignatures(File apkFile) throws Exception {
        List<SignatureInfo> result = new ArrayList<>();

        for (X509Certificate certificate : readV1SignatureCertificates(apkFile)) {
            result.add(new SignatureInfo("v1", certificate));
        }

        try (FileChannel channel = FileChannel.open(apkFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer apk = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .order(ByteOrder.LITTLE_ENDIAN);
            collectSchemeSignatures(apk, result);
        } catch (IOException ignored) {
            // 没有 APK Signing Block（仅 v1 签名或未签名）时跳过 v2/v3。
        }

        return result;
    }

    // ==================== byte[] 入口 ====================

    /**
     * 从 apk 字节数组一次性读取全部签名信息：字节长度、MD5、SHA-256，以及 v1/v2/v3 各方案签名。
     */
    public static ApkSignatureResult readApkSignatureInfo(byte[] apkData) throws Exception {
        long apkLength = apkData.length;
        String apkMd5 = bytesDigestHex(apkData, "MD5");
        String apkSha256 = bytesDigestHex(apkData, "SHA-256");
        return new ApkSignatureResult(apkLength, apkMd5, apkSha256, readAllSignatures(apkData));
    }

    /**
     * 从 apk 字节数组读取 v1（JAR）、v2、v3 全部方案的签名信息。某个方案不存在时会被跳过，不会抛异常。
     */
    public static List<SignatureInfo> readAllSignatures(byte[] apkData) throws Exception {
        List<SignatureInfo> result = new ArrayList<>();

        for (X509Certificate certificate : readV1SignatureCertificates(apkData)) {
            result.add(new SignatureInfo("v1", certificate));
        }

        try {
            ByteBuffer apk = ByteBuffer.wrap(apkData).order(ByteOrder.LITTLE_ENDIAN);
            collectSchemeSignatures(apk, result);
        } catch (IOException ignored) {
            // 没有 APK Signing Block（仅 v1 签名或未签名）时跳过 v2/v3。
        }

        return result;
    }

    // ==================== v2/v3 核心（基于 ByteBuffer） ====================

    private static void collectSchemeSignatures(ByteBuffer apk, List<SignatureInfo> result) throws Exception {
        long centralDirectoryOffset = findCentralDirectoryOffset(apk);
        ByteBuffer signingBlock = findApkSigningBlock(apk, centralDirectoryOffset);

        for (X509Certificate certificate : readSchemeCertificates(signingBlock, APK_SIGNATURE_SCHEME_V2_BLOCK_ID)) {
            result.add(new SignatureInfo("v2", certificate));
        }
        for (X509Certificate certificate : readSchemeCertificates(signingBlock, APK_SIGNATURE_SCHEME_V3_BLOCK_ID)) {
            result.add(new SignatureInfo("v3", certificate));
        }
    }

    private static String fileDigestHex(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesDigestHex(byte[] data, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.update(data);
        return bytesToHex(digest.digest());
    }

    private static List<X509Certificate> readSchemeCertificates(ByteBuffer signingBlock, int schemeId) throws Exception {
        ByteBuffer schemeBlock = findIdValue(signingBlock, schemeId);
        if (schemeBlock == null) {
            return new ArrayList<>();
        }
        return readCertificatesFromSignerBlock(schemeBlock);
    }

    // ==================== v1（JAR）签名 ====================

    private static List<X509Certificate> readV1SignatureCertificates(File apkFile) throws Exception {
        // AAB 的 META-INF 签名可能不是按 JarFile 的校验顺序排列，直接解析 PKCS#7
        // 签名块比读取首个内容条目更稳定，也能收集多个签名证书。
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(apkFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName().toUpperCase(Locale.ROOT);
                if (entry.isDirectory() || !name.startsWith("META-INF/")
                        || !(name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))) {
                    continue;
                }

                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    byte[] block = readAll(inputStream);
                    for (Certificate certificate : certificateFactory.generateCertificates(
                            new ByteArrayInputStream(block))) {
                        if (certificate instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) certificate;
                            if (seen.add(bytesToHex(x509.getEncoded()))) {
                                result.add(x509);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 非法签名块不影响其他签名块的解析。
                }
            }
        }
        return result;
    }

    private static List<X509Certificate> readV1SignatureCertificates(byte[] apkData) throws Exception {
        // JarInputStream 依赖流内条目顺序，签名文件排在内容之后时取不到证书；
        // 这里直接扫描 META-INF 下的 PKCS#7 签名块（.RSA/.DSA/.EC）并解析出证书，可靠且与顺序无关。
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(apkData))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName().toUpperCase(Locale.ROOT);
                if (!name.startsWith("META-INF/")
                        || !(name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))) {
                    continue;
                }

                byte[] block = readAll(zipInputStream);
                try {
                    for (Certificate certificate : certificateFactory.generateCertificates(new ByteArrayInputStream(block))) {
                        if (certificate instanceof X509Certificate) {
                            X509Certificate x509 = (X509Certificate) certificate;
                            if (seen.add(bytesToHex(x509.getEncoded()))) {
                                result.add(x509);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 非法/无法解析的签名块跳过。
                }
            }
        }

        return result;
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    // ==================== ZIP / APK Signing Block 定位（ByteBuffer 版） ====================

    private static long findCentralDirectoryOffset(ByteBuffer apk) throws IOException {
        long fileSize = apk.limit();
        if (fileSize < ZIP_EOCD_REC_MIN_SIZE) {
            throw new IOException("APK too small for ZIP End Of Central Directory");
        }
        long maxCommentLength = Math.min(fileSize - ZIP_EOCD_REC_MIN_SIZE, UINT16_MAX_VALUE);
        int searchStart = (int) (fileSize - ZIP_EOCD_REC_MIN_SIZE - maxCommentLength);

        for (int offset = (int) (fileSize - ZIP_EOCD_REC_MIN_SIZE); offset >= searchStart; offset--) {
            if (apk.getInt(offset) != ZIP_EOCD_REC_SIG) {
                continue;
            }

            int commentLength = getUnsignedShort(apk, offset + 20);
            if (offset + ZIP_EOCD_REC_MIN_SIZE + commentLength == fileSize) {
                return getUnsignedInt(apk, offset + 16);
            }
        }

        throw new IOException("ZIP End Of Central Directory not found");
    }

    private static ByteBuffer findApkSigningBlock(ByteBuffer apk, long centralDirectoryOffset) throws IOException {
        if (centralDirectoryOffset < 32) {
            throw new IOException("APK too small for APK Signing Block");
        }

        int footerStart = (int) (centralDirectoryOffset - 24);
        long blockSizeInFooter = apk.getLong(footerStart);
        long magicLo = apk.getLong(footerStart + 8);
        long magicHi = apk.getLong(footerStart + 16);
        if (magicLo != APK_SIG_BLOCK_MAGIC_LO || magicHi != APK_SIG_BLOCK_MAGIC_HI) {
            throw new IOException("APK Signing Block magic not found");
        }
        if (blockSizeInFooter < 24 || blockSizeInFooter > Integer.MAX_VALUE - 8L) {
            throw new IOException("Invalid APK Signing Block size: " + blockSizeInFooter);
        }

        int totalSize = (int) (blockSizeInFooter + 8);
        long blockOffset = centralDirectoryOffset - totalSize;
        if (blockOffset < 0) {
            throw new IOException("Invalid APK Signing Block offset");
        }

        long blockSizeInHeader = apk.getLong((int) blockOffset);
        if (blockSizeInHeader != blockSizeInFooter) {
            throw new IOException("APK Signing Block sizes do not match");
        }

        // 跳过起始的 8 字节 size 字段，截止到结尾 size(8) + magic(16) 之前，即 id-value 对区域。
        ByteBuffer pairs = apk.duplicate();
        pairs.position((int) blockOffset + 8);
        pairs.limit((int) blockOffset + totalSize - 24);
        return pairs.slice().order(ByteOrder.LITTLE_ENDIAN);
    }

    private static ByteBuffer findIdValue(ByteBuffer pairs, int id) throws IOException {
        ByteBuffer buffer = pairs.slice().order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.hasRemaining()) {
            if (buffer.remaining() < 8) {
                throw new IOException("APK Signing Block pair size missing");
            }

            long pairSize = buffer.getLong();
            if (pairSize < 4 || pairSize > buffer.remaining()) {
                throw new IOException("Invalid APK Signing Block pair size: " + pairSize);
            }

            int pairStart = buffer.position();
            int pairId = buffer.getInt();
            int valueSize = (int) pairSize - 4;
            if (pairId == id) {
                ByteBuffer value = slice(buffer, valueSize);
                value.order(ByteOrder.LITTLE_ENDIAN);
                return value;
            }
            buffer.position(pairStart + (int) pairSize);
        }

        return null;
    }

    private static List<X509Certificate> readCertificatesFromSignerBlock(ByteBuffer schemeBlock) throws Exception {
        ByteBuffer signers = schemeBlock.slice().order(ByteOrder.LITTLE_ENDIAN);
        List<X509Certificate> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        while (signers.hasRemaining()) {
            ByteBuffer signer = getLengthPrefixedSlice(signers);
            collectCertificates(signer, certificateFactory, result, seen, 0);
        }

        return result;
    }

    private static void collectCertificates(
            ByteBuffer buffer,
            CertificateFactory certificateFactory,
            List<X509Certificate> result,
            Set<String> seen,
            int depth
    ) throws Exception {
        if (depth > 8) {
            return;
        }

        ByteBuffer candidate = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        addIfX509Certificate(candidate, certificateFactory, result, seen);

        ByteBuffer fields = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        while (fields.remaining() >= 4) {
            int length = fields.getInt();
            if (length < 0 || length > fields.remaining()) {
                return;
            }

            ByteBuffer field = slice(fields, length);
            collectCertificates(field, certificateFactory, result, seen, depth + 1);
        }
    }

    private static void addIfX509Certificate(
            ByteBuffer candidate,
            CertificateFactory certificateFactory,
            List<X509Certificate> result,
            Set<String> seen
    ) throws Exception {
        if (!candidate.hasRemaining() || (candidate.get(candidate.position()) & 0xff) != 0x30) {
            return;
        }

        byte[] encoded = new byte[candidate.remaining()];
        candidate.get(encoded);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(encoded);
        try {
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            if (inputStream.available() == 0) {
                String signature = bytesToHex(certificate.getEncoded());
                if (seen.add(signature)) {
                    result.add(certificate);
                }
            }
        } catch (Exception ignored) {
            // 并非每个长度前缀字段都是证书，跳过后继续扫描嵌套字段。
        }
    }

    private static ByteBuffer getLengthPrefixedSlice(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < 4) {
            throw new IOException("Length-prefixed field is missing length");
        }

        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining()) {
            throw new IOException("Invalid length-prefixed field size: " + length);
        }

        return slice(buffer, length);
    }

    private static ByteBuffer slice(ByteBuffer buffer, int size) {
        int originalLimit = buffer.limit();
        int start = buffer.position();
        buffer.limit(start + size);
        ByteBuffer result = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        buffer.limit(originalLimit);
        buffer.position(start + size);
        return result;
    }

    private static int getUnsignedShort(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset) & 0xffff;
    }

    private static long getUnsignedInt(ByteBuffer buffer, int offset) {
        return buffer.getInt(offset) & 0xffffffffL;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0f];
        }

        return new String(hexChars);
    }
}
