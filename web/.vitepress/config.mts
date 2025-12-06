import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Firmament",
  description: "Reaching for the sky on HyPixel SkyBlock",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'For Developers', link: '/developers' }
    ],

    sidebar: [
      {
        text: 'Developers',
        items: [
          { text: 'Resource Packs', link: '/developers/texture-packs' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/FirmamentMC/Firmament' },
      { icon: 'modrinth', link: 'https://modrinth.com/mod/firmament' },
    ]
  }
})
