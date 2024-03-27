package org.kunlab.scenamatica.exceptions.context.actor;

import lombok.experimental.StandardException;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;

/**
 * アクターの生成に失敗したことを表す例外です。
 */
@StandardException
public class ActorCreationException extends ContextPreparationException
{
}
