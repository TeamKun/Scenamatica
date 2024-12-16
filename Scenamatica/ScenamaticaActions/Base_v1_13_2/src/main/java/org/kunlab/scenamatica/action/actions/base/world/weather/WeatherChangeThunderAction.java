package org.kunlab.scenamatica.action.actions.base.world.weather;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action("weather_change_thunder")
@ActionDoc(
        name = "雷の変更",
        description = "ワールドで雷が発生しているかどうかを変更します。",
        events = ThunderChangeEvent.class,

        executable = "ワールドで雷が発生しているかどうかを変更します。",
        expectable = "ワールドの雷の発生状態が変わるまで待機します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = WeatherChangeThunderAction.OUTPUT_THUNDERING,
                        description = "雷が発生しているかを出力します。",
                        type = Boolean.class
                ),
                @OutputDoc(
                        name = WeatherChangeThunderAction.OUTPUT_DURATION,
                        description = "その雷が続く期間（チック）を出力します。",
                        type = Integer.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.NOTE,
                        title = "天候について",
                        content = "その他の天候状態を変更する場合は, `weather_change` アクションを使用してください。"
                )
        }
)
public class WeatherChangeThunderAction extends AbstractWeatherAction
        implements Executable, Expectable
{
    public static final String OUTPUT_THUNDERING = "thundering";
    public static final String OUTPUT_DURATION = "duration";

    @InputDoc(
            name = "雷の発生状態",
            description = "雷が発生しているかを設定します。",
            type = Boolean.class,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            on = ActionMethod.EXECUTE,
                            content = "この値を指定しない場合は, 今とは逆の天候に変更されます。\n" +
                                    "雷の場合は晴れに, 晴れの場合は雷に変更されます。"
                    )
            }
    )
    public static final InputToken<Boolean> INPUT_THUNDERING = ofInput("thundering", Boolean.class);

    @InputDoc(
            name = "雷の継続時間",
            description = "雷が続く期間（チック）を設定します。",
            type = Integer.class,
            availableFor = {ActionMethod.EXECUTE},

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            on = ActionMethod.EXECUTE,
                            content = "この値を指定しなかった場合は, 自動的に `0` が設定されます。"
                    ),
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            content = "この値が `0` の場合は, その雷の状態が永久に続くことを意味します。"
                    )
            }
    )
    public static final InputToken<Integer> INPUT_DURATION = ofInput("duration", Integer.class);

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = this.getWorldNonNull(ctxt);

        int duration = ctxt.orElseInput(INPUT_DURATION, () -> 0);
        boolean changeToThundering;
        if (ctxt.hasInput(INPUT_THUNDERING))
            changeToThundering = ctxt.input(INPUT_THUNDERING);
        else
            changeToThundering = !world.hasStorm();

        this.makeOutputs(ctxt, world, changeToThundering, duration);
        world.setThundering(changeToThundering);
        world.setThunderDuration(duration);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof ThunderChangeEvent))
            return false;

        ThunderChangeEvent e = (ThunderChangeEvent) event;
        if (!super.checkMatchedWorld(ctxt, e.getWorld()))
            return false;

        boolean result = ctxt.ifHasInput(INPUT_THUNDERING, r -> e.toThunderState() == r);
        if (result)
            this.makeOutputs(ctxt, e.getWorld(), e.toThunderState(), null);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world, boolean thundering, @Nullable Integer duration)
    {
        ctxt.output(OUTPUT_THUNDERING, thundering);
        if (duration != null)
            ctxt.output(OUTPUT_DURATION, duration);
        super.makeOutputs(ctxt, world);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ThunderChangeEvent.class
        );
    }
}
