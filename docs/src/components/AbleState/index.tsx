import clsx from "clsx";
import React from "react";
import styles from "./index.module.css"

type AbleStateProps = {
  able: boolean
  description?: string
}


const AbleState: React.FC<AbleStateProps> = ({
                                               able = false,
                                               description
                                             }) => {
  const ableClass = able ? styles.able : styles.unable
  const ableLabel = able ? "はい" : "いいえ"

  return <>
    <span className={clsx(styles.ableLabel, ableClass)}>{ableLabel}</span>
    {description ? <><span>：</span><span>{description}</span></> : undefined}
  </>
}

export default AbleState
