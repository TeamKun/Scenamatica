import React from "react"
import styles from "./index.module.css"
import Link from "@docusaurus/Link";
import isInternalUrl from "@docusaurus/core/lib/client/exports/isInternalUrl";
import IconExternalLink from "@theme/Icon/ExternalLink";

type RequirementElement = {
    name: string
    link?: string
    version: string
    description?: string
}

type RequirementsProps = {
    requirements: RequirementElement[]
}

const Requirements: React.FC<RequirementsProps> = ({ requirements }) => {
    const elements = requirements.map((element) =>{
        const isInternalLink = element.link && isInternalUrl(element.link)
        const reqName = element.link ?
            <Link to={element.link}>
                {element.name}
                {!isInternalLink && <IconExternalLink />}
            </Link>:
            element.name
        const description = element.description ? element.description : ""
        return (
            <tr className={styles.requirements + " " + styles.body}>
                <td>{reqName}</td>
                <td>{element.version}</td>
                <td>{description}</td>
            </tr>
        )
    })

    return (
        <>
            <table className={styles.requirements}>
                <thead>
                <tr className={styles.requirements + " " + styles.header}>
                    <th>要件</th>
                    <th>要件名</th>
                    <th>摘要</th>
                </tr>
                </thead>
                <tbody>
                {elements}
                </tbody>
            </table>
        </>
    )
}

export default Requirements
