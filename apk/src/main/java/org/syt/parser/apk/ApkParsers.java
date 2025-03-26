package org.syt.parser.apk;

import org.syt.parser.entry.ApkMeta;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Convenient utils method for parse apk file
 *
 * @author Liu Dong
 */
public class ApkParsers {

    private static boolean useBouncyCastle;

    public static boolean useBouncyCastle() {
        return useBouncyCastle;
    }

    /**
     * Use BouncyCastle instead of JSSE to parse X509 certificate.
     * If want to use BouncyCastle, you will also need to add bcprov and bcpkix lib to your project.
     */
    public static void useBouncyCastle(boolean useBouncyCastle) {
        ApkParsers.useBouncyCastle = useBouncyCastle;
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(String apkFilePath) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(apkFilePath)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(File file) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(file)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(byte[] apkData) throws IOException {
        try (org.syt.parser.apk.ByteArrayApkFile apkFile = new org.syt.parser.apk.ByteArrayApkFile(apkData)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file, with locale
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(String apkFilePath, Locale locale) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(apkFilePath)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(File file, Locale locale) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(file)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(byte[] apkData, Locale locale) throws IOException {
        try (org.syt.parser.apk.ByteArrayApkFile apkFile = new org.syt.parser.apk.ByteArrayApkFile(apkData)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(String apkFilePath) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(apkFilePath)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(File file) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(file)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(byte[] apkData) throws IOException {
        try (org.syt.parser.apk.ByteArrayApkFile apkFile = new org.syt.parser.apk.ByteArrayApkFile(apkData)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(String apkFilePath, Locale locale) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new org.syt.parser.apk.ApkFile(apkFilePath)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(File file, Locale locale) throws IOException {
        try (org.syt.parser.apk.ApkFile apkFile = new ApkFile(file)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(byte[] apkData, Locale locale) throws IOException {
        try (org.syt.parser.apk.ByteArrayApkFile apkFile = new ByteArrayApkFile(apkData)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }
}
