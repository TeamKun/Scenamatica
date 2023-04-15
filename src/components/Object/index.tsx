import React from "react"
import styles from "./index.module.css"

export type ObjectElement = {
    name: string
    anchor?: string
    required?: boolean
    type: string | ObjectType
    type_anchor?: string
    description?: string
    default?: any
}

export enum ObjectType {
    OBJECT = "オブジェクト",
    ARRAY = "配列",
    STRING = "文字列",
    INTEGER = "整数値 (int)",
    LONG = "整数値 (long)",
    FLOAT = "単精度浮動小数点数",
    DOUBLE = "倍精度浮動小数点数",
    BOOLEAN = "真偽値",
    NULL = "null",
}

type ObjectsProps = {
    objects: ObjectElement[]
}

export const Object: React.FC<ObjectsProps> = ({ objects }) => {
    const elements = objects.map((element) =>{
        const name = element.anchor ? <a href={"#" + element.anchor}>{element.name}</a>: element.name
        const required = element.required ? <span className={styles.required}>必須</span>: "任意"

        console.log(typeof element.type_anchor, element.type_anchor)

        const typeNameStr = typeof element.type === "string" ? element.type : (element.type as ObjectType)
        const typeName = element.type_anchor ? <a href={"#" + element.type_anchor}>{typeNameStr}</a>: typeNameStr

        return (
            <tr className={styles.objects} key={element.name}>
                <td><code>{name}</code></td>
                <td><code>{typeName}</code></td>
                <td>{required}</td>
                <td>{element.description}</td>
                <td>{element.default ? <code>{element.default}</code> : "N/A"}</td>
            </tr>
        )
    })

    return (
        <>
            <table className={styles.objects}>
                <thead>
                <tr className={styles.objects + " " + styles.header}>
                    <th>キー</th>
                    <th>型</th>
                    <th>必須？</th>
                    <th>説明</th>
                    <th>デフォルト</th>
                </tr>
                </thead>
                <tbody>
                {elements}
                </tbody>
            </table>
        </>
    )
}
