import React from "react"
import styles from "./index.module.css"
import {Object, ObjectElement} from "@site/src/components/Object";

type BukkitEvent = {
    name: string
    package: string
}

export class ScenarioType {
    public static readonly EXECUTE = new ScenarioType("実行", "#20a420")
    public static readonly EXPECT = new ScenarioType("監視", "#26c9a9")
    public static readonly REQUIRE = new ScenarioType("要求", "#c95d16")

    private readonly shortName: string
    private readonly color: string

    constructor(shortName: string, color: string) {
        this.shortName = shortName
        this.color = color
    }

    public toElement(): JSX.Element {
        return <code style={{color: this.color}}>{this.shortName}</code>
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

const Action: React.FC<ActionProps> = ({name, description, id, events, executable, watchable, requireable, args, outputs}) => {
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
        <p className={styles.bigger}>対応イベント</p>
        <ul>
            {Array.isArray(events) ? events.map(event => <li key={event.name}>{createEventLink(event)}</li>) :
                <li>{createEventLink(events)}</li>}
        </ul>
    </>

    return (<>
        <p>{description}</p>
        <table className={styles.action}>
            <tbody>
                <tr>
                    <td className={styles.name} colSpan={2}>{name}</td>
                </tr>
                <tr>
                    <td>ID(指定用名)</td>
                    <td><kbd><code>{id}</code></kbd></td>
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

        <p className={styles.bigger}>{args ? name + " の引数" : "*（引数なし）*"}</p>

        {args ? <Object objects={args} /> : <></>}

        <p className={styles.bigger}>{outputs ? name + " の出力" : "*（出力なし）*"}</p>

        {outputs ? <Object objects={outputs} /> : <></>}
    </>)
}


export default Action
