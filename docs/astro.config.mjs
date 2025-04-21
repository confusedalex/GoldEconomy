import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: "https://thegoldeconomy.confusedalex.dev",
  integrations: [
    starlight({
      title: 'TheGoldEconomy',
      description: 'TheGoldEconomy is a simple, easy to use, and powerful economy plugin for Minecraft servers.',
      editLink: {
        baseUrl: 'https://github.com/confusedalex/goldeconomy/edit/main/docs',
      },
      logo: {
        src: './src/assets/logo.svg',
        replacesTitle: true,
      },
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/confusedalex/goldeconomy' }],
      sidebar: [
        'installation', 'configuration', 'commands', 'permissions',
      ],
      lastUpdated: true,
    }),
  ],
});
