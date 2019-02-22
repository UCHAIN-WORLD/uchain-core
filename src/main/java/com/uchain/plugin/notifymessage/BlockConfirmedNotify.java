package com.uchain.plugin.notifymessage;

import com.uchain.core.block.Block;
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
