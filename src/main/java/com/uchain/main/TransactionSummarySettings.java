package com.uchain.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TransactionSummarySettings {
    String dir;
    boolean cacheEnabled;
    int cacheSize;
}
