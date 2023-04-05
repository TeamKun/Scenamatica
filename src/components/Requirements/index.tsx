import React from "react"
import styles from "./index.module.css"

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
        const reqName = element.link ? <a href={element.link}>{element.name}</a>: element.name
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
