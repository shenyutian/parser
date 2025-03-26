package parser;

import org.junit.jupiter.api.Test;
import org.syt.parser.apk.utils.Inputs;
import org.syt.parser.apk.parser.ApkSignBlockParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;

public class ApkSignBlockParserTest {

    @Test
    public void parse() throws IOException, CertificateException {
        byte[] bytes = Inputs.readAllAndClose(getClass().getResourceAsStream("/sign/gmail_sign_block"));
        ApkSignBlockParser parser = new ApkSignBlockParser(ByteBuffer.wrap(bytes));
        parser.parse();
    }
}