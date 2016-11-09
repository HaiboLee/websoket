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

  // ��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
  private static int onlineCount = 0;

  // concurrent�����̰߳�ȫSet���������ÿ���ͻ��˶�Ӧ��MyWebSocket������Ҫʵ�ַ�����뵥һ�ͻ���ͨ�ŵĻ�������ʹ��Map����ţ�����Key����Ϊ�û���ʶ
  private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

  private static Map<String, Session> map = new HashMap<String, Session>();

  private static Map<String, Session[]> room = new HashMap<String, Session[]>();

  private static Set<String> flags = new HashSet<String>();
  // ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
  private Session session;

  MakePipe mp;
  Thread t;

  public WebSocketTest() {
    super();
    System.out.println("play���ź���");
  }

  /**
   * ���ӽ����ɹ����õķ���
   * 
   * @param session
   *          ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
   */
  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    sessions.add(this.session); // ����set��
    addOnlineCount(); // ��������1
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
        .println("play�������Ӽ��룡��ǰ��������Ϊ" + getOnlineCount());
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
   * ���ӹرյ��õķ���
   */
  @OnClose
  public void onClose() {
	room.remove(this.session.getId()); //�����ӶϿ���ʱ���Ƴ�����
	flags.remove(this.session.getId()); //�Ƴ�Կ��
    subOnlineCount(); // ��������1
    System.out.println("play��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
  }

  /**
   * �յ��ͻ�����Ϣ����õķ���
   * 
   * @param message
   *          �ͻ��˷��͹�������Ϣ
   * @param session
   *          ��ѡ�Ĳ���
   */
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    System.out
        .println("play���Կͻ��˵���Ϣ:" + message + ":session:" + session.getId()+"||������:"+room.size());
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
    System.out.println("�l����Ϣ:"+rep.toString());
    sendMessage(room.get(ss[1]), rep.toString());
  }

  /**
   * ��������ʱ����
   * 
   * @param session
   * @param error
   */
  @OnError
  public void onError(Session session, Throwable error) {
    System.out.println("play��������" + error);
    //error.printStackTrace();
  }

  /**
   * ����
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

  // �������û�����Ϣ
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
