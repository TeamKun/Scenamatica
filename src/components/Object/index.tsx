import React from "react"
import styles from "./index.module.css"

export type ObjectElement = {
    name: string
    anchor?: string
    required?: boolean
    type: string | ObjectType
    type_anchor?: string
    type_link?: string
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
    const shouldShowDefaultValue = objects.some((element) => element.default)

    const elements = objects.map((element) =>{
        const name = element.anchor ? <a href={"#" + element.anchor}>{element.name}</a>: element.name
        const required = element.required ? <span className={styles.required}>必須</span>: "任意"

        const typeNameStr = typeof element.type === "string" ? element.type : (element.type as ObjectType)
        let typeName: string | JSX.Element = typeNameStr
        if (element.type_anchor)
            typeName = <a href={"#" + element.type_anchor}>{typeNameStr}</a>
        else if (element.type_link)
            typeName = <a href={element.type_link}>{typeNameStr}</a>

        return (
            <tr className={styles.objects} key={element.name}>
                <td><code>{name}</code></td>
                <td><code>{typeName}</code></td>
                <td>{required}</td>
                <td>{element.description}</td>
                {shouldShowDefaultValue ? <td>{element.default ? <code>{element.default}</code> : "N/A"}</td> : null}
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
                    {shouldShowDefaultValue ? <th>デフォルト</th> : null}
                </tr>
                </thead>
                <tbody>
                {elements}
                </tbody>
            </table>
        </>
    )
}
