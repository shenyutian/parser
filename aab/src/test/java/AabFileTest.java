import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.apk.parser.aab.AabFile;
import org.apk.parser.entry.ApkMeta;
import org.apk.parser.entry.IconFace;
import org.apk.parser.json.JSONArray;
import org.apk.parser.json.JSONException;
import org.apk.parser.json.JSONObject;
import org.apk.parser.log.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apk.parser.apk.utils.ApkSignatureUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * zhulei 2025/3/26-11:58
 */
class AabFileTest {

    @BeforeAll
    public static void setUpClass() {
        // 在每个测试类执行前运行的代码
        Log.plant(new Log.DebugTree());
    }


    @Test
    void getManifestXml() throws IOException {
        String path = getClass().getClassLoader().getResource("test.aab").getPath();
        AabFile aabFile = new AabFile(path);
        ApkMeta ApkMeta = aabFile.getApkMeta();
        System.out.println(ApkMeta.getLauncherAll());
    }

    @Test
    void getApkMeta() throws IOException {
        String path = getClass().getClassLoader().getResource("test.aab").getPath();
        AabFile aabFile = new AabFile(path);
        ApkMeta ApkMeta = aabFile.getApkMeta();
        Log.d("ApkMeta = " + ApkMeta);
    }

    @Test
    void getApkMeta2() throws IOException {
        String path = getClass().getClassLoader().getResource("qr.aab").getPath();
        AabFile aabFile = new AabFile(path);
        ApkMeta ApkMeta = aabFile.getApkMeta();
        Log.d("ApkMeta = " + ApkMeta);
        JSONObject apkInfo = aabFile.getInfo();
        Log.d("apkInfo = " + apkInfo);
    }

    @Test
    void getApkInfo() throws IOException, JSONException {
        String path = getClass().getClassLoader().getResource("debug.aab").getPath();
        AabFile aabFile = new AabFile(path);
        JSONObject info = aabFile.getInfo();
        Log.d(info.toString(4));
    }

    @Test
    void getSignedAabInfo() throws Exception {
        String path = getClass().getClassLoader().getResource("qr.aab").getPath();
        AabFile aabFile = new AabFile(path);

        JSONObject info = aabFile.getInfo();
        assertTrue(info.has("aabSize"));
        assertTrue(info.has("aabFileMd5"));
        assertTrue(info.has("aabFileSha256"));
        assertTrue(info.has("sign"));
        assertTrue(info.has("SHA1"));
        assertTrue(info.has("SHA256"));
        assertTrue(info.has("fbkey"));

        JSONObject signatureInfo = info.optJSONObject("signatureInfo");
        assertNotNull(signatureInfo);
        assertEquals(info.optString("aabFileMd5"), signatureInfo.optString("apkMd5"));
        assertEquals(info.optString("aabFileSha256"), signatureInfo.optString("apkSha256"));

        JSONArray signatures = signatureInfo.optJSONArray("signatures");
        assertNotNull(signatures);
        assertTrue(signatures.length() > 0);
        assertEquals("v1", signatures.getJSONObject(0).optString("scheme"));
    }

    @Test
    void getUnsignedAabInfo() throws Exception {
        String path = getClass().getClassLoader().getResource("debug.aab").getPath();
        AabFile aabFile = new AabFile(path);

        JSONObject info = aabFile.getInfo();
        assertTrue(info.has("aabSize"));
        assertTrue(info.has("aabFileMd5"));
        assertTrue(info.has("aabFileSha256"));
        assertFalse(info.has("sign"));

        JSONObject signatureInfo = info.optJSONObject("signatureInfo");
        assertNotNull(signatureInfo);
        JSONArray signatures = signatureInfo.optJSONArray("signatures");
        assertNotNull(signatures);
        assertEquals(0, signatures.length());
    }

    @Test
    void getEmbeddedApkSignatures() throws Exception {
        Path archive = Files.createTempFile("parser-signature-", ".aab");
        try {
            Path apk = Path.of(System.getProperty("user.dir"))
                    .getParent()
                    .resolve("apk/src/test/resources/apks/app-debug.apk");
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive));
                 InputStream input = Files.newInputStream(apk)) {
                output.putNextEntry(new ZipEntry("base.apk"));
                input.transferTo(output);
                output.closeEntry();
            }

            ApkSignatureUtil.ApkSignatureResult result =
                    ApkSignatureUtil.readArchiveSignatureInfo(archive.toFile());
            Set<String> schemes = new HashSet<>();
            for (ApkSignatureUtil.SignatureInfo signature : result.signatures) {
                schemes.add(signature.scheme);
            }
            assertTrue(schemes.contains("v1") || schemes.contains("v2") || schemes.contains("v3"));
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    @Test
    void getICons() throws Exception {
        String path = getClass().getClassLoader().getResource("debug.aab").getPath();
        AabFile aabFile = new AabFile(path);
        for (IconFace icon : aabFile.getAllIcons()) {
            Log.d(icon.toString());
            try {
                // 将字节数组转换为BufferedImage
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(icon.getData()));

                // 创建一个JFrame来显示图片
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(image.getWidth(), image.getHeight());

                // 创建一个JLabel来显示图片
                JLabel label = new JLabel(new ImageIcon(image));
                frame.add(label);

                // 显示窗口
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
