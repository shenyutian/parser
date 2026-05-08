package org.apk.parser.apk.struct.resource;

import org.apk.parser.apk.struct.ChunkHeader;
import org.apk.parser.apk.struct.ChunkType;

public class NullHeader extends ChunkHeader {
    public NullHeader(int headerSize, int chunkSize) {
        super(ChunkType.NULL, headerSize, chunkSize);
    }
}
