package com.buaa.blockchain.vm.program.invoke;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.client.Repository;
import com.buaa.blockchain.vm.program.Program;

public interface ProgramInvokeFactory {

    ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository);

    ProgramInvoke createProgramInvoke(Program program, DataWord callerAddress, DataWord toAddress, long gas, DataWord value, byte[] data, Repository repository, boolean isStaticCall);
}
