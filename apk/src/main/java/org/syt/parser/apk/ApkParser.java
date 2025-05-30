package org.syt.parser.apk;

import org.syt.parser.apk.ApkFile;

import java.io.File;
import java.io.IOException;


/**
 * ApkParse and result holder.
 * This class is not thread-safe.
 *
 * @author dongliu
 * @deprecated use {@link org.syt.parser.apk.ApkFile} instead
 */
@Deprecated
public class ApkParser extends ApkFile {

    public ApkParser(File apkFile) throws IOException {
        super(apkFile);
    }

    public ApkParser(String filePath) throws IOException {
        super(filePath);
    }
}
