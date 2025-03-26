package org.syt.parser.apk.bean;

import org.syt.parser.entry.BaseBean;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * basic certificate info.
 *
 * @author dongliu
 */
public class CertificateMeta implements BaseBean {

    /**
     * the sign algorithm name
     */
    private final String signAlgorithm;

    /**
     * the signature algorithm OID string.
     * An OID is represented by a set of non-negative whole numbers separated by periods.
     * For example, the string "1.2.840.10040.4.3" identifies the SHA-1 with DSA signature algorithm defined in
     * <a href="http://www.ietf.org/rfc/rfc3279.txt">
     * RFC 3279: Algorithms and Identifiers for the Internet X.509 Public Key Infrastructure Certificate and CRL Profile
     * </a>.
     */
    private final String signAlgorithmOID;

    /**
     * the start date of the validity period.
     */
    private final Date startDate;

    /**
     * the end date of the validity period.
     */
    private final Date endDate;

    /**
     * certificate binary data.
     */
    private final byte[] data;

    /**
     * first use base64 to encode certificate binary data, and then calculate md5 of base64b string.
     * some programs use this as the certMd5 of certificate
     */
    private final String certBase64Md5;

    /**
     * use md5 to calculate certificate's certMd5.
     */
    private final String certMd5;

    private final String subject;

    private final X509Certificate x509Certificate;

    public CertificateMeta(String signAlgorithm, String signAlgorithmOID, Date startDate, Date endDate,
                           byte[] data, String certBase64Md5, String certMd5, String subject, X509Certificate x509Certificate) {
        this.signAlgorithm = signAlgorithm;
        this.signAlgorithmOID = signAlgorithmOID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.data = data;
        this.certBase64Md5 = certBase64Md5;
        this.certMd5 = certMd5;
        this.subject = subject;
        this.x509Certificate = x509Certificate;
    }

    @Override
    public String getFileName() {
        return toString();
    }

    public byte[] getData() {
        return data;
    }

    public String getCertBase64Md5() {
        return certBase64Md5;
    }

    public String getCertMd5() {
        return certMd5;
    }

    public String getSignAlgorithm() {
        return signAlgorithm;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getSignAlgorithmOID() {
        return signAlgorithmOID;
    }

    public String getSubject() {
        return subject;
    }

    public X509Certificate getX509Certificate() {
        return x509Certificate;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "CertificateMeta{signAlgorithm=" + signAlgorithm + ", " +
                "certBase64Md5=" + certBase64Md5 + ", " +
                "startDate=" + df.format(startDate) + ", " + "endDate=" + df.format(endDate) + ", subject=" + subject + "}";
    }
}

