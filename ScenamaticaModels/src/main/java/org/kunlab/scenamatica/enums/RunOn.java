package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public
enum RunOn
{
    TRIGGER("trigger"),
    SCENARIOS("scenarios"),
    RUNIF("runif");

    private final String key;
}
