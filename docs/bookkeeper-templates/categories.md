import ActionIcon from "/src/components/ActionIcon";
import Link from "@docusaurus/Link";

# {{name}}関連

{{description}}

---

## 概要 {#overview}

このページは、 `{{name}}` に関連するコンテンツを一覧表示しています。

## 一覧 {#list}

{{#each children}}
- <ActionIcon {{#if executable}}execute{{/if}} {{#if watchable}}watch{{/if}} {{#if requireable}}require{{/if}} /> <Link to="{{path $reference}}">{{name}} - `{{id}}`</Link>  
  {{description}}

{{/each}}
