package com.buaa.blockchain.datasource;

import java.util.Map;
import java.util.Set;

public interface KeyValueDataSource extends DataSource{
    byte[] get(byte[] key);

    byte[] put(byte[] key, byte[] value);

    void delete(byte[] key);

    Set<byte[]> keys();

    void updateBatch(Map<byte[], byte[]> rows);

}
