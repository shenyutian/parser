package org.syt.parser.apk;

import org.syt.parser.apk.ByteArrayApkFile;

/**
 * Parse apk file from byte array.
 * This class is not thread-safe.
 *
 * @author Liu Dong
 * @deprecated using {@link org.syt.parser.apk.ByteArrayApkFile} instead
 */
@Deprecated
public class ByteArrayApkParser extends ByteArrayApkFile {

    public ByteArrayApkParser(byte[] apkData) {
        super(apkData);
    }
}
