package com.buaa.blockchain.vm.program;

import com.buaa.blockchain.vm.OpCode;

import java.util.HashSet;
import java.util.Set;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/16
 * @since JDK1.8
 */
public class ProgramPreprocess {
    private Set<Integer> jumpdest = new HashSet<>();

    public boolean hasJumpDest(int pc) {
        return jumpdest.contains(pc);
    }

    public static ProgramPreprocess compile(byte[] ops) {
        ProgramPreprocess ret = new ProgramPreprocess();

        for (int i = 0; i < ops.length; ++i) {
            OpCode op = OpCode.code(ops[i]);
            if (op == null) {
                continue;
            }

            if (op.equals(OpCode.JUMPDEST)) {
                ret.jumpdest.add(i);
            }

            if (op.asInt() >= OpCode.PUSH1.asInt() && op.asInt() <= OpCode.PUSH32.asInt()) {
                i += op.asInt() - OpCode.PUSH1.asInt() + 1;
            }
        }

        return ret;
    }
}

