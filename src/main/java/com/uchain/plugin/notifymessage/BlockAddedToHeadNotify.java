package com.uchain.plugin.notifymessage;

import com.uchain.core.block.Block;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockAddedToHeadNotify implements NotifyMessage {
    private Block block;

    public BlockAddedToHeadNotify(Block block) {
        this.block = block;
    }

}
