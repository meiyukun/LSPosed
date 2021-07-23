package qingyan.util.lsp;

import android.util.Log;

public class LspUtil {
    public static final String TAG = "BugHook";
    public static void printStack(){
        Exception exception = new Exception("打印调用栈");
        Log.e("TAG", "getAppList: ", exception);
    }
    public static void calculateSpend(String name,Runnable r){
        long l1 = System.currentTimeMillis();
        r.run();
        long l2 = System.currentTimeMillis();
        Log.e(TAG, name+ ": calculateSpend ="+(l2-l1),new Exception(name));
    }
}
