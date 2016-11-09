var websocket = null;
// 判断当前浏览器是否支持WebSocket
if ('WebSocket' in window) {
	websocket = new WebSocket('ws://localhost:8080/websocket/websocket');
} else {
	alert('当前浏览器 Not support websocket')
}

websocket.onopen = function() {
	console.log('连接成功！！');
}

websocket.onclose = function() {
	console.log("关闭连接");
}

function closeWebSocket() {
	websocket.close();
}

websocket.onmessage = function(event) {
	console.log(event.data);
}

function send() {
	var message = document.getElementById("text").value;
	websocket.send(message);
}

// 监听窗口关闭
window.onbeforeunload = function() {
	console.log("关闭窗口");
	closeWebSocket();
}
