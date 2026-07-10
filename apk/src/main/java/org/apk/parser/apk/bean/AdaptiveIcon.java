package org.apk.parser.apk.bean;

import org.apk.parser.entry.Icon;
import org.apk.parser.entry.IconFace;

import java.io.Serializable;

/**
 * Android adaptive icon, from android 8.0
 */
public class AdaptiveIcon implements IconFace, Serializable {
    private static final long serialVersionUID = 4185750290211529320L;
    private final Icon foreground;
    private final Icon background;

    public AdaptiveIcon(Icon foreground, Icon background) {
        this.foreground = foreground;
        this.background = background;
    }


    /**
     * The foreground icon
     */
    public Icon getForeground() {
        return foreground;
    }

    /**
     * The background icon
     */
    public Icon getBackground() {
        return background;
    }

    @Override
    public String toString() {
        return "AdaptiveIcon{" +
                "foreground=" + foreground +
                ", background=" + background +
                '}';
    }

    /**
     * 优先取前景层，缺失前景层的自适应图标（部分应用只提供背景层）回退到背景层
     */
    private Icon preferredLayer() {
        return foreground != null ? foreground : background;
    }

    @Override
    public boolean isFile() {
        Icon icon = preferredLayer();
        return icon != null && icon.isFile();
    }

    @Override
    public byte[] getData() {
        Icon icon = preferredLayer();
        return icon == null ? null : icon.getData();
    }

    @Override
    public String getPath() {
        Icon icon = preferredLayer();
        return icon == null ? null : icon.getPath();
    }
}
