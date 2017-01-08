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
	// 定义一个存储红包hash
	String last = "我要抢红包";
	
	@Override
	protected void onServiceConnected() {
		Log.i(TAG, "config success!");
	}
	
	@SuppressLint("NewApi") 
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO 自动生成的方法存根
		int eventType = event.getEventType();
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			System.out.println("notification: " + event.getText());
		}
		switch(eventType) {
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			System.out.println("notification: " + event.getText());
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				// 做循环查找出含有红包字段的消息
				for (CharSequence text: texts) {
					String content = text.toString();
					//Log.i("demo","text:"+content);
					if (content.contains("[微信红包]")) {
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
			if (event.getText().toString().contains("[微信红包]") && event.getPackageName().toString().equals("com.tencent.mm")) {
				Log.i("tencent","微信红包aa");
				getPacket();
			} else if (className.equals("com.tencent.mm.ui.LauncherUI")) {
				Log.i("tencent","开始抢红包");// 这里如果是自己发的红包是查看红包
				getPacket();			
			} else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f")) {
				Log.i("tencent","打开红包，大兄弟");
				openPacket();
			} else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")){
				Log.i("tencent","红包已进账，大兄弟");
				leavePacket();
			} else {
				Log.i("tencent","啥都没有，大兄弟");
			}
		break;
		}
	}

	
	// 唤醒屏幕解锁,监听系统广播
	
	// 接受红包,需要过滤出可抢的红包，不可抢的红包如：超过24小时过期的红包、已经抢过的红包
	@SuppressLint("NewApi")
	private void getPacket() {
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		// 这里检测到领取红包后，发送点击命令
		if (rootNode == null) {
			Log.i("tencent", "error!!");
		} else {
			//recycle(rootNode);
			List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/appmsg_c2c_desc");
			Log.i("tencent","总共红包数量为："+list.size());
			if (list.isEmpty()) {
				Log.i("tencent","没有红包了");
			} else if (last.equals(getHongbaoHash(list.get(list.size()-1)))) {
				// 证明是第一次抢红包
				last = getHongbaoHash(list.get(list.size()-1));
			} else {
				// hash不相同，则抢红包
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
				Log.i("tencent","打开红包超过24小时，或手慢了！！");
				leavePacket();
			} else {
				Log.i("tencent", "not null");
				for (AccessibilityNodeInfo n: list) {
					Log.i("tencent","开始抢红包了！！");
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
        /* 用正则表达式匹配节点Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
        objHashMatcher.find();
        Log.i("hash",objHashMatcher.group(0));

        return objHashMatcher.group(0);
    }
	
	 @SuppressLint("NewApi") 
	 private String getHongbaoHash(AccessibilityNodeInfo node) {
	        /* 获取红包上的文本 */
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
		// TODO 自动生成的方法存根
		
	}

}
