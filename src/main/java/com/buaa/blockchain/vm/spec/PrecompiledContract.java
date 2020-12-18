package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.vm.utils.Pair;

public interface PrecompiledContract {

    long getGasForData(byte[] data);

    Pair<Boolean, byte[]> execute(PrecompiledContractContext invoke);
}
