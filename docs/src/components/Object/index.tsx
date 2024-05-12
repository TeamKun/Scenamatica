import React from "react"
import styles from "./index.module.css"
import {ActionArgument, ScenarioType} from "@site/src/components/Action";
import Link from "@docusaurus/Link";
import {isType, Type} from "@site/src/components/Object/Types";

export type ObjectElement = {
  name: string | string[]
  anchor?: string
  required?: boolean | ScenarioType[] | string
  type: (string | Type)[] | string | Type
  type_anchor?: string
  type_link?: string
  description?: string
  default?: any
  support?: VersionSupport
}

type ObjectsProps = {
  objects: ObjectElement[]
}

const isActionArgument = (element: ObjectElement): element is ActionArgument => {
  return "available" in element
}

export type VersionSupport = {
  since?: string
  until?: string
}

export const compatibleVersionTag = (version?: VersionSupport) => {
  if (!version || (!version.since && !version.until)) {
    return <span className={styles.label}>全バージョン</span>
  }

  const since = version.since ? <><span>Minecraft </span><code>{version.since}</code></> : ""
  const until = version.until ? <><code>{version.until}</code></> : ""

  return <span className={styles.version}>{since} ～ {until}</span>
}

const getTypeReferenceComponent = (element: ObjectElement): JSX.Element => {
  const type = element.type
  if (Array.isArray(type)) {
    return (
      <span>
        {type.map((type, index) => {
          const {typeNameStr, typeLink} = getOneTypeReferenceComponent(type)
          const component = typeLink ? <Link to={typeLink} key={index}>{typeNameStr}</Link> : <>{typeNameStr}</>
          if (index > 0)
            return <>{" | "}{component}</>
          else
            return component
        })}
      </span>
    )
  } else {
    const {typeNameStr, typeLink} = getOneTypeReferenceComponent(type, element)
    return typeLink ? <Link to={typeLink}>{typeNameStr}</Link> : <>{typeNameStr}</>
  }
}

const getOneTypeReferenceComponent = (type: string | Type, element?: ObjectElement): {
  typeNameStr: string,
  typeLink: string | null
} => {
  let typeNameStr: string
  let typeLink: string
  if (isType(type)) {
    typeNameStr = type.displayString
    typeLink = type.referenceString
  } else {
    typeNameStr = type
    if (element) {
      if (element.type_anchor)
        typeLink = "#" + element.type_anchor
      else if (element.type_link)
        typeLink = element.type_link
    }
  }


  return {typeNameStr, typeLink}
}

export const Object: React.FC<ObjectsProps> = ({objects}: { objects: ObjectElement[] }) => {
  const shouldShowRequiringState = objects.some((element) => element.required)
  const shouldShowDefaultValue = objects.some((element) => element.default)
  const shouldShowAvailableFor = objects.some((element) => isActionArgument(element) && element.available.length > 0)
  const shouldShowAvailability = objects.some((element) => element.support)

  const elements = objects.map((element) => {
    const name = element.anchor ? <a href={"#" + element.anchor}>{element.name}</a> : element.name
    let required: JSX.Element | null
    if (!element.required)
      required = <span>-</span>
    else if (typeof element.required === "string")
      required = <span className={styles.required}>{element.required}</span>
    else if (Array.isArray(element.required)) {
      required = <span className={styles.required}>
                {element.required.map((type, index) => {
                  if (index > 0)
                    return <>{" "}{type.toElement()}</>
                  else
                    return <>{type.toElement()}</>
                })}
            </span>
    } else
      required = <span className={styles.required}>必須</span>

    const typeComponent = getTypeReferenceComponent(element)


    let availableFor: JSX.Element | null = null
    if (isActionArgument(element) && element.available.length > 0) {
      availableFor = <span>
                {element.available.map((type, index) => {
                  if (index > 0)
                    return <>{" "}{type.toElement()}</>
                  else
                    return <>{type.toElement()}</>
                })}
            </span>
    }

    // <code> で name をラップ
    const nameElements = Array.isArray(name) ? name.map((name, index) => {
      if (index > 0)
        return <><code>{name}</code></>
      else
        return <code>{name}</code>
    }).reduce((prev: JSX.Element, curr: JSX.Element) => {
      return <>{prev} | {curr}</>
    }) : <code>{name}</code>

    return (
      <tr className={styles.objects} key={element.name.toString()}>
        <td>{nameElements}</td>
        <td><code>{typeComponent}</code></td>
        {shouldShowRequiringState ? <td className={styles.requiredStatus}>{required}</td> : null}
        <td className={styles.description}>{element.description}</td>
        {shouldShowDefaultValue ? <td className={element.default ? null : styles.none}>{element.default ?
          <code>{element.default}</code> : "-"}</td> : null}
        {shouldShowAvailableFor ? <td className={styles.scenarioType}>{availableFor ? availableFor :
          <code><span className={styles.allType}>すべて</span></code>}</td> : null}
        {shouldShowAvailability ? <td>{compatibleVersionTag(element.support)}</td> : null}
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
          {shouldShowRequiringState ? <th>必須？</th> : null}
          <th>説明</th>
          {shouldShowDefaultValue ? <th>デフォルト</th> : null}
          {shouldShowAvailableFor ? <th>利用可能</th> : null}
          {shouldShowAvailability ? <th>対応バージョン</th> : null}
        </tr>
        </thead>
        <tbody>
        {elements}
        </tbody>
      </table>
    </>
  )
}
