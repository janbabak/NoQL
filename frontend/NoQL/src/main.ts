import './assets/main.css'

// Vue
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

const vuetify = createVuetify({
    components,
    directives,
    theme: {
        // defaultTheme: 'dark',
    },
})

// syntax highlighting
import { createVCodeBlock } from '@wdns/vue-code-block';

const VCodeBlock = createVCodeBlock();

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vuetify)
app.use(VCodeBlock);

app.mount('#app')
