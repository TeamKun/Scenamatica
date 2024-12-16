package org.kunlab.scenamatica.action.actions.base.world.weather;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action("weather_change")
@ActionDoc(
        name = "天候の变化",
        description = "ワールドの天候を変更します。",
        events = WeatherChangeEvent.class,

        executable = "ワールドの天候を変更します。",
        expectable = "ワールドの天候が変わるまで待機します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = WeatherChangeAction.OUTPUT_RAINING,
                        description = "雨（または雪）が降っているかを出力します。",
                        type = Boolean.class
                ),
                @OutputDoc(
                        name = WeatherChangeAction.OUTPUT_DURATION,
                        description = "その天候が続く期間（チック）を出力します。",
                        type = Integer.class,

                        admonitions = {
                                @Admonition(
                                        type = AdmonitionType.INFORMATION,
                                        content = "この値が `0` の場合は, その天候が永久に続くことを意味します。"
                                )
                        }
                )
        }
)
public class WeatherChangeAction extends AbstractWeatherAction
        implements Executable, Expectable
{
    public static final String OUTPUT_RAINING = "raining";
    public static final String OUTPUT_DURATION = "duration";

    @InputDoc(
            name = "raining",
            description = "雨（または雪）が降っているかどうかを指定します。",
            type = Boolean.class,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            on = ActionMethod.EXECUTE,
                            content = "この値を指定しない場合は, 今とは逆の天候に変更されます。\n" +
                                    "雨（または雪）の場合は晴れに, 晴れの場合は雨（または雪）に変更されます。"
                    )
            }
    )
    public static final InputToken<Boolean> IN_RAINING = ofInput("raining", Boolean.class);

    @InputDoc(
            name = "duration",
            description = "その天候が続く期間（チック）を指定します。",
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
                            content = "この値が `0` の場合は, その天候が永久に続くことを意味します。"
                    )
            }
    )
    public static final InputToken<Integer> IN_DURATION = ofInput("duration", Integer.class);

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(WeatherChangeEvent.class);
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = this.getWorldNonNull(ctxt);

        int duration = ctxt.orElseInput(IN_DURATION, () -> 0);
        boolean changeToRaining;
        if (ctxt.hasInput(IN_RAINING))
            changeToRaining = ctxt.input(IN_RAINING);
        else
            changeToRaining = !world.hasStorm();

        world.setStorm(changeToRaining);
        world.setWeatherDuration(duration);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WeatherChangeEvent))
            return false;

        WeatherChangeEvent e = (WeatherChangeEvent) event;
        if (!super.checkMatchedWorld(ctxt, e.getWorld()))
            return false;

        return ctxt.ifHasInput(IN_RAINING, r -> e.toWeatherState() == r);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_RAINING, IN_DURATION);
    }
}
