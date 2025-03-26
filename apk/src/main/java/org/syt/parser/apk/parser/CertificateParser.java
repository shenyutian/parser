package org.syt.parser.apk.parser;

import org.syt.parser.apk.ApkParsers;
import org.syt.parser.apk.bean.CertificateMeta;
import org.syt.parser.apk.parser.BCCertificateParser;
import org.syt.parser.apk.parser.JSSECertificateParser;

import java.security.cert.CertificateException;
import java.util.List;

/**
 * Parser certificate info.
 * One apk may have multi certificates(certificate chain).
 *
 * @author dongliu
 */
public abstract class CertificateParser {

    protected final byte[] data;

    public CertificateParser(byte[] data) {
        this.data = data;
    }

    public static CertificateParser getInstance(byte[] data) {
        if (ApkParsers.useBouncyCastle()) {
            return new BCCertificateParser(data);
        }
        return new JSSECertificateParser(data);
    }

    /**
     * get certificate info
     */
    public abstract List<CertificateMeta> parse() throws CertificateException;

}
