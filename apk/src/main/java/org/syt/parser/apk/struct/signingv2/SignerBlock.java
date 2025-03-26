package org.syt.parser.apk.struct.signingv2;

import org.syt.parser.apk.struct.signingv2.Digest;
import org.syt.parser.apk.struct.signingv2.Signature;

import java.security.cert.X509Certificate;
import java.util.List;

public class SignerBlock {
    private List<org.syt.parser.apk.struct.signingv2.Digest> digests;
    private List<X509Certificate> certificates;
    private List<org.syt.parser.apk.struct.signingv2.Signature> signatures;

    public SignerBlock(List<org.syt.parser.apk.struct.signingv2.Digest> digests, List<X509Certificate> certificates, List<org.syt.parser.apk.struct.signingv2.Signature> signatures) {
        this.digests = digests;
        this.certificates = certificates;
        this.signatures = signatures;
    }

    public List<Digest> getDigests() {
        return digests;
    }

    public List<X509Certificate> getCertificates() {
        return certificates;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }
}
