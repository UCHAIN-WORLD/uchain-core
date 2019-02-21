package com.uchain.plugin.notifymessage;

import com.uchain.core.consensus.ForkItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ForkSwitchNotify implements NotifyMessage {

    private List<ForkItem> from;
    private List<ForkItem> to;

    public ForkSwitchNotify(List<ForkItem> from, List<ForkItem> to) {
        this.from = from;
        this.to = to;
    }

}
