import React from "react"
import clsx from "clsx"
import Link from "@docusaurus/Link"
import useDocusaurusContext from "@docusaurus/useDocusaurusContext"
import Layout from "@theme/Layout"

import styles from "./index.module.css"

type Feature = {
    title: string
    description: JSX.Element | string
}

const features: Feature[] = [
    {
        title: "PaperMC プラグインの世界初のシナリオテスト",
        description: "世界初の PaperMC プラグインに特化したシナリオテストを実際のサーバで実行します。" +
            "開発のフローに取り込むことで、リリース前にリグレッションを防ぎます。"
    },
    {
        title: "YAML で定義されたシナリオ",
        description: "高い可読性のシナリオで、シナリオの追加・変更を容易にします。" +
            "面倒なテストコードを書く必要はなく、プログラマでなくてもテストを追加できます。"
    },
    {
        title: "CI/CD との統合",
        description: "GitHub Actions などの CI/CD に統合することで、プラグインの品質を継続的に監視します。" +
            "プルリクエストをマージする前に、テストに成功することを要求できます。"
    }
]

const Feature = ({ title, description }: Feature): JSX.Element => (
    <div className={clsx("col", styles.feature)}>
        <h3>{title}</h3>
        <p className={styles.description}>{description}</p>
    </div>
)

export default function Home(): JSX.Element {
    const { siteConfig } = useDocusaurusContext()
    return (
        <Layout title={siteConfig.title} description="Scenamatica - 世界初の PaperMC プラグインのシナリオテスト自動化ツール">
            <header className={clsx("hero hero--primary", styles.heroBanner)}>
                <div className="container">
                    <h1 className="hero__title">{siteConfig.title}</h1>
                    <p className={styles.subtitle}>YAML定義のシナリオとCI/CD統合による自動テストフレームワークで、PaperMCプラグインの品質を格段に向上させましょう。</p>
                    <p className={styles.subtitle}>{siteConfig.tagline}</p>
                    <div className={styles.buttons}>
                        <Link className="button button--secondary button--lg" to="/docs/home">
                            使い始める &gt;
                        </Link>
                    </div>
                </div>
            </header>
            <main>
                <div className={styles.container}>
                    <h2 className={styles.topic}>特徴</h2>
                    <div className={styles.features}>
                        <div className="row">
                            {features.map((props, idx) => (
                                <Feature key={idx} {...props} />
                            ))}
                        </div>
                    </div>
                </div>
            </main>
        </Layout>
    )
}
