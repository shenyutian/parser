import org.apk.parser.apk.bean.AdaptiveIcon;
import org.apk.parser.entry.Icon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 部分应用的自适应图标只提供背景层、不提供前景层（如 Goodville，
 * com.goodville.goodgame），此时 AdaptiveIconParser 解出的 foreground 为 null。
 * AdaptiveIcon 的各访问方法此前无条件调用 foreground.xxx()，导致空指针；
 * 回归验证：foreground 缺失时应回退到 background，两者都缺失时返回 null 而非抛异常。
 */
public class AdaptiveIconTest {

    @Test
    public void testFallbackToBackgroundWhenForegroundMissing() {
        Icon background = new Icon("res/background.png", 0, new byte[]{1, 2, 3});
        AdaptiveIcon icon = new AdaptiveIcon(null, background);

        assertArrayEquals(new byte[]{1, 2, 3}, icon.getData());
        assertEquals("res/background.png", icon.getPath());
        assertTrue(icon.isFile());
    }

    @Test
    public void testNullWhenBothLayersMissing() {
        AdaptiveIcon icon = new AdaptiveIcon(null, null);

        assertNull(icon.getData());
        assertNull(icon.getPath());
        assertFalse(icon.isFile());
    }

    @Test
    public void testPrefersForegroundWhenBothPresent() {
        Icon foreground = new Icon("res/foreground.png", 0, new byte[]{9});
        Icon background = new Icon("res/background.png", 0, new byte[]{1});
        AdaptiveIcon icon = new AdaptiveIcon(foreground, background);

        assertArrayEquals(new byte[]{9}, icon.getData());
        assertEquals("res/foreground.png", icon.getPath());
    }
}
