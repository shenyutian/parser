import org.junit.jupiter.api.Test;
import org.syt.parser.apk.bean.ApkSigner;
import org.syt.parser.apk.bean.CertificateMeta;
import org.syt.parser.apk.ApkFile;
import org.syt.parser.entry.ApkMeta;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ApkFileTest {

    @Test
    public void testParserMeta() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/Twitter_v7.93.2.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertEquals("Twitter", apkMeta.getLabel());
        }
    }

    @Test
    public void testParserMeta2() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/test.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println("apkMeta = " + apkMeta.getLauncher());
            System.out.println("apkMeta = " + apkMeta.getLauncherAll());
            assertEquals("life-saving line", apkMeta.getLabel());
        }
    }

    @Test
    public void testParserMeta3() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/ESFile.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            System.out.println("apkMeta = " + apkFile.getManifestXml());
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println("apkMeta.getActivities() = " + apkMeta.getActivities());
            assertEquals("ES File Explorer", apkMeta.getLabel());
        }
    }

    @Test
    public void testParserMeta_Type_0204() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/NetworkStack_210000000.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertEquals("NetworkStack", apkMeta.getLabel());
        }
    }

    @Test
    public void testGetSignature() throws IOException, CertificateException {
        String path = getClass().getClassLoader().getResource("apks/Twitter_v7.93.2.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            List<ApkSigner> apkSingers = apkFile.getApkSingers();
            assertEquals(1, apkSingers.size());
            ApkSigner apkSigner = apkSingers.get(0);
            assertEquals("META-INF/CERT.RSA", apkSigner.getPath());
            List<CertificateMeta> certificateMetas = apkSigner.getCertificateMetas();
            assertEquals(1, certificateMetas.size());
            CertificateMeta certificateMeta = certificateMetas.get(0);
            assertEquals("69ee076cc84f4d94802d61907b07525f", certificateMeta.getCertMd5());
        }
    }

    @Test
    public void testAppIsNotDebuggable() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/app-release.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertFalse(apkMeta.getApplication().getDebuggable());
        }
    }

    @Test
    public void testAppIsDebuggable() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/app-debug.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertTrue(apkMeta.getApplication().getDebuggable());
        }
    }

    @Test
    public void testLauncher() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/launcher.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertEquals("com.unity3d.player.UnityPlayerActivity", apkMeta.getLauncher().getName());
        }
    }
}
