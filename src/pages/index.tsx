import React from "react"
import clsx from "clsx"
import Link from "@docusaurus/Link"
import useDocusaurusContext from "@docusaurus/useDocusaurusContext"
import Layout from "@theme/Layout"
import Tabs from "@theme/Tabs"
import TabItem from "@theme/TabItem"

import styles from "./index.module.css"
import Character from "@site/src/components/Home/Character";
import Tagline from "@site/src/components/Home/Tagline";
import Card from "@site/src/components/Home/Card";
import CodeBlock from '@theme/CodeBlock';
import {faChevronDown, faHeartPulse, faLanguage, faListCheck, faUsers} from "@fortawesome/free-solid-svg-icons"
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faGithub} from "@fortawesome/free-brands-svg-icons";

const EXAMPLE_CODE = `scenamatica: "0.8.0"
name: "test-hoge-success"
description: "hoge 機能の正常系テスト"

context:
  actors:
  - name: Actor1
    permissions:
    - "hogeplugin.commands.hoge"
  stage:
    env: NETHER
    seed: 114514

on:
- type: on_load
- type: manual_dispatch

# highlight-start
scenario:
  # Actor1 が /hoge を実行する。
- type: execute
  action: command_dispatch
  with:
    command: "/hoge"
    sender: Actor1
  # fuga というメッセージが（Actor1 に）表示される。
- type: expect
  action: message
  with:
    content: "fuga"
    recipient: "$\{scenario.scenario.0.sender}"
  # Actor1 が死亡する。
- type: expect
  action: player_death
  with:
    target: "$\{scenario.scenario.0.sender}"
# highlight-end
`

function Characters(): JSX.Element {
    return (
        <div className={styles.characters}>
            <Character name={"logo2"} rotate={-25} style={{top: "60px", left: "30px"}}/>
            <Character name={"logo2"} rotate={25} style={{top: "60px", right: "30px"}}/>

            <Character name={"logo3"} rotate={-20} style={{left: "70px", top: "230px"}}/>
            <Character name={"logo3"} rotate={20} style={{right: "70px", top: "230px"}}/>

            <Character name={"logo3"} rotate={-25} style={{left: "30px", top: "400px"}}/>
            <Character name={"logo3"} rotate={25} style={{right: "30px", top: "400px"}}/>
        </div>
    )
}

function Buttons(): JSX.Element {
    const context = useDocusaurusContext()

    return (
        <div className={styles.buttons}>
            <Link className={clsx(styles.button, " button button--secondary button--lg")} to="/docs/home">
                詳しくみる &gt;
            </Link>
            <Link className={clsx(styles.button, styles.buttonGitHub, " button button--secondary button--lg")}
                  href={`https://github.com/${context.siteConfig.organizationName}/${context.siteConfig.projectName}`}>
                <FontAwesomeIcon
                    icon={faGithub}
                    width={18}
                    height={18}
                /> GitHub&gt;
            </Link>
        </div>
    )
}

function Features(): JSX.Element {
    return (
        <div className={styles.features}>
            <Card
                title={"YAML でシナリオをかんたん定義"}
                description={"YAML でシナリオを直感的に記述しましょう！"}
                emoji={faListCheck}
                emojiColor={"#129182"}
                backgroundColor={"#0e3810"}
                link={"#topics-scenario-yaml"}
            />
            <Card
                title={"ローカライズされたインタフェース"}
                description={"もちろん関西弁にも対応しています！"}
                emoji={faLanguage}
                emojiColor={"#f5bd46"}
                backgroundColor={"#ee2514"}
                link={"#topics-localize"}
            />
            <Card
                title={"CI-CD との統合"}
                description={"プラグインの品質を継続的に監視しましょう。"}
                emoji={faHeartPulse}
                emojiColor={"#ddd"}
                backgroundColor={"#113151"}
                link={"#topics-ci-cd-integration"}
            />
            <Card
                title={"テスト環境をかんたん構築"}
                description={"テストのために、クライアントをわざわざ 人数分起動する必要はもうありません！"}
                emoji={faUsers}
                emojiColor={"#559955"}
                backgroundColor={"#aaaa21"}
                link={"#topics-build-test-environment"}
            />
        </div>
    )
}

function Appeal(props: {right?: boolean, children: JSX.Element[]}, ): JSX.Element {
    const desc = props.children[props.right ? 0 : 1]
    const view = props.children[props.right ? 1 : 0]

    return (
        <div className={clsx(styles.appeal, styles.topic)}>
            <div className={props.right ? styles.topicDesc: null}>
                <div className={props.right ? styles.right : styles.left}>
                    {desc}
                </div>
            </div>
            <div className={props.right ? null : styles.topicDesc}>
                {view}
            </div>
        </div>
    )
}

function Appeals(): JSX.Element {
    return (
        <>
            <Appeal right>
                <div>
                    <h2 id={"topics-scenario-yaml"}>YAML でシナリオをかんたん定義</h2>
                    <p>
                        Scenamatica のシナリオは、右のようなのかんたんな YAML で定義されます。
                        この例では、 仮想的なプレイヤにネザーでコマンドを実行してもらい、
                        それによって発火したプラグインの機能のふるまいを検証しています。
                    </p>
                    <p>
                        YAML でシナリオを定義することで、 Java でテストを書くよりも、より簡潔に、かつより直感的にテストを記述できます。
                        さらに、非プログラマの方でもシナリオを書けるため、チームでのプラグイン開発がはかどります。
                    </p>
                    <p>Scenamatica は、PaperMC プラグインのテスト駆動開発にはもってこいです！</p>
                </div>
                <div className={styles.exampleCode}>
                    <div style={{position: "absolute", width: "100%"}}>
                        <CodeBlock
                            language={"yaml"}
                            showLineNumbers
                        >{EXAMPLE_CODE}
                        </CodeBlock>
                    </div>
                </div>
            </Appeal>
            <Appeal>
                <div style={{marginLeft: "10px"}}>
                    <h2 id={"topics-localize"}>ローカライズされたインタフェース</h2>
                    Scenamatica は、日本語をはじめとする数種類の言語に対応しています。
                    <p>特に、プラグインの出力は完全にローカライズされています！</p>
                    <h3>対応している言語</h3>
                    <ul>
                        <li> 日本語（日本）</li>
                        <li> English（US）</li>
                        <li> 関西弁（関西やで）</li>
                    </ul>

                    <p >
                        ※Scenamatica では、現在翻訳してくれるとても優しい方を募集しています！
                    </p>
                    <span>詳しくは上の <FontAwesomeIcon icon={faGithub}/> ボタンからリポジトリをご覧ください。</span>
                </div>
                <Tabs className={clsx(styles.imageTabs, styles.left)}>
                    <TabItem value="ja_JP" label="日本語">
                        <img src="/img/langs/ja_JP.png" alt="日本語" width={"100%"}/>
                    </TabItem>
                    <TabItem value="en_US" label="English">
                        <img src="/img/langs/en_US.png" alt="English" width={"100%"}/>
                    </TabItem>
                    <TabItem value="ja_KS" label="関西弁">
                        <img src="/img/langs/ja_KS.png" alt="関西弁" width={"100%"}/>
                    </TabItem>
                </Tabs>
            </Appeal>
            <Appeal right>
                <div>
                    <h2 id={"topics-ci-cd-integration"}>CI-CD との統合</h2>
                    <p>
                        CI-CD との統合により、プラグインの品質を継続的に監視できます。
                        例えば、 GitHub Actions と組み合わせることで、プッシュされたときや
                        プルリクエストが更新されたときに、自動的にテストを実行させられます。
                        マージ前に自動でテストを実行して、リグレッションを防ぎましょう。
                    </p>
                    <p>
                        また GitHub Actions と統合した場合は、さらに便利な機能が使えます。
                        例えばテストの結果は表とグラフ、およびガントチャートで表示されます。
                        さらに、プルリクエストで実行した場合は、自動で結果のコメントが追加されます。
                        コメントは最新のコミット用に自動で更新されるため、スパムになる心配はありません。
                    </p>
                    <p>チーム全員とテスト結果を共有して、よりよい開発体験を目指します。</p>
                </div>
                <Tabs className={clsx(styles.imageTabs, styles.ghTab)}>
                    <TabItem value="summary" label="概要">
                        <img src="/img/actions/summary.png" alt="サマリ" width={"100%"}/>
                    </TabItem>
                    <TabItem value="pie" label="パイチャート">
                        <img src="/img/actions/pie.png" alt="パイチャート" width={"100%"}/>
                    </TabItem>
                    <TabItem value="antt" label="ガントチャート">
                        <img src="/img/actions/gantt.png" alt="ガントチャート" width={"100%"}/>
                    </TabItem>
                    <TabItem value="pr-done" label="プルリクエスト">
                        <img src="/img/actions/pr-done.png" alt="プルリクエスト" width={"100%"}/>
                    </TabItem>
                </Tabs>
            </Appeal>
            <Appeal>
                <div className={styles.appealDesc}>
                    <h2 id={"topics-build-test-environment"}>テスト環境をかんたん構築</h2>
                    <p>
                        Scenamatica を利用すると、テストに必要なプレイヤ（アクタ）とワールド（ステージ）、
                        エンティティを自動で準備できます。
                        シナリオの実行ごとに初期化して生成されるため、前の状態を引き継いでしまう心配はありません。
                    </p>
                    <p>
                        アクタは、外部からの接続なしに、サーバに本物のプレイヤとして認識されます。
                        これにより、プラグインは通常のプレイヤを処理するのと同じように、仮想的なプレイヤを処理できます。
                    </p>
                    <p>
                        ステージは、既存のワールドをコピーするモードと、新規に生成するモードをサポートしています。
                        プラグインのテスト専用のワールドを用意することで、限定的な環境下でのテストを実現できます。
                        また、新規に生成するモードを活用すると、ランダムな条件でのテストを実現できます。
                    </p>
                </div>
                <Tabs className={clsx(styles.imageTabs, styles.left)}>
                    <TabItem value="actor" label="アクタ">
                        <img src="/img/contexts/actor.png" alt="アクタ" width={"100%"}/>
                    </TabItem>
                    <TabItem value="en_US" label="ステージ">
                        <img src="/img/contexts/stage.png" alt="ステージ" width={"100%"}/>
                    </TabItem>
                </Tabs>
            </Appeal>
        </>
    );
}

export default function Home(): JSX.Element {
    const {siteConfig} = useDocusaurusContext()

    return (
        <Layout title={siteConfig.title}
                description="Scenamatica - 世界初の PaperMC プラグインのシナリオテスト自動化ツール">
            <header className={clsx("hero hero--primary", styles.heroBanner)}>
                <Characters/>
                <div className="container">
                    <img width={150} height={150} src="/img/logo.png" alt="Scenamatica" className={styles.heroLogo}/>
                    <h1 className={styles.title}>{siteConfig.title}</h1>
                    <Tagline/>
                    <div>
                        <p className={styles.description}>
                            Let's enhance your PaperMC plugin quality with Scenamatica: YAML-defined
                            scenarios, CI/CD integration
                        </p>
                        <p className={styles.description}>
                            Scenamatica の YAML で定義されたテスト、CI/CD 統合で、PaperMCプラグインの品質を向上させましょう。
                        </p>
                    </div>
                    <Buttons/>

                    <div className={styles.scroll}>
                        <p><FontAwesomeIcon icon={faChevronDown}/></p>
                        <p><FontAwesomeIcon icon={faChevronDown}/></p>
                        <p><FontAwesomeIcon icon={faChevronDown}/></p>
                    </div>
                </div>
            </header>
            <main>
                <div className={styles.topics}>
                    <div className={styles.topic}>
                        <div className={styles.overview}>
                            <h2>概要</h2>
                            <p>
                                Scenamaticaは、PaperMC プラグインに特化したエンドツーエンドのテスト自動化フレームワークです。
                                YAML で予め定義されたマルチシナリオのテストにより、プラグイン機能の正常性と異常性を自動的に検証します。
                                Scenamaticaの活用により、リグレッションテストをスピーディに実施でき、PaperMCプラグインの開発にエンジニアが集中できるようになります。
                                このプロセスにより、プラグインの品質が向上し、ユーザーと開発者、両者にとってより優れたエクスペリエンスが提供されます。
                                また、既存の CI/CD パイプラインと統合することで、プラグインの品質を継続的に監視できます。
                            </p>
                        </div>
                    </div>
                    <div className={styles.topic}>
                        <Features/>
                    </div>
                    <Appeals/>
                </div>
            </main>
            <footer className={styles.landingFooter}>
                <h2>ぜひ Scenamatica を使ってみてください！</h2>
                <Buttons/>
            </footer>
        </Layout>
    )
}
