import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import 'element-plus/dist/index.css';

import App from './App.vue';
import router from './router';
import { useAuthStore } from './stores/auth';
import './styles/index.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ElementPlus);

Object.entries(ElementPlusIconsVue).forEach(([key, component]) => {
  app.component(key, component);
});

app.mount('#app');

useAuthStore().bootstrap();
