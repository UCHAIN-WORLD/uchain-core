package com.uchain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CommandSuffix {

    private String suffixParam;

    private String suffixValue;

}
