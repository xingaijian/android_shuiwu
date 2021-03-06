package com.rock.jbjianwu;




import com.lib.CDate;
import com.lib.Jiami;
import com.lib.Json;
import com.lib.Rock;
import com.lib.RockActivity;

import com.view.ImageViewXinhu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;


public class LoginActivity extends RockActivity {

	private EditText userobj,passobj;
	private Boolean loginbool = false;
	private String olduser 	  = "",ltype="0";
	private InputMethodManager mInputMethodManager;
	private Button btnobj,gyzmobj;

	/**
	 * 初始化
	 * */
	protected void initCreate() {
		setContentView(R.layout.activity_login);
		findViewById(R.id.btn).setOnClickListener(OnViewClickListener);
		findViewById(R.id.cogbtn).setOnClickListener(OnViewClickListener);
		findViewById(R.id.wjmmbtn).setOnClickListener(OnViewClickListener);
		findViewById(R.id.yzmbtn).setOnClickListener(OnViewClickListener);
		Xinhu.APIURL    = Sqlite.getOption("apiurl", Xinhu.APIURL);
		Xinhu.device	= Jiami.md5(appnum);
		Xinhu.clearlogin();
		msgview = (TextView)findViewById(R.id.msg);
		userobj = (EditText)findViewById(R.id.user);
		passobj = (EditText)findViewById(R.id.pass);
		olduser = Sqlite.getOption("jizhuuser");
		userobj.setText(olduser);
		passobj.setText(Sqlite.getOption("jizhupass"));
		setface();
		Xinhu.stopService(this);
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		String footer = "Copyright ©"+ CDate.now("Y","")+" 德州市人民检察院 "+Rock.getstring(this, R.string.appgwurl)+"";//"+Rock.getstring(this, R.string.appname)+"
		TextView at = (TextView) findViewById(R.id.banquan);
		at.setText(footer);

		btnobj = (Button)findViewById(R.id.btn);
		gyzmobj = (Button)findViewById(R.id.btngyzm);
		gyzmobj.setOnClickListener(OnViewClickListener);

		EditText sjobj = (EditText)findViewById(R.id.mobile);
		sjobj.setText(Sqlite.getOption("jizhumobile"));


		//初始化登录页面
		initlogin();
		initlogins();
	}

	private void setface()
	{
		ImageViewXinhu imga = (ImageViewXinhu)findViewById(R.id.face);
		imga.setPath(Sqlite.getOption("adminface", "images/logo.png"));
	}

	//点击
	protected void ViewClick(View v) {
		int id = v.getId();
		if(id == R.id.btn){
			logincheck();
		}
		if(id == R.id.cogbtn){
			setcog();
		}
		if(id == R.id.wjmmbtn){
			Dialog.alert(this, "请联系管理员修改");
		}
		if(id==R.id.yzmbtn){
			changeyzm();
		}
		if(id==R.id.btngyzm){
			getyzm();
		}
	}

	//切换验证码登录
	private void changeyzm()
	{
		View vsss;
		TextView tevis=(TextView)findViewById(R.id.yzmbtn);
		if(Rock.equals(ltype,"0")){
			vsss = (View)findViewById(R.id.mobile);
			vsss.setVisibility(View.VISIBLE);
			vsss = (View)findViewById(R.id.mobileyzm);
			vsss.setVisibility(View.VISIBLE);
			vsss = (View)findViewById(R.id.user);
			vsss.setVisibility(View.GONE);
			vsss = (View)findViewById(R.id.pass);
			vsss.setVisibility(View.GONE);
			vsss = (View)findViewById(R.id.wjmmbtn);
			vsss.setVisibility(View.GONE);
			ltype = "1";
			tevis.setText("帐号密码登录");
			gyzmobj.setVisibility(View.VISIBLE);
		}else{
			ltype = "0";
			vsss = (View)findViewById(R.id.mobile);
			vsss.setVisibility(View.GONE);
			vsss = (View)findViewById(R.id.mobileyzm);
			vsss.setVisibility(View.GONE);
			vsss = (View)findViewById(R.id.user);
			vsss.setVisibility(View.VISIBLE);
			vsss = (View)findViewById(R.id.pass);
			vsss.setVisibility(View.VISIBLE);
			vsss = (View)findViewById(R.id.wjmmbtn);
			vsss.setVisibility(View.VISIBLE);
			tevis.setText("验证码登录");
			gyzmobj.setVisibility(View.GONE);
		}
	}

	//获取验证码
	private void getyzm()
	{
		EditText eobj;
		String mobile;
		eobj = (EditText)findViewById(R.id.mobile);
		mobile = eobj.getText().toString();
		if (Rock.isEmpt(mobile)) {
			msgtishi("手机号不能为空");
			return;
		}
		if (mobile.length()!=11) {
			msgtishi("手机号格式有误");
			return;
		}
		Sqlite.setOption("jizhumobile", mobile);

		Xinhu.ajaxpost("yanzm","glogin",new String[]{
				"mobile", mobile
		},myhandler, 2);
		gyzmobj.setClickable(false);
		gyzmobj.setText("获取中");
	}

	private void initlogin()
	{
		Xinhu.ajaxget("login","appinit",myhandler, 3);
	}
	private void initlogins()
	{
		int loginyzm = Integer.parseInt(Sqlite.getOption("loginyzm","0"));
		TextView at = (TextView) findViewById(R.id.yzmbtn);
		if(loginyzm==0 || loginyzm==3){
			at.setVisibility(View.GONE);
		}else{
			at.setVisibility(View.VISIBLE);
		}
		if(loginyzm==3){
			ltype="0";
			changeyzm();
		}
		if(loginyzm==0){
			ltype="1";
			changeyzm();
		}
	}

	//设置
	private void setcog()
	{
		if(loginbool)return;
		final EditText inputServer = new EditText(this);
		inputServer.setBackgroundResource(R.drawable.input_login);
		inputServer.setText(Xinhu.APIURL);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置系统地址").setView(inputServer)
				.setNegativeButton("取消", null);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String url = inputServer.getText().toString();
				if(!Rock.isEmpt(url)) {
					url = url.toLowerCase();
					if(url.length()<6 || !Rock.equals(url.substring(0, 4), "http")){
						msgerror("格式有误，需http开头和/结尾");
						return;
					}
					String lzf = url.substring(url.length()-1);
					if(!Rock.equals(lzf,"/"))url+="/";
					Sqlite.setOption("apiurl", url);
					if (!Rock.equals(Xinhu.APIURL, url)) Sqlite.clearalltable();//地址改变了，清空所有记录
					Xinhu.APIURL = url;
					setface();
					initlogin();
					msgtishi("设置成功");
				}
			}
		});
		builder.setNeutralButton("粘贴",null);
		AlertDialog dialog =builder.show();
		dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String txt = Rock.getcopy(mActivity);
				inputServer.setText(txt);
			}
		});
	}

	//登录
	private void logincheck()
	{
		if(loginbool)return;
		String user,pass="",yanzm="";
		EditText eobj;
		if(Rock.equals(ltype,"0")) {
			user = userobj.getText().toString();
			pass = passobj.getText().toString();
			if (Rock.isEmpt(user)) {
				msgtishi("用户名不能为空");
				return;
			}
			if (Rock.isEmpt(pass)) {
				msgtishi("密码不能为空");
				return;
			}
		}else{
			eobj = (EditText)findViewById(R.id.mobile);
			user = eobj.getText().toString();
			if (Rock.isEmpt(user)) {
				msgtishi("手机号不能为空");
				return;
			}
			if (user.length()!=11) {
				msgtishi("手机号格式有误");
				return;
			}
			eobj = (EditText)findViewById(R.id.mobileyzm);
			yanzm = eobj.getText().toString();
			if (Rock.isEmpt(yanzm)) {
				msgtishi("验证码不能为空");
				return;
			}
			if (yanzm.length()!=6) {
				msgtishi("验证码必须是6位数字");
				return;
			}
			Sqlite.setOption("jizhumobile", user);
		}

		btnobj.setBackground(getResources().getDrawable(R.drawable.btn_hui));
		btnobj.setText("登录中...");
		loginbool = true;

		if(!Rock.equals(olduser, user)){
			Sqlite.setOption("adminface", "");
			setface();
		}

		// 隐藏输入法
		if (mInputMethodManager.isActive()) {
			mInputMethodManager.hideSoftInputFromWindow(passobj.getWindowToken(), 0);
		}
		userobj.clearFocus();
		passobj.clearFocus();
		Sqlite.setOption("jizhuuser", user);
		Sqlite.setOption("jizhupass", pass);
		Xinhu.ajaxpost("login","check",new String[]{
				"user", Jiami.base64encode(user),
				"pass", Jiami.base64encode(pass),
				"ltype", ltype,
				"yanzm", yanzm
		},myhandler, 1);
	}


	protected void handleCallback(Message msg, String bstr)
	{
		//登录的请求
		if(msg.what==1) {
			loginbool = false;
			if(msg.arg1==0){
				msgerror(bstr);
				btnobj.setText("登 录");
				btnobj.setBackground(getResources().getDrawable(R.drawable.btn_zhu));
			}else{
				btnobj.setText("登录成功");
				Map<String, String> data;
				data = Json.getJsonObject(bstr);
				//换帐号登录
				if(!Rock.equals(data.get("uid"), Sqlite.getOption("adminoldid"))){
					Sqlite.clearalltable();
				}
				Sqlite.setOption("adminid", data.get("uid"));
				Sqlite.setOption("adminoldid", data.get("uid"));
				Sqlite.setOption("adminname", data.get("name"));
				Sqlite.setOption("adminface", data.get("face"));
				Sqlite.setOption("admintoken", data.get("token"));
				Sqlite.setOption("apptitle", data.get("title"));
				Sqlite.setOption("loginyzm", data.get("loginyzm"));
				Sqlite.setOption("adminjson", bstr);
				startSimpleActivity(IndexActivity.class);
				finish();
			}
		}

		//获取验证码
		if(msg.what==2) {
			if(msg.arg1==0){
				msgerror(bstr);
				gyzmobj.setClickable(true);
				gyzmobj.setText("重新获取");
			}else{
				msgtishi("验证码已发送到手机上");
				gyzmobj.setBackground(getResources().getDrawable(R.drawable.btn_hui));
				dshitime(60);
			}
		}

		//初始化
		if(msg.what==3) {
			if(msg.arg1!=0){
				Map<String, String> data;
				data = Json.getJsonObject(bstr);
				Sqlite.setOption("apptitle", data.get("title"));
				Sqlite.setOption("loginyzm", data.get("loginyzm"));
				initlogins();
			}else{
				msgerror("设置地址不可用");
			}
		}
	}

	//验证码定时
	private void dshitime(final int time)
	{
		if(time==0){
			gyzmobj.setClickable(true);
			gyzmobj.setText("重新获取");
			gyzmobj.setBackground(getResources().getDrawable(R.drawable.btn_zhu));
			return;
		}
		gyzmobj.setText(""+time+"");
		RunnableTime(new Runnable() {
			@Override
			public void run() {
				dshitime(time-1);
			}
		},1000);
	}

}