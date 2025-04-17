import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.syt.parser.aab.AabFile;
import org.syt.parser.entry.ApkMeta;
import org.syt.parser.entry.IconFace;
import org.syt.parser.json.JSONException;
import org.syt.parser.json.JSONObject;
import org.syt.parser.log.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
    void getApkMeta2() throws IOException {
        String path = getClass().getClassLoader().getResource("qr.aab").getPath();
        AabFile aabFile = new AabFile(path);
        ApkMeta ApkMeta = aabFile.getApkMeta();
        Log.d("ApkMeta = " + ApkMeta);
    }

    @Test
    void getApkInfo() throws IOException, JSONException {
        String path = getClass().getClassLoader().getResource("BlockBreakDoge.aab").getPath();
        AabFile aabFile = new AabFile(path);
        JSONObject info = aabFile.getInfo();
        Log.d(info.toString(4));
    }

    @Test
    void getICons() throws Exception {
        String path = getClass().getClassLoader().getResource("BlockBreakDoge.aab").getPath();
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