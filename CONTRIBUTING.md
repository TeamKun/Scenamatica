# Scenamatica への貢献

まず初めに、Scenamatica への貢献に感謝します。Scenamatica は、 KUN Lab メンバと OSS コミュニティの協力によって開発されています。  
貴重な時間を割いていただき、ありがとうございます。

あらゆる種類の貢献が奨励され、歓迎されています。様々な貢献方法とメンテナの対応については、以下のガイドラインを参照してください。  
また、貢献前には必ず関連するセクションをお読みください。これにより、あなたの貢献がよりスムーズに行なえます。

> もしプロジェクトが気に入っており、でも時間がない場合でも大丈夫です！ 以下のような方法でも十分に貢献できます。
> - [プロジェクトにスターをつける](https://github.com/teamkun/scenamatica)
> - プロジェクトについてツイートする
> - プロジェクトを友人や同僚、他のコミュニティのメンバに紹介する
> - [Github Actions で使用する](https://github.com/TeamKun/scenamatica-action)
> - [Docker Hub で使用する](https://hub.docker.com/r/teamkun/scenamatica)

## 行動規範

このプロジェクトに参加するすべての人は、 [Scenamatica 行動規範](CODE_OF_CONDUCT.md)に遵守する必要があります。  
容認できない行為は、プロジェクトのメンテナによって削除されたり、参加を拒否されることがあります。

## 質問がありますか？

> 質問をする場合は、 [Scenamatica ドキュメント](https://scenamatica.kunlab.org/)を参照したことを前提としています。

質問をする前に、[既知の問題](https://github.com/TeamKun/Scenamatica/issues)を探すことをおすすめします。適切な問題を見つけ、コメントを残せます。  
また、最初にインターネットで検索することもおすすめします。

それでも解決しない場合は、[こちら](https://github.com/TeamKun/Scenamatica/issues/new?template=question.yml)をクリックし、問題の作成を開始します。
ウィザードに従って、質問の内容を記入してください。

また、あなたの行った質問はドキュメントに追加されたり、よくある質問に追加されることがあります。

## バグを見つけたり、機能の提案がありますか？

これらをリクエストする前に、[既知の問題](https://github.com/TeamKun/Scenamatica/issues)を探すことをおすすめします。  
既存の機能の提案にコメントしたり、バグの多重報告を防いだりします。

それでも解決しない場合は、以下のリンクをクリックし、問題の作成を開始します。その後のウィザードに従って、問題の内容を記入してください。

+ [バグを報告する](https://github.com/TeamKun/Scenamatica/issues/new?template=bug_report.yml)
+ [機能の提案](https://github.com/TeamKun/Scenamatica/issues/new?template=feature_request.yml)

## プロジェクトにコードを貢献する

コードでの貢献は常に歓迎されます。 以下の点に注意して、一般的に貢献する方法で貢献してください。

+ 上流ブランチは `main` です。  
  プルリクエストは、 `main` ブランチに対して作成してください。
+ 既存のコードスタイルに従ってください。    
  Scenamatica では主なスタイルとして、 BSD(Allman) スタイルを採用しています。
  また、コードのインデントには４つのスペースを使用してください。  
  丸かっこの中の始まりと終わりの前にはスペースを**入れないで**ください。  
  `if` や `while` などの制御構文の後には、スペースを**入れて**ください。
+ **プルリクエストのタイトル**及び**コミットメッセージ**は [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) に従ってください。  
  これにより、コミット/PR が行う変更が明確になり、スコープの調整が容易になります。

## プロジェクトの構成

このプロジェクトは、以下の１０つのモジュールから構成されています。

+ [ActionEngine](ScenamaticaActionEngine)  
  [アクション](https://scenamatica.kunlab.org/docs/use/scenario/actions/)を実行するためのエンジンです。
+ [ScenamaticaAPI](ScenamaticaAPI)  
  Scenamatica の API です。
+ [ScenamaticaCommons](ScenamaticaCommons)  
  Scenamatica 内部で使用される共通のユーティリティなどです。
+ [ScenamaticaContextEngine](ScenamaticaContextEngine)  
  [コンテキスト](https://scenamatica.kunlab.org/docs/use/scenario/elements#context)を生成管理するためのエンジンです。
+ [ScenamaticaDaemon](ScenamaticaDaemon)  
  Scenamatica のバックエンドのメインです。https://scenamatica.kunlab.org/docs/use/scenario/file-syntax
+ [ScenamaticaModels](ScenamaticaModels)  
  Scenamatica の各サービスのインタフェースや列挙型、例外などのモデルです。
+ [ScenamaticaPlugin](ScenamaticaPlugin)  
  Scenamatica の PaperMC プラグイン自体（フロントエンド）です。
+ [ScenamaticaScenarioEngine](ScenamaticaScenarioEngine)  
  [シナリオ](https://scenamatica.kunlab.org/docs/use/scenario/)を実行するためのエンジンです。
+ [ScenamaticaScenarioFile](ScenamaticaScenarioFile)  
  [シナリオファイル](https://scenamatica.kunlab.org/docs/use/scenario/file-syntax)を読み込むためのエンジンです。
+ [ScenamaticaTriggerEngine](ScenamaticaTriggerEngine)  
  [トリガ](https://scenamatica.kunlab.org/docs/use/scenario/elements#trigger)を実行するためのエンジンです。
