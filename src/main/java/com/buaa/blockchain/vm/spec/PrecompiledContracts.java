package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.vm.DataWord;


public interface PrecompiledContracts {

    PrecompiledContract getContractForAddress(DataWord address);
}
