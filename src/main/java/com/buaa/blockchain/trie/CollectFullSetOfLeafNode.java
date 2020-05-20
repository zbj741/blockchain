package com.buaa.blockchain.trie;

import java.util.ArrayList;
import java.util.List;

public class CollectFullSetOfLeafNode implements TrieImpl.SimpleScanAction {
    List<Values> nodes = new ArrayList<Values>();

    @Override
    public void doOnNode(Values node) {
        nodes.add(node);
    }

    public List<Values> getCollectedNodes() {
        return nodes;
    }
}