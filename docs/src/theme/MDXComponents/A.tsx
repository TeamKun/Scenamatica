import React from 'react';
import Link from '@docusaurus/Link';
import isInternalUrl from "@docusaurus/core/lib/client/exports/isInternalUrl";
import IconExternalLink from '@theme/Icon/ExternalLink';
export default function MDXA(props) {
  const isExternal = isInternalUrl(props.href)

  return <Link
      {...props}
  >
    {props.children}
    {!isExternal && <IconExternalLink />}
  </Link>
}
