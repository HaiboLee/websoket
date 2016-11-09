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

	// ��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
	private static int onlineCount = 0;

	// concurrent�����̰߳�ȫSet���������ÿ���ͻ��˶�Ӧ��MyWebSocket������Ҫʵ�ַ�����뵥һ�ͻ���ͨ�ŵĻ�������ʹ��Map����ţ�����Key����Ϊ�û���ʶ
	private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

	// ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	private Session session;
	
	//��ŷ������
	private static ArrayList<String> rooms = new ArrayList<String>();
	
	private static StringBuffer allRoom=new StringBuffer();
	
	public WebSocketJoin() {
		super();
	}

	/**
	 * ���ӽ����ɹ����õķ���
	 * 
	 * @param session
	 *            ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	 */
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		sessions.add(this.session); // ����set��
		addOnlineCount(); // ��������1
		System.out.println("menu�������Ӽ��룡��ǰ��������Ϊ" + getOnlineCount());
		try {
			this.session.getBasicRemote().sendText(allRoom.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ���ӹرյ��õķ���
	 */
	@OnClose
	public void onClose() {
		sessions.remove(this.session); // ��set��ɾ��
		subOnlineCount(); // ��������1
		System.out.println("menu��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
	}

	/**
	 * �յ��ͻ�����Ϣ����õķ���
	 * 
	 * @param message
	 *            �ͻ��˷��͹�������Ϣ
	 * @param session
	 *            ��ѡ�Ĳ���
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("menu���Կͻ��˵���Ϣ:" + message+":session:"+session.getId());
		rooms.add(message);
		allRoom();
		// Ⱥ����Ϣ
				try {
					sendMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	/**
	 * ��������ʱ����
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("menu��������" + error);
		error.printStackTrace();
	}

	/**
	 * ������������漸��������һ����û����ע�⣬�Ǹ����Լ���Ҫ��ӵķ�����
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
