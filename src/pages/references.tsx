import Heading from "@theme/Heading";
import Link from "@docusaurus/Link";
import Layout from "@theme/Layout";

export default function References(): JSX.Element {
    return (
        <Layout title="Scenamatica リファレンス"
                description="Scenamatica - 世界初の PaperMC プラグインのシナリオテスト自動化ツール">
        <main className={'container margin-vert--xl'}>
            <div className="row">
                <div className="col col--6 col--offset-3">
                    <Heading as="h1" className="hero__title">Scenamatica リファレンス</Heading>
                    <hr />
                    <p>Scenamatica リファレンスへようこそ！</p>
                    <p>エディタ上で該当する要素の定義を参照するか、以下のリンクを使用して目的のページに移動できます。</p>
                </div>
                <div className="col col--9 col--offset-3">
                    <Heading as="h2" className="hero__subtitle">リファレンス集</Heading>
                    <ul>
                        <li><Link to="/docs/home">ドキュメントホーム</Link></li>
                        <li><Link to="/docs/getting-started/tutorials">各種チュートリアル</Link></li>
                        <li><Link to="/docs/use/scenario/types#scenario-file">シナリオファイルの構造</Link>
                        </li>
                        <br/>
                        <li><Link to="/docs/use/scenario/types">型一覧</Link></li>
                        <li><Link to="/docs/use/scenario/actions">アクション一覧</Link></li>
                        <li><Link to="/docs/use/scenario/trigger">トリガ一覧</Link></li>
                    </ul>
                </div>
            </div>
        </main>
        </Layout>
    )
}
