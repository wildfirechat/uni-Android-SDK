# uni-Android-SDK

## 重要说明

本项目正在活跃开发中，最终目标：

0. 提供两个插件```uni-client-module```和```uni-uikit-module```，分别是 IM 能力层插件和 UI 层插件
1. ```uni-app```层提供对```uni-client-module``` 插件的封装，接口和```web/pc/wx```一致
2. ```uni-app```层提供对```uni-uikit-module``` 插件的封装，支持```uni-app```直接打开原生界面
3. 支持推送

## 快速开始

1. 跟随[野火文档](https://docs.wildfirechat.net/)在本地或在服务器上构建并配置好服务端
2. 运行```app module```！

## 更改示例项目并测试

用 hbuilderx 打开 `uniapp示例工程/uni-wfc-demo`

- 更改示例后在 hbuilderx -> 发行 -> 本地打包 -> 生成本地打包资源，并将其放入`app/src/main/assets/apps`中，然后在 android studio 中运行
- 或者修改原生代码后[打自定义基座](https://ask.dcloud.net.cn/article/35482)并在 hbuilderx 中运行

具体选择哪种取决于当前工作重心，封装原生新功能使用本地打包，只需调试示例打自定义基座

## 感谢

本项目参考了[wildfire-uniplugin-demo](https://github.com/PentaTea/wildfire-uniplugin-demo)
