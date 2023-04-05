import React from "react"
import DocCard from "@theme/DocCard"
// @ts-ignore
import { useCurrentSidebarCategory, useLocalPathname } from "@docusaurus/theme-common/internal"

type BelowDocumentProps = {
    docId: string
    label?: string
    message?: string

    small?: boolean

    anchor?: string
}

const BelowDocument: React.FC<BelowDocumentProps> = ({ docId, label, message, small , anchor}) => {

    const smallMode = !small
        ? undefined
        : {
            width: "350px",
        }

    const displayMessage = message ? message : "詳しくは以下のドキュメントを参照してください："
    const path = "/" + useLocalPathname().split("/").slice(1, 2).join("/") + "/" + (docId.endsWith("/README") ? docId.slice(0, -7) : docId) +
        (anchor ? "#" + anchor : "")


    return (
        <>
            <p>{displayMessage}</p>
            <div style={smallMode}>
                <DocCard
                    item={{
                        type: "link",
                        label: label,
                        docId: docId,
                        href: path,
                    }}
                />
            </div>
        </>
    )
}

export default BelowDocument
