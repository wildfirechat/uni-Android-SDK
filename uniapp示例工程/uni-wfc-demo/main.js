import Vue from 'vue'
import App from './App'
import store from "./store";
import VueContext from 'vue-context';

Vue.config.productionTip = false

App.mpType = 'app'

Vue.use(VueContext);
Vue.component("vue-context", VueContext)

const app = new Vue({
    ...App
})

app.store = store;
store.init();

app.$mount()
