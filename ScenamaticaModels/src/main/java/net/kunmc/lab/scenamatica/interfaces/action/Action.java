package net.kunmc.lab.scenamatica.interfaces.action;

/**
 * 動作のインタフェースです。
 */
public interface Action<A extends ActionArgument>
{
    /**
     * 動作を実行します。
     */
    void execute(A argument);

    /**
     * 動作が行われたかどうかを返します。
     */
    boolean isExecuted(A argument);
}
