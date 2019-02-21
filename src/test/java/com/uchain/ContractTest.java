package com.uchain;

import com.uchain.solidity.CallTransaction;
import com.uchain.solidity.SolcUtils;
import com.uchain.solidity.compiler.CompilationResult;
import com.uchain.solidity.compiler.SolidityCompiler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Arrays.stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ContractTest {


    private static final String SRC = "" +
            "pragma solidity ^0.4.19;" +
            "contract Foo {\n" +
            "\n" +
            "        uint32 idCounter = 1;\n" +
            "        bytes32 public lastError;\n" +
            "\n" +
            "        //  function bar(uint[2] xy) {}\n" +
            "        function baz(uint32 x, bool y) returns(bool r) {\n" +
            "                        r = x > 32 || y;\n" +
            "                }\n" +
            "                //  function sam(bytes name, bool z, uint[] data) {}\n" +
            "        function sam(bytes strAsBytes, bool someFlag, string str) {}\n" +
            "}";

    private static final String CODE = "112233";

    private static final String SRC2 = "contract A { " +
            "   event message(string msg);" +
            "   uint public num; " +
            "   function set(uint a) {" +
            "   require(a > 100);" +
            "       num = a; " +
            "       log1(0x1111, 0x2222);" +
            "   }" +
            "   function getPublic() public constant returns (address) {" +
            "        return msg.sender;" +
            "   }" +
            "   function fire() {" +
            "       message(\"fire\");" +
            "   }" +
            "}";

    private static final String SRCCODE = "608060405260016000806101000a81548163ffffffff021916908363ffffffff16021790555" +
            "034801561003157600080fd5b506101f4806100416000396000f300608060405260043610610057576000357c010000000000000" +
            "0000000000000000000000000000000000000000000900463ffffffff16806329f0de3f1461005c57806390a161301461008f578" +
            "063cdcd77c01461014a575b600080fd5b34801561006857600080fd5b506100716101a1565b60405180826000191660001916815" +
            "260200191505060405180910390f35b34801561009b57600080fd5b5061014860048036038101908080359060200190820180359" +
            "0602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505" +
            "0509192919290803515159060200190929190803590602001908201803590602001908080601f016020809104026020016040519" +
            "08101604052809392919081815260200183838082843782019150505050505091929192905050506101a7565b005b34801561015" +
            "657600080fd5b50610187600480360381019080803563ffffffff169060200190929190803515159060200190929190505050610" +
            "1ac565b604051808215151515815260200191505060405180910390f35b60015481565b505050565b600060208363ffffffff161" +
            "1806101c05750815b9050929150505600a165627a7a72305820c177834ee14ec0a39a0bd5198e259eda9b2e734ae8119f6e9b4ee" +
            "605322213450029";

    @Before
    public void before() {
    }

    @Test
    public void contracts_readSolcVersion() {
        assertThat(SolcUtils.getSolcVersion(), is("0.4.25"));
    }

    @Test
    public void contracts_shouldCompileSuccess() throws IOException{
        final List<String> contractResults = loadContractBin(SRC2, "A");
        final String contractBin = contractResults.get(0);
        System.out.println(contractBin);
        //assertThat(contractBin, is(SRCCODE));
        final String contractAbi = contractResults.get(1);
        CallTransaction.Function[] abiDefinition = new CallTransaction.Contract(contractAbi).functions;
        boolean compiledAllMethods = stream(abiDefinition)
                .map(abi -> abi.name)
                .collect(Collectors.toSet())
                .containsAll(Arrays.asList("lastError", "baz", "sam"));
        assertTrue(compiledAllMethods);
    }

    private List<String> loadContractBin(String sourceCode, String contractName) throws IOException {

        List<String> compileResList = new ArrayList<>();

        final SolidityCompiler.Result compiled = SolidityCompiler.compile(sourceCode.getBytes("UTF-8"), true, SolidityCompiler.Options.BIN, SolidityCompiler.Options.ABI);
        final CompilationResult result = CompilationResult.parse(compiled.output);

        compileResList.add(result.getContract(contractName).bin);
        compileResList.add(result.getContract(contractName).abi);
        return compileResList;
    }


}
