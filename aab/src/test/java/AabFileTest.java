import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.syt.parser.aab.AabFile;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;

import java.io.IOException;

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
    void getApkInfo() throws IOException, JSONException {
        String path = getClass().getClassLoader().getResource("qr.aab").getPath();
        AabFile aabFile = new AabFile(path);
        JSONObject info = aabFile.getInfo();
        Log.d(info.toString(4));
    }

}