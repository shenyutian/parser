package org.apk.parser.apk.struct.xml;

import org.apk.parser.apk.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class XmlResourceMapHeader extends ChunkHeader {
    public XmlResourceMapHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
