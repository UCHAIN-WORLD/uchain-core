package com.uchain.core;

import com.uchain.main.Witness;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
public class ForkBase {
    private String dir;
    private ArrayList<Witness> witnesses;

    private boolean confirmedFlag;

    ForkBase (String dir, ArrayList<Witness> witnesses, boolean confirmedFlag){
        this.dir = dir;
        this.witnesses = witnesses;
        this.confirmedFlag = confirmedFlag;
    }


}
