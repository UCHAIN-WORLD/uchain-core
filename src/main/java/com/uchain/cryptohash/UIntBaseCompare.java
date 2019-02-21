package com.uchain.cryptohash;

abstract class UIntBaseCompare {
	public static int compare(UIntBase x, UIntBase y) {
        if (x == null || y == null){
            throw new IllegalArgumentException();
        }
        int r = 0;
        if (x != y){
            for (int i = 0; i < x.getSize(); i++ ){
                if (r == 0){
                    if (x.getData()[i] < y.getData()[i]){
                        r = -1;
                    } else if (x.getData()[i] > y.getData()[i]){
                        r = 1;
                    }
                }
            }
        }
        return r;
    }
}
