package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public
enum RunAs
{
    NORMAL(null),
    RUNIF("runif"),
    BEFORE("before"),
    AFTER("after");

    private final String key;
}
