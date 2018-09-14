package com.uchain.core.consensus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TwoTuple<A, B> {

    public final A first;

    public final B second;

    public String toString(){
        return "(" + first + ", " + second + ")";
    }

}