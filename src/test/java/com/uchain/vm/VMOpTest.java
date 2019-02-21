package com.uchain.vm;import com.uchain.vm.program.Program;import org.junit.Test;import org.spongycastle.util.encoders.Hex;import java.util.List;import static org.junit.Assert.assertEquals;import static org.junit.Assert.assertTrue;public class VMOpTest extends VMBaseOpTest{    @Test // LOG0 OP    public void tesLog0() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 LOG0"), invoke,Long.MAX_VALUE);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        List<LogInfo> logInfoList = program.getResult().getLogInfoList();        LogInfo logInfo = logInfoList.get(0);        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));        assertEquals(0, logInfo.getTopics().size());        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo                .getData()));    }    @Test // LOG1 OP    public void tesLog1() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH1 0x20 PUSH1 0x00 LOG1"), invoke, Long.MAX_VALUE);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        List<LogInfo> logInfoList = program.getResult().getLogInfoList();        LogInfo logInfo = logInfoList.get(0);        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));        assertEquals(1, logInfo.getTopics().size());        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo                .getData()));    }    @Test // LOG2 OP    public void tesLog2() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH1 0x20 PUSH1 0x00 LOG2"), invoke, Long.MAX_VALUE);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        List<LogInfo> logInfoList = program.getResult().getLogInfoList();        LogInfo logInfo = logInfoList.get(0);        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));        assertEquals(2, logInfo.getTopics().size());        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo                .getData()));    }    @Test // LOG3 OP    public void tesLog3() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH2 0x3333 PUSH1 0x20 PUSH1 0x00 LOG3"), invoke, Long.MAX_VALUE);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        List<LogInfo> logInfoList = program.getResult().getLogInfoList();        LogInfo logInfo = logInfoList.get(0);        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));        assertEquals(3, logInfo.getTopics().size());        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo                .getData()));    }    @Test // LOG4 OP    public void tesLog4() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH2 0x9999 PUSH2 0x6666 PUSH2 0x3333 PUSH2 0x5555 PUSH1 0x20 PUSH1 0x00 LOG4"), invoke, Long.MAX_VALUE);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        List<LogInfo> logInfoList = program.getResult().getLogInfoList();        LogInfo logInfo = logInfoList.get(0);        assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(logInfo.getAddress()));        assertEquals(4, logInfo.getTopics().size());        assertEquals("0000000000000000000000000000000000000000000000000000000000001234", Hex.toHexString(logInfo                .getData()));    }    @Test // STOP OP    public void testSTOP_1() {        VM vm = new VM();        program = new Program(compile("PUSH1 0x20 PUSH1 0x30 PUSH1 0x10 PUSH1 0x30 PUSH1 0x11 PUSH1 0x23 STOP"), invoke, Long.MAX_VALUE);        int expectedSteps = 7;        int i = 0;        while (!program.isStopped()) {            vm.step(program);            ++i;        }        assertEquals(expectedSteps, i);    }    @Test // RETURN OP    public void testRETURN_1() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 RETURN"), invoke, Long.MAX_VALUE);        String s_expected_1 = "0000000000000000000000000000000000000000000000000000000000001234";        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());        assertTrue(program.isStopped());    }    @Test // RETURN OP    public void testRETURN_2() {        VM vm = new VM();        program = new Program(compile("PUSH2 0x1234 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x1F RETURN"), invoke, Long.MAX_VALUE);        String s_expected_1 = "3400000000000000000000000000000000000000000000000000000000000000";        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());        assertTrue(program.isStopped());    }    @Test // RETURN OP    public void testRETURN_3() {        VM vm = new VM();        program = new Program(compile                        ("PUSH32 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x00 RETURN"), invoke, Long.MAX_VALUE);        String s_expected_1 = "A0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1";        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());        assertTrue(program.isStopped());    }    @Test // RETURN OP    public void testRETURN_4() {        VM vm = new VM();        program = new Program(compile                        ("PUSH32 0xA0B0C0D0E0F0A1B1C1D1E1F1A2B2C2D2E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B1 PUSH1 0x00 MSTORE PUSH1 0x20 PUSH1 0x10 RETURN"), invoke, Long.MAX_VALUE);        String s_expected_1 = "E2F2A3B3C3D3E3F3A4B4C4D4E4F4A1B100000000000000000000000000000000";        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        vm.step(program);        assertEquals(s_expected_1, Hex.toHexString(program.getResult().getHReturn()).toUpperCase());        assertTrue(program.isStopped());    }    @Test    public void regression1Test() {        String code2 = "60006116bf537c01000000000000000000000000000000000000000000000000000000006000350463b041b2858114156101c257600435604052780100000000000000000000000000000000000000000000000060606060599059016000905260028152604051816020015260008160400152809050205404606052606051151561008f57600060a052602060a0f35b66040000000000015460c052600760e0525b60605178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540413156101b0575b60e05160050a60605178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540403121561014457600060e05113610147565b60005b1561015a57600160e0510360e0526100ea565b7c010000000000000000000000000000000000000000000000000000000060e05160200260020a6060606059905901600090526002815260c051816020015260018160400152809050205402045460c0526100a1565b60405160c05114610160526020610160f35b63720f60f58114156102435760043561018052601c60445990590160009052016305215b2f601c820352790100000000000000000000000000000000000000000000000000600482015260206101c0602483600061018051602d5a03f1506101c05190506604000000000003556604000000000003546101e05260206101e0f35b63b8c48f8c8114156104325760043560c05260243561020052604435610220526000660400000000000254141515610286576000610240526020610240f3610292565b60016604000000000002555b60c0516604000000000001556060606059905901600090526002815260c051816020015260008160400152809050205461026052610260610200518060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610260516060606059905901600090526002815260c05181602001526000816040015280905020556060606059905901600090526002815260c051816020015260008160400152809050205461030052601061030001610220518060101a82538060111a60018301538060121a60028301538060131a60038301538060141a60048301538060151a60058301538060161a60068301538060171a60078301538060181a60088301538060191a600983015380601a1a600a83015380601b1a600b83015380601c1a600c83015380601d1a600d83015380601e1a600e83015380601f1a600f8301535050610300516060606059905901600090526002815260c051816020015260008160400152809050205560016103a05260206103a0f35b632b861629811415610eed57365990590160009052366004823760043560208201016103e0525060483580601f1a6104405380601e1a6001610440015380601d1a6002610440015380601c1a6003610440015380601b1a6004610440015380601a1a600561044001538060191a600661044001538060181a600761044001538060171a600861044001538060161a600961044001538060151a600a61044001538060141a600b61044001538060131a600c61044001538060121a600d61044001538060111a600e61044001538060101a600f610440015380600f1a6010610440015380600e1a6011610440015380600d1a6012610440015380600c1a6013610440015380600b1a6014610440015380600a1a601561044001538060091a601661044001538060081a601761044001538060071a601861044001538060061a601961044001538060051a601a61044001538060041a601b61044001538060031a601c61044001538060021a601d61044001538060011a601e61044001538060001a601f6104400153506104405161040052700100000000000000000000000000000000700100000000000000000000000000000000606060605990590160009052600281526104005181602001526000816040015280905020540204610460526104605161061b57005b6103e05160208103516020599059016000905260208183856000600287604801f15080519050905090506104a0526020599059016000905260208160206104a0600060026068f1508051905080601f1a6105605380601e1a6001610560015380601d1a6002610560015380601c1a6003610560015380601b1a6004610560015380601a1a600561056001538060191a600661056001538060181a600761056001538060171a600861056001538060161a600961056001538060151a600a61056001538060141a600b61056001538060131a600c61056001538060121a600d61056001538060111a600e61056001538060101a600f610560015380600f1a6010610560015380600e1a6011610560015380600d1a6012610560015380600c1a6013610560015380600b1a6014610560015380600a1a601561056001538060091a601661056001538060081a601761056001538060071a601861056001538060061a601961056001538060051a601a61056001538060041a601b61056001538060031a601c61056001538060021a601d61056001538060011a601e61056001538060001a601f6105600153506105605160c0527001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540204610580526000610580511415156108345760006105c05260206105c0f35b608c3563010000008160031a02620100008260021a026101008360011a028360001a01010190506105e05263010000006105e051046106405262ffffff6105e0511661066052600361064051036101000a610660510261062052600060c05113156108a6576106205160c051126108a9565b60005b15610ee05760c05160c05160c051660400000000000054556060606059905901600090526002815260c0518160200152600081604001528090502054610680526008610680016604000000000000548060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610680516060606059905901600090526002815260c05181602001526000816040015280905020556001660400000000000054016604000000000000556060606059905901600090526002815260c051816020015260008160400152809050205461072052610720600178010000000000000000000000000000000000000000000000006060606059905901600090526002815261040051816020015260008160400152809050205404018060181a82538060191a600183015380601a1a600283015380601b1a600383015380601c1a600483015380601d1a600583015380601e1a600683015380601f1a60078301535050610720516060606059905901600090526002815260c051816020015260008160400152809050205560006107e052780100000000000000000000000000000000000000000000000068010000000000000000606060605990590160009052600281526104005181602001526000816040015280905020540204610800526107e06108005180601c1a825380601d1a600183015380601e1a600283015380601f1a600383015350506001610880525b6008610880511215610c07576108805160050a6108a05260016108a05178010000000000000000000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205404071415610b7957610880516004026107e0016108005180601c1a825380601d1a600183015380601e1a600283015380601f1a60038301535050610bf7565b610880516004026107e0017c01000000000000000000000000000000000000000000000000000000006108805160200260020a60606060599059016000905260028152610400518160200152600181604001528090502054020480601c1a825380601d1a600183015380601e1a600283015380601f1a600383015350505b6001610880510161088052610adf565b6107e0516060606059905901600090526002815260c051816020015260018160400152809050205550506080608059905901600090526002815260c051816020015260028160400152600081606001528090502060005b6002811215610c8057806020026103e051015182820155600181019050610c5e565b700100000000000000000000000000000000600003816020026103e051015116828201555050610620517bffff000000000000000000000000000000000000000000000000000005610a00526060606059905901600090526002815260c0518160200152600081604001528090502054610a20526010610a2001610a005161046051018060101a82538060111a60018301538060121a60028301538060131a60038301538060141a60048301538060151a60058301538060161a60068301538060171a60078301538060181a60088301538060191a600983015380601a1a600a83015380601b1a600b83015380601c1a600c83015380601d1a600d83015380601e1a600e83015380601f1a600f8301535050610a20516060606059905901600090526002815260c05181602001526000816040015280905020557001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205402046105805266040000000000025461058051121515610e965760c05166040000000000015561058051660400000000000255601c606459905901600090520163c86a90fe601c8203526103e860048201523260248201526020610ae06044836000660400000000000354602d5a03f150610ae051905015610e95576103e8660400000000000454016604000000000004555b5b78010000000000000000000000000000000000000000000000006060606059905901600090526002815260c051816020015260008160400152809050205404610b00526020610b00f35b6000610b40526020610b40f35b63c6605beb811415611294573659905901600090523660048237600435610b6052602435610b80526044356020820101610ba0526064356040525067016345785d8a00003412151515610f47576000610bc0526020610bc0f35b601c6044599059016000905201633d73b705601c82035260405160048201526020610be0602483600030602d5a03f150610be05190508015610f895780610fc1565b601c604459905901600090520163b041b285601c82035260405160048201526020610c20602483600030602d5a03f150610c20519050155b905015610fd5576000610c40526020610c40f35b6060601c61014c59905901600090520163b7129afb601c820352610b60516004820152610b80516024820152610ba05160208103516020026020018360448401526020820360a4840152806101088401528084019350505081600401599059016000905260648160648460006004601cf161104c57fe5b6064810192506101088201518080858260a487015160006004600a8705601201f161107357fe5b508084019350508083036020610d008284600030602d5a03f150610d00519050905090509050610c60526080608059905901600090526002815260405181602001526002816040015260008160600152809050207c010000000000000000000000000000000000000000000000000000000060028201540464010000000060018301540201610d805250610d805180601f1a610de05380601e1a6001610de0015380601d1a6002610de0015380601c1a6003610de0015380601b1a6004610de0015380601a1a6005610de001538060191a6006610de001538060181a6007610de001538060171a6008610de001538060161a6009610de001538060151a600a610de001538060141a600b610de001538060131a600c610de001538060121a600d610de001538060111a600e610de001538060101a600f610de0015380600f1a6010610de0015380600e1a6011610de0015380600d1a6012610de0015380600c1a6013610de0015380600b1a6014610de0015380600a1a6015610de001538060091a6016610de001538060081a6017610de001538060071a6018610de001538060061a6019610de001538060051a601a610de001538060041a601b610de001538060031a601c610de001538060021a601d610de001538060011a601e610de001538060001a601f610de0015350610de051610d4052610d4051610c60511415611286576001610e00526020610e00f3611293565b6000610e20526020610e20f35b5b638f6b104c8114156115195736599059016000905236600482376004356020820101610e4052602435610b6052604435610b80526064356020820101610ba05260843560405260a435610e60525060016080601c6101ac59905901600090520163c6605beb601c820352610b60516004820152610b80516024820152610ba05160208103516020026020018360448401526020820360c48401528061014884015280840193505050604051606482015281600401599059016000905260848160848460006004601ff161136357fe5b6084810192506101488201518080858260c487015160006004600a8705601201f161138a57fe5b508084019350508083036020610e80828434306123555a03f150610e8051905090509050905014156114b3576040601c60ec59905901600090520163f0cf1ff4601c820352610e40516020601f6020830351010460200260200183600484015260208203604484015280608884015280840193505050610b60516024820152816004015990590160009052604481604484600060046018f161142857fe5b604481019250608882015180808582604487015160006004600a8705601201f161144e57fe5b508084019350508083036020610ec082846000610e6051602d5a03f150610ec0519050905090509050610ea0526040599059016000905260018152610ea051602082015260208101905033602082035160200282a150610ea051610f20526020610f20f35b604059905901600090526001815261270f600003602082015260208101905033602082035160200282a150604059905901600090526001815261270f6000036020820152602081019050610e6051602082035160200282a1506000610f80526020610f80f35b6309dd0e8181141561153957660400000000000154610fa0526020610fa0f35b630239487281141561159557780100000000000000000000000000000000000000000000000060606060599059016000905260028152660400000000000154816020015260008160400152809050205404610fc0526020610fc0f35b6361b919a68114156116045770010000000000000000000000000000000070010000000000000000000000000000000060606060599059016000905260028152660400000000000154816020015260008160400152809050205402046110005261100051611040526020611040f35b63a7cc63c28114156118b55766040000000000015460c0527001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c05181602001526000816040015280905020540204611060526000610880525b600a610880511215611853576080608059905901600090526002815260c05181602001526002816040015260008160600152809050207c0100000000000000000000000000000000000000000000000000000000600182015404640100000000825402016110c052506110c05180601f1a6111205380601e1a6001611120015380601d1a6002611120015380601c1a6003611120015380601b1a6004611120015380601a1a600561112001538060191a600661112001538060181a600761112001538060171a600861112001538060161a600961112001538060151a600a61112001538060141a600b61112001538060131a600c61112001538060121a600d61112001538060111a600e61112001538060101a600f611120015380600f1a6010611120015380600e1a6011611120015380600d1a6012611120015380600c1a6013611120015380600b1a6014611120015380600a1a601561112001538060091a601661112001538060081a601761112001538060071a601861112001538060061a601961112001538060051a601a61112001538060041a601b61112001538060031a601c61112001538060021a601d61112001538060011a601e61112001538060001a601f6111200153506111205160c0526001610880510161088052611671565b7001000000000000000000000000000000007001000000000000000000000000000000006060606059905901600090526002815260c0518160200152600081604001528090502054020461114052611140516110605103611180526020611180f35b63b7129afb811415611e35573659905901600090523660048237600435610b6052602435610b80526044356020820101610ba05250610b60516111a0526020610ba05103516111c0526000610880525b6111c051610880511215611e0c5761088051602002610ba05101516111e0526002610b805107611200526001611200511415611950576111e051611220526111a0516112405261196e565b600061120051141561196d576111a051611220526111e051611240525b5b604059905901600090526112205180601f1a6112805380601e1a6001611280015380601d1a6002611280015380601c1a6003611280015380601b1a6004611280015380601a1a600561128001538060191a600661128001538060181a600761128001538060171a600861128001538060161a600961128001538060151a600a61128001538060141a600b61128001538060131a600c61128001538060121a600d61128001538060111a600e61128001538060101a600f611280015380600f1a6010611280015380600e1a6011611280015380600d1a6012611280015380600c1a6013611280015380600b1a6014611280015380600a1a601561128001538060091a601661128001538060081a601761128001538060071a601861128001538060061a601961128001538060051a601a61128001538060041a601b61128001538060031a601c61128001538060021a601d61128001538060011a601e61128001538060001a601f6112800153506112805181526112405180601f1a6112e05380601e1a60016112e0015380601d1a60026112e0015380601c1a60036112e0015380601b1a60046112e0015380601a1a60056112e001538060191a60066112e001538060181a60076112e001538060171a60086112e001538060161a60096112e001538060151a600a6112e001538060141a600b6112e001538060131a600c6112e001538060121a600d6112e001538060111a600e6112e001538060101a600f6112e0015380600f1a60106112e0015380600e1a60116112e0015380600d1a60126112e0015380600c1a60136112e0015380600b1a60146112e0015380600a1a60156112e001538060091a60166112e001538060081a60176112e001538060071a60186112e001538060061a60196112e001538060051a601a6112e001538060041a601b6112e001538060031a601c6112e001538060021a601d6112e001538060011a601e6112e001538060001a601f6112e00153506112e051602082015260205990590160009052602081604084600060026088f1508051905061130052602059905901600090526020816020611300600060026068f1508051905080601f1a6113805380601e1a6001611380015380601d1a6002611380015380601c1a6003611380015380601b1a6004611380015380601a1a600561138001538060191a600661138001538060181a600761138001538060171a600861138001538060161a600961138001538060151a600a61138001538060141a600b61138001538060131a600c61138001538060121a600d61138001538060111a600e61138001538060101a600f611380015380600f1a6010611380015380600e1a6011611380015380600d1a6012611380015380600c1a6013611380015380600b1a6014611380015380600a1a601561138001538060091a601661138001538060081a601761138001538060071a601861138001538060061a601961138001538060051a601a61138001538060041a601b61138001538060031a601c61138001538060021a601d61138001538060011a601e61138001538060001a601f6113800153506113805190506111a0526002610b805105610b80526001610880510161088052611905565b6111a0511515611e265760016000036113a05260206113a0f35b6111a0516113c05260206113c0f35b633d73b7058114156120625760043560405266040000000000015460c0526000610880525b60066108805112156120555760c0516040511415611e7f5760016113e05260206113e0f35b6080608059905901600090526002815260c05181602001526002816040015260008160600152809050207c01000000000000000000000000000000000000000000000000000000006001820154046401000000008254020161142052506114205180601f1a6114805380601e1a6001611480015380601d1a6002611480015380601c1a6003611480015380601b1a6004611480015380601a1a600561148001538060191a600661148001538060181a600761148001538060171a600861148001538060161a600961148001538060151a600a61148001538060141a600b61148001538060131a600c61148001538060121a600d61148001538060111a600e61148001538060101a600f611480015380600f1a6010611480015380600e1a6011611480015380600d1a6012611480015380600c1a6013611480015380600b1a6014611480015380600a1a601561148001538060091a601661148001538060081a601761148001538060071a601861148001538060061a601961148001538060051a601a61148001538060041a601b61148001538060031a601c61148001538060021a601d61148001538060011a601e61148001538060001a601f6114800153506114805160c0526001610880510161088052611e5a565b60006114a05260206114a0f35b6391cf0e96811415612105576004356114c052601c60845990590160009052016367eae672601c8203523360048201526114c051602482015230604482015260206114e06064836000660400000000000354602d5a03f1506114e051905015612104576604000000000004546114c05130310205611500526114c0516604000000000004540366040000000000045560006000600060006115005133611388f1505b5b6313f955e18114156122985736599059016000905236600482376004356020820101611520526024356115405250605061156052600061158052611560516115a0526000610880525b611540516108805112156122895761158051806115a051038080602001599059016000905281815260208101905090508180828286611520510160006004600a8705601201f161219a57fe5b50809050905090506115c0526020601c608c599059016000905201632b861629601c8203526115c0516020601f6020830351010460200260200183600484015260208203602484015280604884015280840193505050816004015990590160009052602481602484600060046015f161220f57fe5b602481019250604882015180808582602487015160006004600a8705601201f161223557fe5b5080840193505080830360206116808284600030602d5a03f150611680519050905090509050610ea05261156051611580510161158052611560516115a051016115a052600161088051016108805261214e565b610ea0516116a05260206116a0f35b50";        String result = Program.stringifyMultiline(Hex.decode(code2));        System.out.println(result);    }    @Test    public void regression2Test() {        String code2 = "6060604052604051602080603f8339016040526060805190602001505b806000600050819055505b50600a8060356000396000f30060606040526008565b000000000000000000000000000000000000000000000000000000000000000021";        String result = Program.stringifyMultiline(Hex.decode(code2));        System.out.println(result);        assertTrue(result.contains("00000000000000000000000000000000")); // detecting bynary data in bytecode    }}