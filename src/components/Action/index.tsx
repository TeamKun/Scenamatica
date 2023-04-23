import React from "react"
import styles from "./index.module.css"
import {Object, ObjectElement} from "@site/src/components/Object";

type ActionProps = {
    name: string
    description: string
    id: string
    executable?: boolean
    watchable?: boolean
    requireable?: boolean

    args?: ObjectElement[]
}

const Action: React.FC<ActionProps> = ({name, description, id, executable, watchable, requireable, args}) => {
    const able = (value: boolean) => (
        <td className={value ? styles.able : styles.unable}>{value ? "はい" : "いいえ"}</td>
    )

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
        <h4>{args ? name + " の引数は以下の通りです。" : "このアクションは引数を取りません。"}</h4>

        {args ? <Object objects={args} /> : <></>}
    </>)
}


export default Action
