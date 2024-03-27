import {Children, JSX} from "react";
import React from "react"
import styles from "./index.module.css"

const COPY_MESSAGE = "コピーしました"
const DOM_PREFIX = "copyable--"

const padCopyMessage = (message: string) => {
    const spaces = message.length - COPY_MESSAGE.length / 2   // 全部全角なので
    if (spaces <= 0)
        return COPY_MESSAGE
    return "!".repeat(spaces / 4) + COPY_MESSAGE + "!".repeat(spaces / 4)
}



const copyID = (text: string, domID) => {
    navigator.clipboard.writeText(text).then(r => {
        const dom = document.getElementById(domID)
        if (!dom)
            return;
        const originalText = dom.innerHTML
        if (originalText.includes(COPY_MESSAGE))
            return

        dom.classList.remove(styles.pointer)
        dom.classList.add(styles.defaultPointer)
        dom.innerText = padCopyMessage(text)
        setTimeout(() => {
            if (dom)
            {
                dom.innerText = originalText
                dom.classList.add(styles.pointer)
                dom.classList.remove(styles.defaultPointer)
            }
        }, 1000)
    })
}

const CopyableText = (props:{domID: string, children: JSX.Element[] | JSX.Element | string }): JSX.Element => {
    const identifier = Math.random() * 10000
    const domID = DOM_PREFIX + identifier + "-" + props.domID

    if (typeof props.children === "string")
        return (
            <kbd className={styles.id} title={"クリックしてコピー！"}
                 onClick={() => copyID(props.children as string, domID)}>
                <code id={domID} className={styles.pointer}>{props.children}</code>
            </kbd>
        )
    else if (Array.isArray(props.children))
        return props.children.map(child => {
            if (child.props)
                child.props.onClick = () => copyID(child.innerText, domID)
        })
    else {
        const casted = props.children as JSX.Element
        return <span onClick={() => copyID(props.children as string, domID)}>{props.children}</span>
    }
}

export default CopyableText
