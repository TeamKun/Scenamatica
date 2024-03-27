package org.kunlab.scenamatica.exceptions.context.stage;

import lombok.experimental.StandardException;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;

/**
 * ステージが既に破棄されている場合にスローされる例外です。
 */
@StandardException
public class StageAlreadyDestroyedException extends ContextPreparationException
{
}
