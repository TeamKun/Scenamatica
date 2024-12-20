---
title: {{name}}
sidebar_label: {{name}}
keywords: [Scenamatica, 型, type, {{name}}, {{id}}]
custom_edit_url: ""
sidebar_position: {{orderOf $reference}}
---

import AbleState from "/src/components/AbleState";
import ActionIcon from "/src/components/ActionIcon";
import CopyableText from "/src/components/CopyableText";
import RequiredMark from "/src/components/RequiredMark";

import Admonition from '@theme/Admonition';
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

#

## {{name}} {{#if enums}}列挙{{/if}}型 {#type}

{{#if description}}
{{markdown (lineOf description "0")}}
{{else}}
{{#if enums}}
自動生成された列挙型に関するページです。
{{/if}}
{{/if}}
{{#if category}}

<Link to="."><small>カテゴリ：{{$ (resolve category) "name"}}</small></Link>
{{/if}}

---
{{#if description}}
{{markdown description}}
{{/if}}
{{>admonitions admonitions=admonitions}}
{{#if super}}
<Admonition type="tip">
  {{#with (resolve super)}}
    この型は, 型 <Link to="{{path $reference}}">{{name}}</Link>, およびその一部又はすべてのプロパティを継承しています。
  {{/with}}
</Admonition>
{{/if}}
{{#if enums}}

### 値の一覧 {#values}

<table>
  <tbody>
  {{#with (groupingWith (sort enums) "3")}}
    {{#each this}}
    <tr>
      {{#each this}}
      <td><CopyableText domID="{{this}}">{{this}}</CopyableText></td>
      {{/each}}
    </tr>
    {{/each}}
  {{/with}}
  </tbody>
</table>

{{else}}
### プロパティの一覧 {#properties}

{{#if (expr supportsSince "||" supportsUntil)}}対応 Minecraft バージョン：{{#if supportsSince}}v{{normalizeMCVersion supportsSince}}{{/if}}～{{#if supportsUntil}}v{{normalizeMCVersion supportsUntil}}{{/if}}{{/if}}

<table>
  <tbody>
  {{#each (sort properties "name")}}
  <tr>
      <td><code>{{#if (expr supportsSince "||" supportsUntil)}}<i>{{/if}}{{#if (expr (expr (isMultiLine description) "||" admonitions) "||" pattern)}}<a href="#property-{{name}}">{{name}}</a>{{else}}{{name}}{{/if}}{{#if (expr supportsSince "||" supportsUntil)}}</i></code> - {{else}}</code>{{/if}}{{#if (expr supportsSince "||" supportsUntil)}}({{#if supportsSince}}v{{normalizeMCVersion supportsSince}}{{/if}}～{{#if supportsUntil}}v{{normalizeMCVersion supportsUntil}}{{/if}}){{/if}}</td>
      <td>{{#with (resolveType type)}}{{#if (path $reference)}}<Link to="{{path $reference}}">{{/if}}{{id}}{{#if (path $reference)}}</Link>{{/if}}{{#if ../array}}[]{{/if}}{{/with}}</td>
      <td>{{#if description}}{{markdown (lineOf description "0")}}{{/if}}</td>
  </tr>
  {{/each}}
  </tbody>
</table>

{{#each (sort properties "name")}}
{{#if (expr (expr (isMultiLine description) "||" admonitions) "||" pattern)}}
### `{{name}}` {#property-{{name}} }

{{markdown description}}
{{#if pattern}}

パターン：<code>{{pattern}}</code>
{{/if}}

{{>admonitions admonitions=admonitions}}
{{/if}}
{{/each}}

{{/if}}
 
