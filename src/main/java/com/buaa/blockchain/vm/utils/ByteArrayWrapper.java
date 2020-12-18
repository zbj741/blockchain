package com.buaa.blockchain.vm.utils;

import java.util.Arrays;

public class ByteArrayWrapper {

    private final byte[] bytes;

    public ByteArrayWrapper(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getData() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(bytes, ((ByteArrayWrapper) o).bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return HexUtil.toHexString(bytes);
    }
}
