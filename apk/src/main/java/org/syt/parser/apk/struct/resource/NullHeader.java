package org.syt.parser.apk.struct.resource;

import org.syt.parser.apk.struct.ChunkHeader;
import org.syt.parser.apk.struct.ChunkType;

public class NullHeader extends ChunkHeader {
    public NullHeader(int headerSize, int chunkSize) {
        super(ChunkType.NULL, headerSize, chunkSize);
    }
}
