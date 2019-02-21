package com.uchain.plugin.notify;

import com.uchain.core.Block;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockConfirmedNotify implements NotifyMessage {

    private Block block;

    public BlockConfirmedNotify(Block block) {
        this.block = block;
    }
}
