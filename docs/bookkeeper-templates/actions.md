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
import RequiredMark from "/src/components/RequiredMark";

import Admonition from '@theme/Admonition';
import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

#

## {{name}} - `{{id}}` {#action}

{{markdown (lineOf description "0")}}

{{#if category}}
<Link to="."><small>カテゴリ：{{$ (resolve category) "name"}}</small></Link>
{{/if}}

---

{{markdown description}}

{{#if (hasAdmonitionsContracts admonitions)}}
{{#if (hasAdmonitions admonitions "execute")}}
**アクション実行シナリオでの使用**
{{>admonitions admonitions=admonitions mode="execute"}}
{{/if}}
{{#if (hasAdmonitions admonitions "expect")}}
**アクション実行期待シナリオでの使用**
{{>admonitions admonitions=admonitions mode="expect"}}
{{/if}}
{{#if (hasAdmonitions admonitions "require")}}
**コンディション要求シナリオでの使用**
{{>admonitions admonitions=admonitions mode="require"}}
{{/if}}
{{else}}
{{>admonitions admonitions=admonitions}}
{{/if}}
{{#if super}}
<Admonition type="tip">
  {{#with (resolve super)}}
    このアクションは, アクション <Link to="{{path $reference}}">{{name}}(<kbd><code>{{id}}</code></kbd>)</Link>, およびその一部又はすべての入力・出力を継承しています。
  {{/with}}
</Admonition>
{{/if}}

### 基本情報 {#overview}

<table>
  <tbody>
    <tr>
      <td colspan="2"><center>**{{name}}**</center></td>
    </tr>
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
      <td>実行可能 (<kbd>{{#if executable}}<a href="?scenario-type=execute#inputs">execute</a>{{else}}execute{{/if}}</kbd>)</td>
      <td><AbleState {{#if executable}}able description="{{executable}}" {{/if}}/></td>
    </tr>
    <tr>
      <td>監視可能 (<kbd>{{#if expectable}}<a href="?scenario-type=expect#inputs">expect</a>{{else}}expect{{/if}}</kbd>)</td>
      <td><AbleState {{#if expectable}}able description="{{expectable}}" {{/if}}/></td>
    </tr>
    <tr>
      <td>要求可能 (<kbd>{{#if requireable}}<a href="?scenario-type=require#inputs">require</a>{{else}}require{{/if}}</kbd>)</td>
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
            {{#if javadoc_link}}
            <a href="{{javadoc_link}}">{{name}}</a><br />
            {{else}}
            {{name}}<br />
            {{/if}}
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
  **入力パラメータ一覧(<RequiredMark /> = 必須, クリックでコピー可)**
  {{#if (isEmpty inputs)}}
  <p>入力パラメータはありません。</p>
  {{else}}
  <table>
    <tbody>
      {{#each (sort inputs "name")}}
      {{#if (expr (not availableFor) "||" (contains availableFor ../mode))}}
      <tr>
      {{!-- ↑ availableFor が存在しないものまたは, アクションが指定されているもののみレンダリング。 --}}
      <td><code>{{#if (expr (isMultiLine description) "||" admonitions)}}<a href="#input-{{../mode}}-{{name}}">{{name}}</a>{{else}}{{name}}{{/if}}</code>{{#if (contains requiredOn ../mode)}}<RequiredMark />{{/if}}</td>
      <td>{{#with (resolveType type)}}{{#if (path $reference)}}<Link to="{{path $reference}}">{{name}}</Link>{{else}}{{name}}{{/if}}{{/with}}</td>
      <td>{{lineOf description "0"}}</td>
      </tr>
      {{/if}}
      {{/each}}
    </tbody>
  </table>
  {{/if}}
  {{!--インデントを変えるとパラメタが壊れる。--}}
  {{#each (sort inputs)}}
  {{#if (expr (not availableFor) "||" (contains availableFor ../mode))}}
  {{#if (expr (isMultiLine description) "||" admonitions)}}
  <Heading id="input-{{../mode}}-{{name}}" as="h3">{{name}}{{#if (contains requiredOn ../mode)}}<RequiredMark />{{/if}}</Heading>
  {{#if description}}
  {{markdown description}}
  {{/if}}
  {{>admonitions admonitions=admonitions mode=../mode}}
  <br />
  {{/if}}
  {{/if}}
  {{/each}}
</TabItem>
{{/inline}}
<Tabs groupId="scenario-type" queryString>
  {{#if executable}}
  {{> input mode="execute" mode_jp="実行"}}
  {{/if}}
  {{#if expectable}}
  {{> input mode="expect" mode_jp="監視"}}
  {{/if}}
  {{#if requireable}}
  {{> input mode="require" mode_jp="要求"}}
  {{/if}}
</Tabs>

### 出力 {#outputs}

{{#*inline "output"}}
<TabItem value="{{mode}}" label="{{mode_jp}}">
  **出力一覧(クリックでコピー可)**
  {{#if (isEmpty outputs)}}
  <p>出力はありません。</p>
  {{else}}
  <table>
    <tbody>
      {{#each (sort outputs "name")}}
      <tr>
      <td><code>{{#if (expr (isMultiLine description) "||" admonitions)}}<a href="#output-{{../mode}}-{{name}}">{{name}}</a>{{else}}{{name}}{{/if}}</code></td>
      <td>{{#with (resolveType type)}}{{#if (path $reference)}}<Link to="{{path $reference}}">{{id}}</Link>{{else}}{{id}}{{/if}}{{/with}}</td>
      <td>{{lineOf description "0"}}</td>
      </tr>
      {{/each}}
    </tbody>
  </table>
  {{/if}}
  {{#each (sort outputs "name")}}
  {{#if (expr (isMultiLine description) "||" admonitions)}}
  <Heading id="output-{{../mode}}-{{name}}" as="h3">{{name}}</Heading>
  {{markdown description}}
  {{>admonitions admonitions=admonitions mode=../mode}}
  {{/if}}
  <br />
  {{/each}}
</TabItem>
{{/inline}}
<Tabs groupId="scenario-type" queryString>
  {{#if executable}}
  {{> output mode="execute" mode_jp="実行"}}
  {{/if}}
  {{#if expectable}}
  {{> output mode="expect" mode_jp="監視"}}
  {{/if}}
  {{#if requireable}}
  {{> output mode="require" mode_jp="要求"}}
  {{/if}}
</Tabs>
