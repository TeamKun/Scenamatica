import React from "react"
import styles from "./index.module.css"
import {Object, ObjectElement} from "@site/src/components/Object";

type BukkitEvent = {
    name: string
    package: string
}

type ActionProps = {
    name: string
    description: string
    id: string
    events?: BukkitEvent | BukkitEvent[]
    executable?: boolean
    watchable?: boolean
    requireable?: boolean

    args?: ObjectElement[]
}

const EVENT_JAVADOC_LINK_BASE =  "https://jd.papermc.io/paper/1.16/"

const Action: React.FC<ActionProps> = ({name, description, id, events, executable, watchable, requireable, args}) => {
    const able = (value: boolean) => (
        <td className={value ? styles.able : styles.unable}>{value ? "はい" : "いいえ"}</td>
    )

    const createEventLink = (event: BukkitEvent) => (
        <a href={EVENT_JAVADOC_LINK_BASE + event.package.replace(/\./g, "/") + "/" + event.name + ".html"}>{event.name}</a>
    )

    const buildEventsComponent = () => <>
        <p className={styles.bigger}>このアクションは以下のイベントで呼び出されます。</p>
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
                    <td>実行可能</td>
                    {able(executable)}
                </tr>
                <tr>
                    <td>監視可能</td>
                    {able(watchable)}
                </tr>
                <tr>
                    <td>コンディションチェック可能</td>
                    {able(requireable)}
                </tr>
            </tbody>
        </table>
        {events ? buildEventsComponent() : <></>}

        <p className={styles.bigger}>{args ? name + " の引数は以下の通りです。" : "このアクションは引数を取りません。"}</p>

        {args ? <Object objects={args} /> : <></>}
    </>)
}


export default Action
