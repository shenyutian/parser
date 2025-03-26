package org.syt.parser.apk.bean;

import org.syt.parser.apk.bean.CertificateMeta;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * ApkSignV1 certificate file.
 */
public class ApkSigner {
    /**
     * The cert file path in apk file
     */
    private String path;
    /**
     * The meta info of certificate contained in this cert file.
     */
    private List<CertificateMeta> certificateMetas;

    public ApkSigner(String path, List<CertificateMeta> certificateMetas) {
        this.path = path;
        this.certificateMetas = requireNonNull(certificateMetas);
    }

    public String getPath() {
        return path;
    }

    public List<CertificateMeta> getCertificateMetas() {
        return certificateMetas;
    }

    @Override
    public String toString() {
        return "ApkSigner{" +
                "path='" + path + '\'' +
                ", certificateMetas=" + certificateMetas +
                '}';
    }
}
