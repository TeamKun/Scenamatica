import {themes} from "prism-react-renderer";
import type {Config, ThemeConfig} from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const docCommons = {
  sidebarPath: require.resolve("./sidebars.js"),
  editUrl: "https://github.com/TeamKUN/Scenamatica/edit/develop/docs/",
  showLastUpdateAuthor: true,
  showLastUpdateTime: true,
  lastVersion: "current",
  versions: {
    current: {
      label: "Scenamatica v1",
    },
  },
}

// noinspection JSUnusedGlobalSymbols
export default {
  title: "Scenamatica",
  staticDirectories: ["static"],
  tagline: "Let's enhance your PaperMC plugin quality with Scenamatica: YAML-defined scenarios, CI/CD integration.",
  favicon: "img/favicon.ico",
  url: "https://scenamatica.kunlab.org/",
  baseUrl: "/",

  clientModules: [
    require.resolve("@fontsource/jetbrains-mono/index.css"),
    require.resolve("@fontsource/murecho/index.css"),
  ],

  organizationName: "TeamKUN",
  projectName: "Scenamatica",

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  i18n: {
    defaultLocale: "ja",
    locales: ["ja"],
  },

  plugins: [
    require.resolve("docusaurus-plugin-image-zoom"),
    [
      "@docusaurus/plugin-pwa",
      {
        debug: false,
        pwaHead: [
          {
            tagName: "link",
            rel: "icon",
            href: "/img/favicon.ico",
          },
          {
            tagName: "link",
            rel: "manifest",
            href: "/manifest.json",
          },
          {
            tagName: "meta",
            name: "theme-color",
            content: "#53BBFFFF",
          },
          {
            tagName: "meta",
            name: "apple-mobile-web-app-capable",
            content: "yes",
          },
          {
            tagName: "meta",
            name: "apple-mobile-web-app-status-bar-style",
            content: "black",
          },
          {
            tagName: "link",
            rel: "apple-touch-icon",
            href: "/img/logo2.png",
          },
          {
            tagName: "link",
            rel: "mask-icon",
            href: "/img/logo2.png",
            color: "#53BBFFFF",
          },
          {
            tagName: "meta",
            name: "msapplication-TileImage",
            content: "/img/logo2.png",
          },
          {
            tagName: "meta",
            name: "msapplication-TileColor",
            content: "#53BBFFFF",
          },
        ],
      }
    ],
    [
      "@docusaurus/plugin-content-docs",
      {
        id: "references",
        path: "references",
        routeBasePath: "references",
        ...docCommons,
      }
    ]
  ],

  presets: [
    [
      "classic",
      {
        docs: {
          ...docCommons,
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    announcementBar: {
      id: "announcementBar-1",
      content: `✨ もし Scenamatica が気に入ったなら,  GitHub で <a href="https://github.com/TeamKUN/Scenamatica">Star</a> をしてください！`,
    },
    navbar: {
      title: "Scenamatica",
      logo: {
        alt: "Scenamatica Logo",
        src: "img/logo2.png",
      },
      items: [
        {
          to: "/docs",
          position: "left",
          label: "ホーム",
        },
        {
          to: "/docs/getting-started",
          position: "left",
          label: "使い始める",
        },
        {
          to: "/docs/use/scenario",
          position: "left",
          label: "シナリオ",
        },
        {
          label: "コマンド",
          position: "left",
          to: "/docs/use/commands",
        },
        {
          label: "リファレンス",
          position: "left",
          to: "/references",
        },
        {
          type: "docsVersionDropdown",
          position: "right",
          dropdownActiveClassDisabled: true,
        },
        {
          href: "https://github.com/TeamKUN/Scenamatica/releases",
          position: "right",
          label: "ダウンロード",
        },
        {
          href: "https://github.com/TeamKUN/Scenamatica",
          position: "right",
          className: "icon-link i-github",
        },
      ],
    },
    footer: {
      style: "dark",
      links: [
        {
          title: "ドキュメント - Documents",
          items: [
            {
              label: "ホーム",
              to: "/docs",
            },
            {
              label: "使い始める",
              to: "/docs/getting-started",
            },
            {
              label: "チュートリアル",
              to: "/docs/getting-started/tutorials",
            },
            {
              label: "コマンド",
              to: "/docs/use/commands",
            },
            {
              label: "シナリオ",
              to: "/docs/use/scenario",
            },
          ],
        },
        {
          title: "リファレンス - References",
          items: [
            {
              label: "アクション - Actions",
              to: "/references/actions",
            },
            {
              label: "型 - Types",
              to: "/references/types",
            }
          ]
        },
        {
          title: "開発者 - Peyang",
          items: [
            {
              label: "GitHub",
              href: "https://github.com/PeyaPeyaPeyang",
            },
            {
              label: "Twitter",
              href: "https://twitter.com/peyang9799",
            },
            {
              label: "Email",
              href: "mailto:peyang@peya.tokyo",
            },
          ],
        },
        {
          title: "Scenamatica",
          items: [
            {
              label: "GitHub",
              href: "https://github.com/TeamKUN/Scenamatica",
            },
            {
              label: "バグを報告",
              href: `https://github.com/TeamKUN/Scenamatica/issues/new`,
            },
            {
              label: "機能をリクエスト",
              href: `https://github.com/TeamKUN/Scenamatica/issues/new`,
            },
          ],
        },
      ],
      copyright: `
          Copyright &copy; ${new Date().getFullYear()} Scenamatica HQ
          in <a href="https://github.com/TeamKUN">KUN Development Team</a>.,
          <a href="https://peya.tokyo/">Peyang</a>
          <br /><br />
          Minecraft is a registered trademark of Mojang AB.<br />
          Scenamatica HQ and Scenamatica are not officially endorsed by Minecraft or Mojang AB,
          nor are they affiliated with Minecraft or Mojang AB.
        `,
    },
    prism: {
      theme: themes.github,
      darkTheme: themes.vsDark,
      additionalLanguages: ["java", "yaml", "ebnf"],
    },
    zoom: {
      background: {
        light: "rgba(0, 0, 0, 0.6)",
        dark: "rgba(0, 0, 0, 0.6)",
      },
    },
    colorMode: {
      defaultMode: "dark",
      respectPrefersColorScheme: false,
    },
    docs: {
      sidebar: {
        autoCollapseCategories: true,
        hideable: true,
      },
    },
    algolia: {
      appId: "QWWI0CDV6V",
      apiKey: "cc1542c44da9a2e9d96d7d681ea7ee04",
      indexName: "scenamatica-kunlab",
      contextualSearch: true,
      searchPagePath: "search",
    },
    metadata: [
      {
        name: "twitter:card",
        content: "summary_large_image"
      },
      {
        name: "keywords",
        content: "Scenamatica, Minecraft, PaperMC, Plugin, Scenario, CI/CD, Automatic Testing, Spigot, Bukkit, Plugin, Plugin Development"
      }
    ],
    headTags: [
      {
        tagName: "meta",
        name: "twitter:title",
        content: "Scenamatica"
      },
      {
        tagName: "meta",
        name: "twitter:description",
        content: "Let's enhance your PaperMC plugin quality with Scenamatica: YAML-defined scenarios, CI/CD integration."
      },
      {
        tagName: "meta",
        name: "twitter:image",
        content: "https://scenamatica.kunlab.org/img/landing.png"
      },
      {
        tagName: "meta",
        name: "twitter:card",
        content: "summary_large_image"
      },
      {
        tagName: "meta",
        name: "og:title",
        content: "Scenamatica"
      },
      {
        tagName: "meta",
        name: "og:description",
        content: "Let's enhance your PaperMC plugin quality with Scenamatica: YAML-defined scenarios, CI/CD integration."
      },
      {
        tagName: "meta",
        name: "og:url",
        content: "https://scenamatica.kunlab.org/"
      },
      {
        tagName: "meta",
        name: "og:site_name",
        content: "Scenamatica"
      }
    ],
    image: "img/landing.png",
  } satisfies ThemeConfig,
} satisfies Config;
