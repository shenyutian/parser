package org.syt.parser.apk.struct.signingv2;

public class Digest {
    private int algorithmID;
    private byte[] value;

    public Digest(int algorithmID, byte[] value) {
        this.algorithmID = algorithmID;
        this.value = value;
    }

    public int getAlgorithmID() {
        return algorithmID;
    }

    public byte[] getValue() {
        return value;
    }
}
