import React from "react"
import styles from "./index.module.css"
import {ActionArgument} from "@site/src/components/Action";

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

const isActionArgument = (element: ObjectElement): element is ActionArgument => {
    return "available" in element
}

export const Object: React.FC<ObjectsProps> = ({ objects }) => {
    const shouldShowDefaultValue = objects.some((element) => element.default)
    const shouldShowAvailableFor = objects.some((element) => isActionArgument(element) && element.available.length > 0)

    const elements = objects.map((element) =>{
        const name = element.anchor ? <a href={"#" + element.anchor}>{element.name}</a>: element.name
        const required = element.required ? <span className={styles.required}>必須</span>: "任意"

        const typeNameStr = typeof element.type === "string" ? element.type : (element.type as ObjectType)
        let typeName: string | JSX.Element = typeNameStr
        if (element.type_anchor)
            typeName = <a href={"#" + element.type_anchor}>{typeNameStr}</a>
        else if (element.type_link)
            typeName = <a href={element.type_link}>{typeNameStr}</a>

        let availableFor: JSX.Element | null = null
        if (isActionArgument(element) && element.available.length > 0) {
            for (const type of element.available) {
                if (availableFor)
                    availableFor = <>{availableFor} | <code>{type}</code></>
                else
                    availableFor = <code>{type}</code>
            }
        }

        return (
            <tr className={styles.objects} key={element.name}>
                <td><code>{name}</code></td>
                <td><code>{typeName}</code></td>
                <td>{required}</td>
                <td>{element.description}</td>
                {shouldShowDefaultValue ? <td>{element.default ? <code>{element.default}</code> : "N/A"}</td> : null}
                {shouldShowAvailableFor ? <td>{availableFor ? availableFor : "N/A"}</td> : null}
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
                    {shouldShowAvailableFor ? <th>利用可能</th> : null}
                </tr>
                </thead>
                <tbody>
                {elements}
                </tbody>
            </table>
        </>
    )
}
