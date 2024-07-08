import React from "react";
import styles from "./index.module.css"
import clsx from "clsx";


type ActionProps = {
  executable: boolean
  watchable: boolean
  requireable: boolean
}

const ActionIcon: React.FC<ActionProps> = ({
                                             executable,
                                             watchable,
                                             requireable,
                                           }) => {
  const icons = []
  if (executable)
    icons.push(<span className={clsx(styles.scenarioType, styles.execution)}>E</span>)
  if (watchable)
    icons.push(<span className={clsx(styles.scenarioType, styles.expectation)}>W</span>)
  if (requireable)
    icons.push(<span className={clsx(styles.scenarioType, styles.requirement)}>R</span>)

  return <>
    {icons}
  </>
}

export default ActionIcon
