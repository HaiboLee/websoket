package com.lee.one;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/join")
public class WebSocketJoin {

	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;

	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	
	//存放房间号码
	private static ArrayList<String> rooms = new ArrayList<String>();
	
	private static StringBuffer allRoom=new StringBuffer();
	
	public WebSocketJoin() {
		super();
	}

	/**
	 * 连接建立成功调用的方法
	 * 
	 * @param session
	 *            可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		sessions.add(this.session); // 加入set中
		addOnlineCount(); // 在线数加1
		System.out.println("menu有新连接加入！当前在线人数为" + getOnlineCount());
		try {
			this.session.getBasicRemote().sendText(allRoom.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		sessions.remove(this.session); // 从set中删除
		subOnlineCount(); // 在线数减1
		System.out.println("menu有一连接关闭！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message
	 *            客户端发送过来的消息
	 * @param session
	 *            可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("menu来自客户端的消息:" + message+":session:"+session.getId());
		rooms.add(message);
		allRoom();
		// 群发消息
				try {
					sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("menu发生错误" + error);
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage() throws IOException {
		for (Session s : sessions) {
			if(s!=this.session){
				s.getBasicRemote().sendText(allRoom.toString());
			}
		}
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		WebSocketJoin.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		WebSocketJoin.onlineCount--;
	}
	
	public static synchronized void allRoom(){
			if(allRoom.length()==0){
				allRoom.append(rooms.get(rooms.size()-1));
			}else{
				allRoom.append(":").append(rooms.get(rooms.size()-1));
			}
	}

}
