import React from "react"
import type { Props } from "@theme/MDXComponents/Img"

export default function MDXImg(props: Props): JSX.Element {
  let size = {}
  let altStr = props.alt
  if (props.alt.includes("#")) {
    const sizeStr = props.alt.split("#")[1]
    const sizeArr = sizeStr.split("x")

    if (sizeArr[0] && sizeArr[1]) {
      size = {
        width: sizeArr[0] + "px",
        height: sizeArr[1] + "px",
      }
    } else if (sizeStr.startsWith("x")) {
      size = {
        height: sizeArr[1] + "px",
        width: "auto",
      }
    } else {
      size = {
        height: "auto",
        width: sizeArr[0] + "px",
      }
    }

    altStr = props.alt.split("#")[0]
  }

  return (
    // eslint-disable-next-line jsx-a11y/alt-text
    <img loading="lazy" style={size} {...props} alt={altStr} />
  )
}
