package com.lee.one;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
public class WebSocketTest {

  // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
  private static int onlineCount = 0;

  // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
  private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

  private static Map<String, Session> map = new HashMap<String, Session>();

  private static Map<String, Session[]> room = new HashMap<String, Session[]>();

  private static Set<String> flags = new HashSet<String>();
  // 与某个客户端的连接会话，需要通过它来给客户端发送数据
  private Session session;

  MakePipe mp;
  Thread t;

  public WebSocketTest() {
    super();
    System.out.println("play够着函数");
  }

  /**
   * 连接建立成功调用的方法
   * 
   * @param session
   *          可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
   */
  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    sessions.add(this.session); // 加入set中
    addOnlineCount(); // 在线数加1
    String flag = "";
    int num = 0;

    if (haveNoRoom()) {
      flag = makeRoom(session);
      num = 1;
    } else {
      for (String string : flags) {
        Session[] ss = room.get(string);
        if (ss[1] == null) {
          ss[1] = session;
          flag = string;
          num = 2;
          break;
        }
      }
    }
    sendMessageToSession(session, "start:" + flag + ":" + num);
    System.out
        .println("play有新连接加入！当前在线人数为" + getOnlineCount());
  }

  private boolean haveNoRoom() {
    if (flags.size() == 0) {
      return true;
    }
    for (String string : flags) {
      Session[] ss = room.get(string);
      if (ss[1] == null) {
        return false;
      }
    }
    return true;
  }

  public String makeRoom(Session session) {
    String flag =session.getId();
    Session[] sessions = new Session[2];
    sessions[0] = session;
    room.put(flag, sessions);
    flags.add(flag);
    return session.getId();
  }

  public String createFlag() {
    String chars = "abcdefghijklmnopqrstuvwxyz";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 5; i++) {
      sb.append(chars.charAt((int) (Math.random() * 26)));
    }
    return sb.toString();
  }

  /**
   * 连接关闭调用的方法
   */
  @OnClose
  public void onClose() {
	room.remove(this.session.getId()); //当链接断开的时候移除房间
	flags.remove(this.session.getId()); //移除钥匙
    subOnlineCount(); // 在线数减1
    System.out.println("play有一连接关闭！当前在线人数为" + getOnlineCount());
  }

  /**
   * 收到客户端消息后调用的方法
   * 
   * @param message
   *          客户端发送过来的消息
   * @param session
   *          可选的参数
   */
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    System.out
        .println("play来自客户端的消息:" + message + ":session:" + session.getId()+"||房间数:"+room.size());
    String[] ss = message.split(":");
    if (ss[0].equals("turn")) {
      leftRight(ss);
    }
  }

  private void leftRight(String[] ss) throws IOException {
    StringBuffer rep = new StringBuffer();
    rep.append("turn:");
    int positionx = Integer.parseInt(ss[4]);
    if (ss[2].equals("1")) {
      if (ss[3].equals("left")) {
        rep.append("plane1:" + (positionx-5));
      } else {
    	  rep.append("plane1:" + (positionx+5));
      }
    } else if (ss[2].equals("2")) {
      if (ss[3].equals("left")) {
    	  rep.append("plane2:" + (positionx-5));
      } else {
    	  rep.append("plane2:" + (positionx+5));
      }
    }
    System.out.println("發出消息:"+rep.toString());
    sendMessage(room.get(ss[1]), rep.toString());
  }

  /**
   * 发生错误时调用
   * 
   * @param session
   * @param error
   */
  @OnError
  public void onError(Session session, Throwable error) {
    System.out.println("play发生错误" + error);
    //error.printStackTrace();
  }

  /**
   * 发送
   * 
   * @param message
   * @throws IOException
   */
  public void sendMessage(Session[] sessions, String message)
      throws IOException {
    for (Session session : sessions) {
    	if(session!=null){
    		session.getBasicRemote().sendText(message);
    	}
    }
  }

  // 给单个用户发消息
  public void sendMessageToSession(Session session, String text) {
    try {
      session.getBasicRemote().sendText(text);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static synchronized int getOnlineCount() {
    return onlineCount;
  }

  public static synchronized void addOnlineCount() {
    WebSocketTest.onlineCount++;
  }

  public static synchronized void subOnlineCount() {
    WebSocketTest.onlineCount--;
  }

}
