package org.syt.parser.apk.parser;

import org.bouncycastle.asn1.cms.CMSAlgorithmProtection;
import org.bouncycastle.internal.asn1.cms.CMSObjectIdentifiers;
import org.syt.parser.apk.bean.CertificateMeta;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parser certificate info using BouncyCastle.
 *
 * @author dongliu
 */
public class BCCertificateParser extends CertificateParser {

    private static final Provider provider = new BouncyCastleProvider();

    public BCCertificateParser(byte[] data) {
        super(data);
    }

    /**
     * get certificate info
     */
    @SuppressWarnings("unchecked")
    public List<CertificateMeta> parse() throws CertificateException {
        CMSSignedData cmsSignedData;
        try {
            cmsSignedData = new CMSSignedData(data);
        } catch (CMSException e) {
            throw new CertificateException(e);
        }
        Store<X509CertificateHolder> certStore = cmsSignedData.getCertificates();
        SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
        Collection<SignerInformation> signers = signerInfos.getSigners();
        List<X509Certificate> certificates = new ArrayList<>();
        for (SignerInformation signer : signers) {
            Collection<X509CertificateHolder> matches = certStore.getMatches(signer.getSID());
            for (X509CertificateHolder holder : matches) {
                certificates.add(new JcaX509CertificateConverter().setProvider(provider).getCertificate(holder));
            }
        }
        return CertificateMetas.from(certificates);
    }

}
