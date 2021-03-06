package com.uchain.plugin.notifymessage;

import com.uchain.core.block.Block;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NewBlockProducedNotify implements NotifyMessage {

    private Block block;

    public NewBlockProducedNotify(Block block) {
        this.block = block;
    }

}
