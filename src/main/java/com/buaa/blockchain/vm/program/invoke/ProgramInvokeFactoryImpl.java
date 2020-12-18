/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buaa.blockchain.vm.program.invoke;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.client.Repository;
import com.buaa.blockchain.vm.program.Program;

import java.math.BigInteger;

public class ProgramInvokeFactoryImpl implements ProgramInvokeFactory {

    @Override
    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository
                                             ) {

        // creates an phantom invoke, from the sender to the sender, at depth -1

        byte[] address = tx.getFrom();
        byte[] origin = tx.getFrom();
        byte[] caller = tx.getFrom();
        long gas = tx.getGas();
        BigInteger gasPrice = tx.getGasPrice();
        BigInteger callValue = tx.getValue();
        byte[] callData = tx.getData();

        byte[] prevHash = block.getParentHash();
        byte[] coinbase = block.getCoinbase();
        long timestamp = block.getTimestamp();
        long number = block.getNumber();
        BigInteger difficulty = block.getDifficulty();
        long gasLimit = block.getGasLimit();

        Repository originalRepository = repository.clone();
        int callDepth = -1;

        return new ProgramInvokeImpl(DataWord.of(address), DataWord.of(origin), DataWord.of(caller),
                gas, DataWord.of(gasPrice), DataWord.of(callValue), callData,
                DataWord.of(prevHash), DataWord.of(coinbase), DataWord.of(timestamp), DataWord.of(number),
                DataWord.of(difficulty), DataWord.of(gasLimit),
                repository, originalRepository, callDepth, false);
    }

    @Override
    public ProgramInvoke createProgramInvoke(Program program,
                                             DataWord callerAddress, DataWord toAddress,
                                             long gas, DataWord value, byte[] data,
                                             Repository repository, boolean isStaticCall) {

        DataWord origin = program.getOriginAddress();
        DataWord gasPrice = program.getGasPrice();

        DataWord prevHash = program.getBlockPrevHash();
        DataWord coinbase = program.getBlockCoinbase();
        DataWord timestamp = program.getBlockTimestamp();
        DataWord number = program.getBlockNumber();
        DataWord difficulty = program.getBlockDifficulty();
        DataWord gasLimit = program.getBlockGasLimit();

        Repository originalRepository = program.getOriginalRepository();
        int callDepth = program.getCallDepth() + 1;

        return new ProgramInvokeImpl(toAddress, origin, callerAddress, gas, gasPrice, value, data,
                prevHash, coinbase, timestamp, number, difficulty, gasLimit,
                repository, originalRepository,  callDepth, isStaticCall);
    }
}
