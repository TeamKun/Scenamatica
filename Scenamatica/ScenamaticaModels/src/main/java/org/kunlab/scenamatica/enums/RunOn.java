package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public
enum RunOn
{
    TRIGGER("trigger"),
    SCENARIOS("scenario"),
    RUNIF("runif");

    private final String key;
}
