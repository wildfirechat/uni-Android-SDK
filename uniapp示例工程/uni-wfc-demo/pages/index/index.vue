<template>
  <pre v-if="err" v-html="obj2html(err)"></pre>
  <div v-else style="display: flex">
	<button @click="connect">连接</button>
	<button @click="getClientId">getClientId</button>
	<button @click="sendMessage">发送消息</button>
	<button @click="createGroup"> 创建群组</button>
  </div>
</template>

<script>
import hanabi from "common/hanabi";
import stringifyObject from "common/stringify-object";
import wfc from '../../wfc/client/wfc.js'
import Conversation from '../../wfc/model/conversation.js';
import TextMessageContent from '../../wfc/messages/textMessageContent'

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

	console.log('mountedww')
    //#ifdef APP-PLUS

    //#endif
	// console.log('getclientId', clientId);


  },
  methods: {
    connect: () => {
		let userId = '8Smy8ypp';
		let token = "E13pRXWlp/lNl+9tKwIY5AH82iZIOqlLh5ZJsxZwL6e356dq48CEhTRzS4OkynoI5ad+GuA7E8SaiScP19w807L70UHTzVJxXJOjPzH6lwO9zJ+I3600C0gJjWBO4KOyEvhZQ9XwvrTQDJOytP8+hV8Hk5M4DuFnEPSYCAHRa9c="
        wfc.connect(userId, token);
    },

    getClientId(){
        let clientId = wfc.getClientId();
        console.log('getClientId', clientId);
    },

    sendMessage(){
        let conversation = new Conversation(0, 'FireRobot', 0)
        let textMessageContent = new TextMessageContent(' 你好，小火。')
        wfc.sendConversationMessage(conversation, textMessageContent, [], (messageId, timestamp) => {
            console.log('onPrepared', messageId, timestamp);
        }, (uploaded, total) => {
            console.log('onProgress', uploaded, total);
        }, (messageUid, timestamp) => {
            console.log('onSuccess', messageUid, timestamp);
        }, (err) => {
            console.log('onFail', err);
        });
    },

    createGroup(){
        wfc.createGroup(null, 1, 'uni-test-group', null, null, ['uiuJuJcc', 'FireRobot'], null, [0], null, (groupId) => {
            console.log('createGroup id', groupId);
        }, (err) => {
            console.log('create group error', err);

        });

    },

    obj2html: (data) => {
      return hanabi(
        stringifyObject(data, {
          indent: "  ",
        })
      );
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
