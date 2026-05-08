package org.apk.parser.apk.struct.xml;

import org.apk.parser.apk.struct.ChunkHeader;

/**
 * Null header.
 *
 * @author dongliu
 */
public class NullHeader extends ChunkHeader {
    public NullHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
