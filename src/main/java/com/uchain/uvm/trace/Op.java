package com.uchain.uvm.trace;import com.uchain.uvm.OpCode;import lombok.Getter;import lombok.Setter;import java.math.BigInteger;@Getter@Setterpublic class Op {    private OpCode code;    private int deep;    private int pc;    private BigInteger gas;    private OpActions actions;}