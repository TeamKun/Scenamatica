package org.kunlab.scenamatica.action.actions.extended_v1_16.player;

import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

@Action(value = "player_death", supportsSince = MinecraftVersion.V1_16)
@OutputDocs({
        @OutputDoc(
                name = PlayerDeathAction.KEY_OUT_DO_EXP_DROP,
                description = "プレイヤが経験値をドロップするかどうかを示します。",
                type = boolean.class
        )
})
public class PlayerDeathAction extends org.kunlab.scenamatica.action.actions.base.player.PlayerDeathAction
        implements Executable, Requireable, Expectable
{
    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        // assert !(event instanceof PlayerEvent);

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        boolean result = super.checkFired(ctxt, e)
                && ctxt.ifHasInput(IN_DO_EXP_DROP, doExpDrop -> doExpDrop == e.shouldDropExperience());

        if (result)
            this.makeAdditionalOutputs(ctxt, e.shouldDropExperience());

        return result;
    }

    protected void makeAdditionalOutputs(@NotNull ActionContext ctxt, @NotNull Boolean doExpDrop)
    {
        ctxt.output(KEY_OUT_DO_EXP_DROP, doExpDrop);
        ctxt.commitOutput();
    }
}
