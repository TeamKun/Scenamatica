import React from "react"
import Admonition from "@theme/Admonition"
import Translate from "@docusaurus/Translate"

type DeprecationProps = {
  message?: string
  link?: string
  version?: string
  removeMode?: boolean
}

const Deprecation: React.FC<DeprecationProps> = ({ message, link, version, removeMode }) => {
  const versionString = version ?? "次のリリース"
  const messageBody = message
    ? message
    : removeMode
    ? `この機能は KPM ${versionString} で**<strong>削除</strong>**されます。`
    : `この機能は KPM ${versionString} から非推奨になります。`
  const linkString = link ? `詳細は <a href="${link}">${link}</a> を参照してください。` : ""

  const admonitionType = removeMode ? "caution" : "note"

  return (
    <Admonition
      type={admonitionType}
      title={
        <span>
          <Translate id={`theme.admonition.${admonitionType}`}>deprecation</Translate>
          <span> - </span>
          Deprecation
        </span>
      }
    >
      <p dangerouslySetInnerHTML={{ __html: messageBody }}></p>
      {linkString && <p dangerouslySetInnerHTML={{ __html: linkString }} />}
    </Admonition>
  )
}

export default Deprecation
