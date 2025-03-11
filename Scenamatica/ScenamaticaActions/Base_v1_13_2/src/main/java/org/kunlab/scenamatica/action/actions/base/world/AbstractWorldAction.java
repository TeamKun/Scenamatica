package org.kunlab.scenamatica.action.actions.base.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

@Category(
        id = "worlds",
        name = "ワールド",
        description = "ワールドに関するアクションを提供します。"
)
@OutputDocs({
        @OutputDoc(
                name = AbstractWorldAction.KEY_OUT_WORLD,
                description = "ワールドの名前です。",
                type = String.class
        )
})
public abstract class AbstractWorldAction extends AbstractAction
        implements Expectable
{
    @InputDoc(
            name = "world",
            description = "ワールドの名前です。",
            type = String.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            on = ActionMethod.EXECUTE,
                            content = "この項目を省略した場合は, 自動的にステージが選択されます。"
                    )
            }
    )
    public static final InputToken<String> IN_WORLD = ofInput(
            "world",
            String.class
    );
    public static final String KEY_OUT_WORLD = "world";
    private static final String[] PADDING_TARGET = {"the_end", "nether"};

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WorldEvent))
            return false;

        WorldEvent e = (WorldEvent) event;
        return this.checkMatchedWorld(ctxt, e.getWorld());
    }

    protected boolean checkMatchedWorld(@NotNull ActionContext ctxt, @NotNull World world)
    {
        boolean result = ctxt.ifHasInput(
                IN_WORLD, worldName ->
                        worldName.equals(world.getName())
                                || ("world_" + worldName).equals(world.getName())
        );

        if (result)
            this.makeOutputs(ctxt, world);

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(type, IN_WORLD);
    }

    @Nullable
    protected World getWorld(@NotNull ActionContext ctxt)
    {
        String name = ctxt.input(IN_WORLD);
        World world = null;
        if (name == null || (world = Bukkit.getWorld(name)) != null)
            return world;

        if (ArrayUtils.contains(PADDING_TARGET, name))
            return Bukkit.getWorld("world_" + name);

        return null;
    }

    @NotNull
    protected World getWorldNonNull(@NotNull ActionContext ctxt)
    {
        if (!ctxt.hasInput(IN_WORLD))
            return ctxt.getContext().getStage().getWorld();

        World world = this.getWorld(ctxt);
        if (world == null)
        {
            String inputWorldName = ctxt.orElseInput(IN_WORLD, () -> "**unset**");
            throw new IllegalActionInputException(IN_WORLD, "Unable to find world: " + inputWorldName);
        }

        return world;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull String worldName)
    {
        ctxt.output(KEY_OUT_WORLD, worldName);
        ctxt.commitOutput();
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world)
    {
        this.makeOutputs(ctxt, world.getName());
    }
}
