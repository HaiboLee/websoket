package com.lee.one;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

public class MakePipe extends TimerTask{
	
	private CopyOnWriteArraySet<Session> cop;

	public MakePipe(CopyOnWriteArraySet<Session> cop) {
		super();
		this.cop=cop;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int gap = (int) (85 + Math.random() * 168);
		for (Session s : cop) {
			try {
				s.getBasicRemote().sendText(gap + "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
