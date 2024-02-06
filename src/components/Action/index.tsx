import React from "react"
import styles from "./index.module.css"
import {Object, ObjectElement} from "@site/src/components/Object";
import clsx from "clsx";
import CopyableText from "@site/src/components/CopyableText";

type BukkitEvent = {
    name: string
    package: string
}

export class ScenarioType {
    public static readonly EXECUTE = new ScenarioType("実行", styles.execution)
    public static readonly EXPECT = new ScenarioType("監視", styles.expectation)
    public static readonly REQUIRE = new ScenarioType("要求", styles.requirement)

    private readonly shortName: string
    private readonly clazz: string

    constructor(shortName: string, clazz: string) {
        this.shortName = shortName
        this.clazz = clazz
    }

    public toElement(): JSX.Element {
        return <code className={clsx(styles.scenarioType, this.clazz)}>{this.shortName}</code>
    }
}

export type ActionArgument = ObjectElement & {
    available?: ScenarioType[]
}

export type ActionOutput = ObjectElement & {
    available?: ScenarioType[]
}

type ActionProps = {
    name: string
    description: string
    id: string
    events?: BukkitEvent | BukkitEvent[]
    executable?: boolean | string
    watchable?: boolean | string
    requireable?: boolean | string

    args?: ActionArgument[]
    outputs?: ActionOutput[]
}

const EVENT_JAVADOC_LINK_BASE =  "https://jd.papermc.io/paper/1.16/"

const Action: React.FC<ActionProps> = ({
                                           name,
                                           description,
                                           id,
                                           events,
                                           executable,
                                           watchable,
                                           requireable,
                                           args,
                                           outputs
                                       }) => {
    const able = (value: boolean | string) => {
        if (typeof value === "boolean" || value === undefined) {
            return <td className={value ? styles.able : styles.unable}><span className={styles.label}>{value ? "はい" : "いいえ"}</span></td>
        } else {
            return <td className={styles.able}><span className={styles.label}>はい</span>：{value}</td>
        }
    }

    const createEventLink = (event: BukkitEvent) => (
        <a href={EVENT_JAVADOC_LINK_BASE + event.package.replace(/\./g, "/") + "/" + event.name + ".html"}>{event.name}</a>
    )

    const buildEventsComponent = () => <>
        <p className={clsx(styles.bigger, "compatibleEvents")}>対応イベント</p>
        <ul>
            {Array.isArray(events) ? events.map(event => <li key={event.name}>{createEventLink(event)}</li>) :
                <li>{createEventLink(events)}</li>}
        </ul>
    </>

    return (<>
        <p className={"actionDescription"}>{description}</p>
        <table className={styles.action}>
            <tbody>
                <tr>
                    <td className={styles.name} colSpan={2}>{name}</td>
                </tr>
                <tr>
                    <td>ID(指定用名)</td>
                    <td className={"actionID"}>
                        <CopyableText domID={id}>{id}</CopyableText>
                    </td>
                </tr>
                <tr>
                    <td>実行可能 (<kbd>execute</kbd>)</td>
                    {able(executable)}
                </tr>
                <tr>
                    <td>監視可能 (<kbd>expect</kbd>)</td>
                    {able(watchable)}
                </tr>
                <tr>
                    <td>要求可能 (<kbd>require</kbd>)</td>
                    {able(requireable)}
                </tr>
            </tbody>
        </table>
        {events ? buildEventsComponent() : <></>}

        <p className={clsx(styles.bigger, "actionArguments")}>{args ? name + " の引数" : "*（引数なし）*"}</p>

        {args ? <Object objects={args} /> : <></>}

        <p className={clsx(styles.bigger, "actionOutputs")}>{outputs ? name + " の出力" : "*（出力なし）*"}</p>

        {outputs ? <Object objects={outputs} /> : <></>}
    </>)
}


export default Action
