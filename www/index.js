//支付
var pay = exports.pay = {};
/**
 * 初始化支付sdk
 */
pay.init = function() {
		cordova.exec(function(res) {
			var wxinitmsg = res.wxinitmsg;
			//微信支付初始化失败
			if (wxinitmsg) {
				pay.wx_app = function() {
					var cb = arguments[arguments.length - 1];
					typeof cb == 'function' && cb(new Error(wxinitmsg));
				}
			}
		}, function(err) {

		}, 'Pay', 'init', []);
	}
	/**
	 * 微信app支付
	 */
pay.wx_app = function(title, totalfee, orderNo, optional, cb) {
	payExec('wx_app', [title, totalfee, orderNo, optional], cb);
}
pay.ali_app = function(title, totalfee, orderNo, optional, cb) {
		payExec('ali_app', [title, totalfee, orderNo, optional], cb);
	}
	/**
	 * 发起支付请求
	 * @param {string} action pay方法
	 * @param {array} args 订单参数。[title, totalfee, orderNo, optionalObj]
	 * @param {function} cb(err, ) 返回error，或结果
	 */
function payExec(action, args, cb) {
	cordova.exec(function(info) {
		cb(null, info);
	}, function(errmsg) {
		cb(new Error(errmsg));
	}, "Pay", action, args);
}

module.exports = exports;
