import React from "react"
import Admonition from "@theme/Admonition"
import Translate from "@docusaurus/Translate"

type SinceProps = {
  version: string
  newMode?: boolean
  message?: string
  link?: string
}

const Since: React.FC<SinceProps> = ({version, newMode, message, link}) => {
  const messageBody = message
    ? message
    : newMode
      ? `✨Scenamatica ${version} の新機能です！✨`
      : `この機能は Scenamatica ${version} で強化されました！`
  const linkString = link ? `詳細は <a href="${link}">${link}</a> を参照してください。` : ""

  return (
    <Admonition
      type="info"
      title={
        <span>
          <Translate id={`theme.admonition.info`}>since</Translate>
          <span> - </span>
          Since
        </span>
      }
    >
      <p dangerouslySetInnerHTML={{__html: messageBody}}></p>
      {linkString && <p dangerouslySetInnerHTML={{__html: linkString}}/>}
    </Admonition>
  )
}

export default Since
