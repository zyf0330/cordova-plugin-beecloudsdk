package beecloudsdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import cn.beecloud.BCPay;
import cn.beecloud.BeeCloud;
import cn.beecloud.async.BCCallback;
import cn.beecloud.async.BCResult;
import cn.beecloud.entity.BCPayResult;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Pay extends CordovaPlugin {
    private static final String TAG = "Pay";

    private String[] payTypes = {"wx_app", "ali_app"};
    private ProgressDialog loadingDialog;
    private boolean isInit = false;
    private String payCallbackId;

    //支付结果返回入口
    private BCCallback bcCallback = new BCCallback() {
        @Override
        public void done(final BCResult bcResult) {
            final BCPayResult bcPayResult = (BCPayResult) bcResult;
            String result = bcPayResult.getResult();
            String msg;
            boolean ok = false;
            if (result.equals(BCPayResult.RESULT_SUCCESS)) {
                ok = true;
                msg = "用户支付成功";
            } else if (result.equals(BCPayResult.RESULT_CANCEL)) {
                msg = "用户取消支付";
            } else if (result.equals(BCPayResult.RESULT_FAIL)) {
                msg = "支付失败";
                Log.e(TAG, "errcode: " + bcPayResult.getErrCode() + ", msg: " + bcPayResult.getErrMsg() + ", detail: " + bcPayResult.getDetailInfo());
            } else if (result.equals(BCPayResult.RESULT_UNKNOWN)) {
                //可能出现在支付宝8000返回状态
                msg = "订单状态未知";
            } else {
                msg = "非法结果";
            }
            PluginResult pr;
            if(ok){
                pr = new PluginResult(PluginResult.Status.OK, msg);
            }else{
                pr = new PluginResult(PluginResult.Status.ERROR, msg);
            }
            webView.sendPluginResult(pr, payCallbackId);
            endPay();
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals("init")){
            JSONObject res = new JSONObject();
            Context context = webView.getContext();
            //TODO 需要修改为实际值
            boolean sandbox = false;
            String appId = "";
            String appSecret = "";
            String wxAppId = null;

            BeeCloud.setSandbox(sandbox);
            BeeCloud.setAppIdAndSecret(appId, appSecret);
            String errmsg = BCPay.initWechatPay(context, wxAppId);
            if (errmsg != null) {
                errmsg = errmsg.replaceFirst("Error: ?", "");
                res.put("wxinitmsg", "微信支付初始化失败");
            }
            loadingDialog = new ProgressDialog(context);
            loadingDialog.setMessage("处理中，请稍候...");
            loadingDialog.setIndeterminate(true);
            loadingDialog.setCancelable(true);

            isInit = true;
            callbackContext.success(res);
            return true;
        }
        if(!isInit){
            callbackContext.error("尚未初始化");
            return true;
        }
        //支付
        if(this.payCallbackId != null){
            callbackContext.error("有正在进行的支付");
            return true;
        }
        boolean validPayType = false;
        for(String payType : payTypes){
            if(payType.equals(action)){
                validPayType = true;
                break;
            }
        }
        if(validPayType){
            startPay(callbackContext.getCallbackId());
            final String title = args.getString(0);
            final Integer totalfee = args.getInt(1);
            final String orderno = args.getString(2);
            final JSONObject optional = args.optJSONObject(3);
            if ("wx_app".equals(action)) {
                //微信app
                if (BCPay.isWXAppInstalledAndSupported() && BCPay.isWXPaySupported()) {
                    cordova.getThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            BCPay.getInstance(webView.getContext()).reqWXPaymentAsync(title, totalfee, orderno, jsonToMap(optional), bcCallback);
                        }
                    });
                } else {
                    endPay();
                    callbackContext.error("尚未安装微信或者安装的微信版本不支持");
                }
            }else if("ali_app".equals(action)){
                //支付宝app
                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        BCPay.getInstance(webView.getContext()).reqAliPaymentAsync(title, totalfee, orderno, jsonToMap(optional), bcCallback);
                    }
                });
            }
            return true;
        }else{
            return false;
        }
    }
    private void startPay(String callbackId){
        this.payCallbackId = callbackId;
        loadingDialog.show();
    }
    public void endPay(){
        this.payCallbackId = null;
        loadingDialog.dismiss();
    }
    private Map<String, String> jsonToMap(JSONObject json) {
        if (json != null) {
            Map<String, String> map = new HashMap<String, String>();
            Iterator i = json.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                try {
                    map.put(key, json.get(key).toString());
                } catch (JSONException e) {
                    Log.e(TAG, "json转换错误", e);
                    map.put(key, "null");
                }
            }
            return map;
        } else {
            return null;
        }
    }
}