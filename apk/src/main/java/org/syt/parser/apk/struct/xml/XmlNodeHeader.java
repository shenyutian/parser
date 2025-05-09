package org.syt.parser.apk.struct.xml;

import org.syt.parser.apk.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class XmlNodeHeader extends ChunkHeader {
    // Line number in original source file at which this element appeared.
    private  int lineNum;
    // Optional XML comment string pool ref, -1 if none
    private int commentRef;

    public XmlNodeHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getCommentRef() {
        return commentRef;
    }

    public void setCommentRef(int commentRef) {
        this.commentRef = commentRef;
    }
}
