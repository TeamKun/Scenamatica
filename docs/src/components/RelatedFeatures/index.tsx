import React from "react"
import DocCard from "@theme/DocCard"
// @ts-ignore
import {useDocById, useLocalPathname} from "@docusaurus/theme-common/internal"

type RelatedFeatureProps = {
  docNames: string[]
  headerLevel?: number
}

const RelatedFeatures: React.FC<RelatedFeatureProps> = ({docNames, headerLevel = 3}) => {
  const docs = docNames.map((docName) => {
    let doc;
    try {
      doc = useDocById("use-scenamatica/features/" + docName)
    } catch (e) {
      doc = useDocById(docName)
    }

    return doc
  })

  const Header = `h${headerLevel}` as keyof JSX.IntrinsicElements

  const DocCards = docs.map((doc) => {
    let path = useLocalPathname().split("/").slice(0, 2).join("/") + "/" + doc.id

    return (
      <span style={{display: "inline-block", width: "250px", marginRight: "1rem"}} key={"related-" + doc.id}>
        <DocCard
          item={{
            type: "link",
            label: doc.title,
            docId: doc.id,
            href: path,
          }}
        />
      </span>
    )
  })

  return (
    <div>
      {!headerLevel ? undefined : <Header>関連項目</Header>}
      {DocCards}
    </div>
  )
}

export default RelatedFeatures
