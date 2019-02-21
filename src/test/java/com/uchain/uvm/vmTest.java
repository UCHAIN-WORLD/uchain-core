package com.uchain.uvm;

import com.uchain.core.Transaction;
import com.uchain.main.SystemProperties;
import com.uchain.uvm.hook.VMHook;
import com.uchain.uvm.program.Program;
import com.uchain.uvm.program.invoke.ProgramInvoke;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class vmTest {



    @Test
    public void test(){

        Repository repository = mock(Repository.class);
        //when(repository.getContractDetails(new byte[32])).thenReturn(repository.getContractDetails(new byte[32]));
        Transaction tx = mock(Transaction.class);


        byte[] code = new byte[]{96,96,96,64,82,52,21,97,0,15,87,96,0,-128,-3,91,97,1,-16,-128,97,0,30,96,0,57,96,0,-13,0,96,96,96,64,82,96,4,54,16,97,0,98,87,96,0,53,124,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-112,4,99,-1,-1,-1,-1,22,-128,99,44,4,71,121,20,97,0,103,87,-128,99,69,112,-108,-52,20,97,0,-68,87,-128,99,78,112,-79,-36,20,97,0,-47,87,-128,99,96,-2,71,-79,20,97,0,-6,87,91,96,0,-128,-3,91,52,21,97,0,114,87,96,0,-128,-3,91,97,0,122,97,1,29,86,91,96,64,81,-128,-126,115,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,22,115,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,22,-127,82,96,32,1,-111,80,80,96,64,81,-128,-111,3,-112,-13,91,52,21,97,0,-57,87,96,0,-128,-3,91,97,0,-49,97,1,37,86,91,0,91,52,21,97,0,-36,87,96,0,-128,-3,91,97,0,-28,97,1,-113,86,91,96,64,81,-128,-126,-127,82,96,32,1,-111,80,80,96,64,81,-128,-111,3,-112,-13,91,52,21,97,1,5,87,96,0,-128,-3,91,97,1,27,96,4,-128,-128,53,-112,96,32,1,-112,-111,-112,80,80,97,1,-107,86,91,0,91,96,0,51,-112,80,-112,86,91,127,5,-57,102,-47,-59,-22,111,64,-81,-61,-116,-40,-30,115,8,-62,54,-60,-110,-5,-49,-93,43,69,-115,39,85,-49,118,-20,30,33,96,64,81,-128,-128,96,32,1,-126,-127,3,-126,82,96,4,-127,82,96,32,1,-128,127,102,105,114,101,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-127,82,80,96,32,1,-111,80,80,96,64,81,-128,-111,3,-112,-95,86,91,96,0,84,-127,86,91,-128,96,0,-127,-112,85,80,97,34,34,96,1,2,97,17,17,96,64,81,-128,-126,96,1,2,96,0,25,22,-127,82,96,32,1,-111,80,80,96,64,81,-128,-111,3,-112,-95,80,86,0,-95,101,98,122,122,114,48,88,32,-121,-35,34,93,122,-19,95,-92,-1,-29,108,-128,-54,-55,-79,-52,23,125,-42,54,38,-119,-44,-87,61,-57,-70,101,-45,77,11,-73,0,41};

        when(tx.getDatas()).thenReturn(code);


        ProgramInvoke programInvoke = mock(ProgramInvoke.class);

        when(programInvoke.getOwnerAddress()).thenReturn(DataWord.ZERO);
        when(programInvoke.getBalance()).thenReturn(DataWord.ZERO);
        when(programInvoke.getOriginAddress()).thenReturn(DataWord.ZERO);
        when(programInvoke.getCallerAddress()).thenReturn(DataWord.ZERO);
        when(programInvoke.getMinGasPrice()).thenReturn(DataWord.ZERO);
        when(programInvoke.getGas()).thenReturn(DataWord.of(Long.MAX_VALUE));
        when(programInvoke.getGasLong()).thenReturn(Long.MAX_VALUE);
        when(programInvoke.getCallValue()).thenReturn(DataWord.ZERO);
        when(programInvoke.getDataSize()).thenReturn(DataWord.ZERO);
        when(programInvoke.getPrevHash()).thenReturn(DataWord.ZERO);
        when(programInvoke.getCoinbase()).thenReturn(DataWord.ZERO);
        when(programInvoke.getTimestamp()).thenReturn(DataWord.ZERO);
        when(programInvoke.getNumber()).thenReturn(DataWord.ZERO);
        when(programInvoke.getDifficulty()).thenReturn(DataWord.ZERO);
        when(programInvoke.getGaslimit()).thenReturn(DataWord.ZERO);

        when(programInvoke.byTransaction()).thenReturn(true);
        when(programInvoke.byTestingSuite()).thenReturn(false);
        when(programInvoke.getCallDeep()).thenReturn(0);
        when(programInvoke.getRepository()).thenReturn(repository);
        when(programInvoke.getOrigRepository()).thenReturn(repository);
        when(programInvoke.isStaticCall()).thenReturn(false);



        SystemProperties config = new SystemProperties(true, 0, "", true, true, true,"", false, false, false, false, false);

        VMHook vmHook = VMHook.EMPTY;

        VM vm = new VM(config, vmHook);

        Program program = new Program(tx.getDatas(), programInvoke, tx, config, vmHook,Long.MAX_VALUE);

        vm.play(program);

        byte testbyte = 64;
        int opcode = testbyte & 0xFF;
        System.out.println("1111111111111111111111111");
        System.out.println(opcode);


    }
}
