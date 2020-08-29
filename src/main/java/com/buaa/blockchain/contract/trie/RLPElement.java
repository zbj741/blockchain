package com.buaa.blockchain.contract.trie;

import java.io.Serializable;


public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
