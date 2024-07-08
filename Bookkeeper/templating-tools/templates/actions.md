---
title: {{name}}
sidebar_label: {{name}}
keywords: [Scenamatica, アクション, action, {{name}}, {{id}}{{#if events}}{{#each events}}, {{$ (resolve this) "name"}}{{/each}}{{/if}}]
custom_edit_url: ""
sidebar_position: {{orderOf $reference}}
---

import AbleState from "/src/components/AbleState";
import ActionIcon from "/src/components/ActionIcon";
import CopyableText from "/src/components/CopyableText";
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

#

## {{name}} - `{{id}}`

{{lineOf description "0"}}

{{#if category}}
<Link to="."><small>カテゴリ：{{$ (resolve category) "name"}}</small></Link>
{{/if}}

---

{{markdown description}}

{{#if admonitions}}
{{#each admonitions}}
:::{{type}} {{#if title}}{{title}}{{/if}}
{{markdown content}}
:::
{{/each}}
{{/if}}

### 基本情報 {#overview}

<table>
  <tbody>
    <tr>
      <td>ID(指定用名)</td>
      <td><CopyableText domID="{{id}}">{{id}}</CopyableText></td>
    </tr>
    {{#if category}}
    <tr>
      <td>カテゴリ</td>
      <td>{{$ (resolve category) "name"}}</td>
    </tr>
    {{/if}}
    <tr>
      <td>実行可能 (<kbd><a href="?scenario-type=execute#inputs">execute</a></kbd>)</td>
      <td><AbleState {{#if executable}}able description="{{executable}}" {{/if}}/></td>
    </tr>
    <tr>
      <td>監視可能 (<kbd><a href="?scenario-type=expect#inputs">expect</a></kbd>)</td>
      <td><AbleState {{#if watchable}}able description="{{watchable}}" {{/if}}/></td>
    </tr>
    <tr>
      <td>要求可能 (<kbd><a href="?scenario-type=require#inputs">require</a></kbd>)</td>
      <td><AbleState {{#if requireable}}able description="{{requireable}}" {{/if}}/></td>
    </tr>
    {{#if events}}
    <tr>
      <td>対応イベント</td>
      <td>
        <ul>
        {{#each events}}
        {{#with (resolve this)}}
          <li>
            <a href="{{javadoc_link}}">{{name}}</a><br />
            {{#if description}}
            {{markdown description "12"}}
            {{/if}}
          </li>
        {{/with}}
        {{/each}}
        </ul>
       </td>
     </tr>
     {{/if}}
    {{#if (expr supports_since "||" supports_until)}}
    <tr>
      <td>互換バージョン</td>
      <td>{{#if supports_since}}Minecraft {{supports_since}}{{/if}} ～ {{#if supports_until}}Minecraft {{supports_until}}{{/if}}</td>
    </tr>
    {{/if}}
  </tbody>
</table>

### 入力パラメータ {#inputs}
{{#*inline "input"}}
<TabItem value="{{mode}}" label="{{mode_jp}}">
  **入力パラメータ一覧**
  <table>
    <tbody>
      {{#each inputs}}
      {{#if (expr (not availableFor) "||" (contains availableFor ../mode))}}
      <tr>
      {{!-- ↑ availableFor が存在しないものまたは, アクションが指定されているもののみレンダリング。 --}}
      <td><CopyableText domID="{{name}}">{{name}}</CopyableText></td>
      <td>{{#with (resolveType type)}}{{#if (path $reference)}}<Link to="{{path $reference}}">{{name}}</Link>{{else}}{{name}}{{/if}}{{/with}}</td>
      <td>{{lineOf description "0"}}</td>
      </tr>
      {{/if}}
      {{/each}}
    </tbody>
  </table>
  {{!--インデントを変えるとパラメタが壊れる。--}}
  {{#each inputs}}
  {{#if (expr (not availableFor) "||" (contains availableFor ../mode))}}
  <Heading id="input-{{../mode}}-{{name}}" as="h3">{{name}}</Heading>
  {{#if description}}
  {{markdown description}}
  {{/if}}
  {{/if}}
  {{/each}}
</TabItem>
{{/inline}}
<Tabs groupId="scenario-type" queryString>
  {{#if executable}}
  {{> input mode="execute" mode_jp="実行"}}
  {{/if}}
  {{#if watchable}}
  {{> input mode="watch" mode_jp="監視"}}
  {{/if}}
  {{#if requireable}}
  {{> input mode="require" mode_jp="要求"}}
  {{/if}}
</Tabs>
