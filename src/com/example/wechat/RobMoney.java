package com.example.wechat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class RobMoney extends AccessibilityService {

	private static final String TAG = "wechart";
	// ����һ���洢���hash
	String last = "��Ҫ�����";
	
	@Override
	protected void onServiceConnected() {
		Log.i(TAG, "config success!");
	}
	
	@SuppressLint("NewApi") 
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO �Զ����ɵķ������
		int eventType = event.getEventType();
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			System.out.println("notification: " + event.getText());
		}
		switch(eventType) {
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			System.out.println("notification: " + event.getText());
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				// ��ѭ�����ҳ����к���ֶε���Ϣ
				for (CharSequence text: texts) {
					String content = text.toString();
					//Log.i("demo","text:"+content);
					if (content.contains("[΢�ź��]")) {
						//Log.i("money","text:"+content);
						//System.out.println(event.getParcelableData());
						//Log.i("data",event.getParcelableData().toString());
						if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
							Log.i("money","text:"+content);
							Notification notification = (Notification) event.getParcelableData();
							PendingIntent pendingIntent = notification.contentIntent;
							try {
								pendingIntent.send();
							} catch (CanceledException e) {
								e.printStackTrace();
							}
						} else {
							System.out.println("error!!");
						}
					}
				}
			} else {
				
			}
		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
			System.out.println("windowchange: " + event.getText());	
			String className = event.getClassName().toString();
			Log.i("tencent",event.toString());
			Log.i("tencent",className);
			//Log.i("tencent",event.getPackageName().toString());
			if (event.getText().toString().contains("[΢�ź��]") && event.getPackageName().toString().equals("com.tencent.mm")) {
				Log.i("tencent","΢�ź��aa");
				getPacket();
			} else if (className.equals("com.tencent.mm.ui.LauncherUI")) {
				Log.i("tencent","��ʼ�����");// ����������Լ����ĺ���ǲ鿴���
				getPacket();			
			} else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f")) {
				Log.i("tencent","�򿪺�������ֵ�");
				openPacket();
			} else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")){
				Log.i("tencent","����ѽ��ˣ����ֵ�");
				leavePacket();
			} else {
				Log.i("tencent","ɶ��û�У����ֵ�");
			}
		break;
		}
	}

	
	// ������Ļ����,����ϵͳ�㲥
	
	// ���ܺ��,��Ҫ���˳������ĺ�����������ĺ���磺����24Сʱ���ڵĺ�����Ѿ������ĺ��
	@SuppressLint("NewApi")
	private void getPacket() {
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		// �����⵽��ȡ����󣬷��͵������
		if (rootNode == null) {
			Log.i("tencent", "error!!");
		} else {
			//recycle(rootNode);
			List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/appmsg_c2c_desc");
			Log.i("tencent","�ܹ��������Ϊ��"+list.size());
			if (list.isEmpty()) {
				Log.i("tencent","û�к����");
			} else if (last.equals(getHongbaoHash(list.get(list.size()-1)))) {
				// ֤���ǵ�һ�������
				last = getHongbaoHash(list.get(list.size()-1));
			} else {
				// hash����ͬ���������
				list.get(list.size()-1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
				last = getHongbaoHash(list.get(list.size()-1));
			}
		} 
				//list.get(list.size()-1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
	}
	
	@SuppressLint("NewApi")
	private void openPacket() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/lucky_money_recieve_open");
			Log.i("tencent","number:" + list.size());
			if(list.isEmpty()){
				Log.i("tencent","null");
				Log.i("tencent","�򿪺������24Сʱ���������ˣ���");
				leavePacket();
			} else {
				Log.i("tencent", "not null");
				for (AccessibilityNodeInfo n: list) {
					Log.i("tencent","��ʼ������ˣ���");
					n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			}
		}
	}
	
	@SuppressLint("NewApi")
	private void leavePacket() {
		boolean test;
		test = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		Log.i("tencent", "back"+test);
	}
	
	@SuppressLint("NewApi") 
	private String getNodeId(AccessibilityNodeInfo node) {
        /* ��������ʽƥ��ڵ�Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo��Ȼ����ֻ��һ��ƥ�䣬��˲������ж�
        objHashMatcher.find();
        Log.i("hash",objHashMatcher.group(0));

        return objHashMatcher.group(0);
    }
	
	 @SuppressLint("NewApi") 
	 private String getHongbaoHash(AccessibilityNodeInfo node) {
	        /* ��ȡ����ϵ��ı� */
	        String content;
	        try {
	            AccessibilityNodeInfo i = node.getParent().getChild(0);
	            content = i.getText().toString();
	        	//content = node.getText().toString();
	        } catch (NullPointerException npr) {
	            return null;
	        }

	        return content + "@" + getNodeId(node);
	        //return content;
	    }
	 
	@Override
	public void onInterrupt() {
		// TODO �Զ����ɵķ������
		
	}

}
