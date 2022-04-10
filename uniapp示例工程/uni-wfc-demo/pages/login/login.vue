<template>
<view class="page-body">
  <view class="page-section">
    <view class="weui-cells__title">请输入手机号</view>
    <view class="weui-cells weui-cells_after-title">
      <view class="weui-cell weui-cell_input">
        <input class="weui-input" @input="bindPhoneInput" type="number" placeholder="手机号"></input>
      </view>
    </view>
  </view>

    <view class="page-section">
    <view class="weui-cells__title">请输入验证码</view>
    <view class="weui-cells weui-cells_after-title">
      <view class="weui-cell weui-cell_input">
        <input class="weui-input" @input="bindCodeInput" type="number" placeholder="验证码"></input>
      </view>
    </view>
  </view>

<button @tap="bindLoginTap">登录</button>
<button @tap="bindAuthCodeTap">获取验证码</button>

</view>
</template>

<script>
import wfc from "../../wfc/client/wfc";
import Config from '../../config';
import {setItem} from "../util/storageHelper";

export default {
  data() {
    return {
      focus: false,
      phone: '',
      code: ''
    };
  },

  components: {},
  props: {},
    onShow(){
      let userId = 'EPhwEwgg';
      let tokne = 'tOi3KkHT+mFNRN1GacssK29YgcK/oCl/Os0IE53dZkNAeIdrevILfL0WIytwJbYAVdi1CNoyU1yAxqhKzxTboWivB+R1do3J2Os/QCUpETc2H1Eo9TZgrt1UUyYR2FWc9GVQV9StCpjvv0AunLc+2OmwJWfoel/2Vg56aKJjEYc='
      console.log('jyj', 'saved token', userId, tokne)
        if (tokne){
            wfc.connect(userId, tokne);
            uni.switchTab({
                url: '../conversationList/ConversationListView',
                success:()=>{
                    console.log('to conversation list success');
                },
                fail: e => {
                    console.log('to conversation list error', e);
                },
                complete: () => {
                    console.log('switch tab complete')
                }
            });
        }

    },
  methods: {
    bindPhoneInput: function (e) {
      // this.setData({
      //   phone: e.detail.value
      // });
	  this.phone = e.detail.value;
    },
    bindCodeInput: function (e) {
      // this.setData({
      //   code: e.detail.value
      // });
	  this.code  = e.detail.value;
    },
    bindLoginTap: function (e) {
      console.log(this.phone); // console.log(this.data.code)

      this.login(this.phone, this.code);
    },

    login(phone, code) {
      let appServer = Config.APP_SERVER + '/login';
      let clientId = wfc.getClientId();
      uni.request({
        url: appServer,
        data: {
          mobile: phone,
          code: code,
          clientId: clientId,
		  platform:2,
        },
        header: {
          'content-type': 'application/json' // 默认值

        },
        method: 'POST',

        success(res) {
          console.log(res.data);

          if (res.statusCode === 200) {
            let loginResult = res.data;

            if (loginResult.code === 0) {
              let userId = loginResult.result.userId;
              let token = loginResult.result.token;
              wfc.connect(userId, token);

              console.log('jyj uuu',userId, token, wfc.getClientId())
              uni.switchTab({
                url: '../conversationList/ConversationListView',
				success:()=>{
					console.log('to conversation list success');
				},
                fail: e => {
					console.log('to conversation list error', e);
                },
				complete: () => {
					console.log('switch tab complete')
				}
              });
            } else {
              console.log('login failed', loginResult);
            }
          }
        }

      });
    },

    bindAuthCodeTap: function (e) {
      console.log(this.phone);
      this.authCode(this.phone);
    },

    authCode(phone) {
      let appServer = Config.APP_SERVER + '/send_code';
      uni.request({
        url: appServer,
        data: {
          mobile: phone
        },
        header: {
          'content-type': 'application/json' // 默认值

        },
        method: 'POST',

        success(res) {
          console.log(res.data);

          if (res.statusCode === 200) {
            console.log('发送验证码成功');
          }
        }

      });
    }

  }
};
</script>
<style>
@import "./login.css";
</style>
