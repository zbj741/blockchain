package com.buaa.blockchain.trie;

import com.buaa.blockchain.utils.ByteArrayWrapper;

import java.util.HashSet;
import java.util.Set;


public class CollectFullSetOfNodes implements TrieImpl.ScanAction {
    Set<ByteArrayWrapper> nodes = new HashSet<>();

    @Override
    public void doOnNode(byte[] hash, Values node) {
        nodes.add(new ByteArrayWrapper(hash));
    }

    public Set<ByteArrayWrapper> getCollectedHashes() {
        return nodes;
    }
}
