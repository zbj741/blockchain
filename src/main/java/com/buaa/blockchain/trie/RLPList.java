package com.buaa.blockchain.trie;

import com.buaa.blockchain.utils.ByteUtil;

import java.util.ArrayList;


public class RLPList extends ArrayList<com.buaa.blockchain.trie.RLPElement> implements com.buaa.blockchain.trie.RLPElement {

    byte[] rlpData;

    public void setRLPData(byte[] rlpData) {
        this.rlpData = rlpData;
    }

    public byte[] getRLPData() {
        return rlpData;
    }

    public static void recursivePrint(com.buaa.blockchain.trie.RLPElement element) {

        if (element == null)
            throw new RuntimeException("RLPElement object can't be null");
        if (element instanceof com.buaa.blockchain.trie.RLPList) {

            com.buaa.blockchain.trie.RLPList rlpList = (com.buaa.blockchain.trie.RLPList) element;
            System.out.print("[");
            for (com.buaa.blockchain.trie.RLPElement singleElement : rlpList)
                recursivePrint(singleElement);
            System.out.print("]");
        } else {
            String hex = ByteUtil.toHexString(element.getRLPData());
            System.out.print(hex + ", ");
        }
    }
}
