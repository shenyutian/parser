package parser;

import org.junit.jupiter.api.Test;
import org.syt.parser.apk.bean.CertificateMeta;
import org.syt.parser.apk.parser.BCCertificateParser;
import org.syt.parser.apk.parser.CertificateParser;
import org.syt.parser.apk.utils.Inputs;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BCCertificateParserTest {

    @Test
    public void parse() throws IOException, CertificateException {
        byte[] data = Inputs.readAllAndClose(getClass().getResourceAsStream("/sign/63_CERT.RSA"));
        CertificateParser parser = new BCCertificateParser(data);
        List<CertificateMeta> certificateMetas = parser.parse();
        assertEquals("SHA1WITHRSA", certificateMetas.get(0).getSignAlgorithm());


        data = Inputs.readAllAndClose(getClass().getResourceAsStream("/sign/gmail_CERT.RSA"));
        parser = new BCCertificateParser(data);
        certificateMetas = parser.parse();
        assertEquals(1, certificateMetas.size());
        CertificateMeta certificateMeta = certificateMetas.get(0);
        assertEquals("MD5WITHRSA", certificateMeta.getSignAlgorithm());
        assertEquals("9decc0608f773ad1f4a017c02598d80c", certificateMeta.getCertBase64Md5());
    }
}