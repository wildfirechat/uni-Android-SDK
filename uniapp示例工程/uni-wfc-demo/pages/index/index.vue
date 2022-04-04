<template>
  <pre v-if="err" v-html="obj2html(err)"></pre>
  <div v-else style="display: flex">
	<button @click="connect">连接</button>
	<button >getClientId</button>
  </div>
</template>

<script>
import hanabi from "common/hanabi";
import stringifyObject from "common/stringify-object";

var wfcClient = uni.requireNativePlugin("uni-wfc-client");

var ConnectionStatus = {
  "-6": " SecretKey 不匹配",
  "-5": " 令牌不正确",
  "-4": " 服务器关闭",
  "-3": " 连接被拒绝",
  "-2": " 退出登录",
  "-1": " 未连接",
  0: "连接中",
  1: "连接成功",
  2: "连接状态接收中",
};

export default {
  data() {
    return {
      height: uni.getSystemInfoSync().windowHeight + "px",
      wildfire: null,
      log: [],
      err: "",

      userId: "",
      token: "",
    };
  },
  mounted() {
    //#ifndef APP-PLUS
    this.err = "请​在​APP​端​测​试​哦";
    //#endif

    //#ifdef APP-PLUS
    plus.globalEvent.addEventListener("wildfire", (e) => {
      console.log(e);
      this.log = [this.interpreter(e), ...this.log];
    });
    this.log = [wfcClient.init(), ...this.log];
    //#endif
	let clientId = wfcClient.getClientId();
	console.log('getclientId', clientId);
	
	wfcClient.setOnConnectStatusListener((status)=>{
		console.log('connectStatus', status);
	});
	
  },
  methods: {
    connect: () => {
		let userId = 'uiuJuJcc';
		let token = 'P/xDVWeStEUmzKHlqRF+KJ7H59w70t8NgJvCGh7pRBwQ4YwdsEIW30136FL7MzRFN1RvXq5kKwckLohqbFqHQxFpMIRmYEfxwo0cDg4HBFqx2HXEWMOtn7NdWuJZCOP0UGn9U5DLg+H9r7MLqUC8+KVkf+pk42UALcwUpkOBYwA=';
		wfcClient.connect('wildfirechat.net',userId, token );
    },
    obj2html: (data) => {
      return hanabi(
        stringifyObject(data, {
          indent: "  ",
        })
      );
    },
    interpreter(e) {
      //做一些操作,拦截事件等等
      //开发时建议使用ts写清楚type,使用中间件模式做拦截
      e.tags = [];
      if (e.data[0] === "onConnectionStatusChange")
        e.tags.push(ConnectionStatus[e.data[1]]);
      return e;
    },
    time(timestamp) {
      var date = new Date(timestamp);
      let prefix = function (num, len = 2) {
        return (
          Array(Math.abs(("" + num).length - ((len || 2) + 1))).join(0) + num
        );
      };
      return (
        prefix(date.getHours()) +
        ":" +
        prefix(date.getMinutes()) +
        ":" +
        prefix(date.getSeconds())
      );
    },
  },
};
</script>

<style lang="scss">
.operation {
  padding: 20rpx;
  box-sizing: border-box;
  width: 30%;
  position: fixed;
  right: 0;
  opacity: 0.5;
}

.log {
  user-select: text;
  padding: 20rpx;
  padding-top: 250rpx;
  box-sizing: border-box;
  font-size: 10px;
  line-height: 1.5;

  .container:nth-child(even) {
    background: rgb(246, 248, 250);
  }

  //翻转180度使最新项保持在滚动底部
  transform: rotate(180deg);

  .item {
    transform: rotate(180deg);
    animation: push 1s linear both,
      insert 0.3s 0.1s cubic-bezier(0.18, 0.89, 0.32, 1) both;

    margin-bottom: 10px;

    .title {
      font-size: 14px;
      color: rgb(65, 80, 95);
      padding: 6px;
      padding-left: 12px;
    }

    .tag {
      padding: 4px 8px;
      margin: 5px;
      border-radius: 4px;
      color: #476582;

      font-size: 9px;
      background-color: rgba(27, 31, 35, 0.05);
    }
  }

  @keyframes push {
    0% {
      max-height: 0%;
    }

    100% {
      max-height: 4000px;
    }
  }

  @keyframes insert {
    0% {
      opacity: 0;

      transform: translateX(100%) rotate(180deg);
    }

    100% {
      opacity: 1;
      transform: translateX(0) rotate(180deg);
    }
  }
}

input,
textarea {
  background: #f0f0f0;
  margin-bottom: 5px;
  padding: 5px;
}

button {
  font-size: 14px;
}
</style>
