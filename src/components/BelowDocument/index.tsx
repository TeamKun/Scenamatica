import React from "react"
import DocCard from "@theme/DocCard"
// @ts-ignore
import { useCurrentSidebarCategory, useLocalPathname } from "@docusaurus/theme-common/internal"

type BelowDocumentProps = {
    docId: string
    label?: string
    message?: string

    small?: boolean
    tutorial?: boolean

    anchor?: string
}

const BelowDocument: React.FC<BelowDocumentProps> = ({ docId, label, message, small, tutorial, anchor}) => {

    const style = !small
        ? {
            marginBottom: "10px"
        } : {
            width: "350px",
    }

    const defaultDisplayMessage =
        tutorial ? "これについての実践的なチュートリアルは以下のドキュメントを参照してください。" : "詳しくは以下のドキュメントを参照してください："
    const displayMessage = message ? message : defaultDisplayMessage
    const path = "/" + useLocalPathname().split("/").slice(1, 2).join("/") + "/" + (docId.endsWith("/README") ? docId.slice(0, -7) : docId) +
        (anchor ? "#" + anchor : "")


    return (
        <>
            <p>{displayMessage}</p>
            <div style={style}>
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
