import org.junit.jupiter.api.Test;
import org.apk.parser.apk.ApksFile;
import org.apk.parser.entry.ApkMeta;

import java.io.IOException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ApksFile 需要处理多种“容器里套多个 apk”的格式：标准 .apks（bundletool 产物）、
 * .xapk、以及内部仅有 apk 条目的普通 .zip。这些容器内往往包含 base 分片和若干
 * language/density config 分片，manifest 必须取自 base 分片，否则会读到 config
 * 分片残缺的包名/缺失 label（回归测试见 ApksFile.getBaseApkFile 的排序修复）。
 */
public class ApksFileMultiFormatTest {

    @Test
    public void testZipContainer() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/sample.zip").getPath();
        try (ApksFile apksFile = new ApksFile(path)) {
            apksFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apksFile.getApkMeta();
            assertEquals("com.google.android.networkstack", apkMeta.getPackageName());
            assertEquals("NetworkStack", apkMeta.getLabel());
        }
    }

    @Test
    public void testXapkContainer() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/sample.xapk").getPath();
        try (ApksFile apksFile = new ApksFile(path)) {
            apksFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apksFile.getApkMeta();
            assertEquals("com.google.android.networkstack", apkMeta.getPackageName());
            assertEquals("NetworkStack", apkMeta.getLabel());
        }
    }

    @Test
    public void testApksContainerWithBaseMasterSplit() throws IOException {
        // splits/base-master.apk + splits/config.en.apk：模拟 bundletool 的 splits 命名
        String path = getClass().getClassLoader().getResource("apks/sample.apks").getPath();
        try (ApksFile apksFile = new ApksFile(path)) {
            apksFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apksFile.getApkMeta();
            assertEquals("com.google.android.networkstack", apkMeta.getPackageName());
            assertEquals("NetworkStack", apkMeta.getLabel());
        }
    }
}
