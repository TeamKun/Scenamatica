import React from "react";
import styles from "./index.module.css"
import clsx from "clsx";


type ActionProps = {
  execute: boolean
  expect: boolean
  require: boolean
}

const ActionIcon: React.FC<ActionProps> = ({
                                             execute = false,
                                             expect = false,
                                             require = false,
                                           }) => {
  const icons = []
  if (execute)
    icons.push(<span className={clsx(styles.scenarioType, styles.execution)}>実行</span>)
  if (expect)
    icons.push(<span className={clsx(styles.scenarioType, styles.expectation)}>期待</span>)
  if (require)
    icons.push(<span className={clsx(styles.scenarioType, styles.requirement)}>要求</span>)

  return <>
    {icons}
  </>
}

export default ActionIcon
