package com.xengine.android.system.ssm;

import com.xengine.android.system.ui.XUIFrame;
import com.xengine.android.system.ui.XUIFrameState;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

/**
 * 完成系统状态管理器。
 * 监听系统中所有的activity的生命周期。
 * <pre>
 * 系统的状态：
 *      ESTABLISHED:系统刚刚启动，第一个activity刚刚创建。
 *      ACTIVE:当系统中有activity处于活跃周期。
 *      全退出，所有的activity都被销毁了。INACTIVE:当系统中所有的activity都退出了可见周期（比如切出去打电话了，按了home键切出去了）。
 *      EXIT:系统完
 * </pre>
 * Created by 赵之韵.
 * Date: 11-12-18
 * Time: 上午2:03
 */
public class XAndroidSSM implements XSystemStateManager {

    private static final String TAG = "SSM";

    private static XAndroidSSM instance;


    public synchronized static XAndroidSSM getInstance() {
        if(instance == null) {
            instance = new XAndroidSSM();
        }
        return instance;
    }

    /**
     * 用一个栈来管理进入可见周期的窗口。
     */
    private Stack<XUIFrame> visibleStack = new Stack<XUIFrame>();

    /**
     * 系统状态的监听者。
     */
    private ArrayList<XSystemStateListener> listeners = new ArrayList<XSystemStateListener>();

    /**
     * 记录系统中的activity当前的状态。
     */
    private HashMap<XUIFrame, XUIFrameState> frameStates = new HashMap<XUIFrame, XUIFrameState>();

    /**
     * 当前系统的状态。
     */
    private XSystemState currentSystemState = XSystemState.EXIT;

    private XAndroidSSM() {}

    /**
     * 注册系统状态监听器
     */
    @Override
    public synchronized void registerSystemStateListener(XSystemStateListener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 注销系统状态监听器
     */
    @Override
    public synchronized void unregisterSystemStateListener(XSystemStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知系统状态管理器，窗口的状态改变了
     * @param uiFrame 窗口
     * @param newState 新的状态
     */
    @Override
    public synchronized void notifyUIStateChanged(XUIFrame uiFrame, XUIFrameState newState) {
        XLog.d(TAG, uiFrame.getName() + " enter " + newState);
        frameStates.put(uiFrame, newState);

        try {
            XUIFrame topUI = visibleStack.peek();
            if(uiFrame == topUI) {
                switch (newState) {
                    case RESUMED:
                        // 当位于栈顶的activity进入了活跃状态的话。
                        // 系统就进入了活跃状态。
                        if(currentSystemState != XSystemState.ACTIVE) {
                            XLog.d(TAG, "System ACTIVE.");
                            currentSystemState = XSystemState.ACTIVE;
                            tellListeners(XSystemState.ACTIVE);
                        }
                        break;
                    case STOPPED:
                        // 位于栈顶的activity退出了可见周期。
                        // 系统就进入了非活跃状态。
                        XLog.d(TAG, "System INACTIVE.");
                        currentSystemState = XSystemState.INACTIVE;
                        tellListeners(XSystemState.INACTIVE);
                        visibleStack.pop();
                        break;
                }
            }else {
                switch (newState) {
                    // 当activity不是位于栈顶。
                    case STARTED:
                        // 进入可见周期的时候。
                        // 就将activity压入栈。
                        visibleStack.push(uiFrame);
                        break;
                    case STOPPED:
                        // 退出可见周期。
                        // 直接从栈中删除。
                        visibleStack.remove(uiFrame);
                        break;
                }
            }
        }catch (EmptyStackException e) {
            switch (newState) {
                case CREATED:
                    // 系统刚创建的时候activity栈是空的。
                    // 此时有新activity创建的话，系统就进入ESTABLISHED状态。
                    XLog.d(TAG, "System ESTABLISHED.");
                    currentSystemState = XSystemState.ESTABLISHED;
                    tellListeners(XSystemState.ESTABLISHED);
                    break;
                case STARTED:
                    // 系统刚刚创建完成，activity仍然是空的。
                    // 此时新activity已经进入可见周期，可以压入栈了。
                    visibleStack.push(uiFrame);
                    break;
                case DESTROYED:
                    // 所有的activity都退出了可见周期。
                    // 最后一个activity被销毁的话。
                    // 系统就退出了。
                    XLog.d(TAG, "System EXIT.");
                    currentSystemState = XSystemState.EXIT;
                    tellListeners(XSystemState.EXIT);
                    break;
            }
        }
    }

    /**
     * 返回当前系统的状态。
     */
    @Override
    public XSystemState getCurrentSystemState() {
        return currentSystemState;
    }

    /**
     * 获取当前处于活跃状态的窗口
     */
    @Override
    public XUIFrame getCurrentActiveUIFrame() {
        if(currentSystemState == XSystemState.ACTIVE) {
            return visibleStack.peek();
        }else {
            return null;
        }
    }

    /**
     * 将新的系统状态通知所有的listener
     * @param newState 系统新的状态
     */
    private void tellListeners(XSystemState newState) {
        for(XSystemStateListener listener: listeners) {
            listener.onSystemStateChanged(newState);
        }
    }

}
