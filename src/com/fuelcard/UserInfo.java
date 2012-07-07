package com.fuelcard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.ksoap2.SoapEnvelope;
//import org.ksoap2.serialization.SoapObject;
//import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserInfo extends Activity {
	private String URL = "http://service.fuelcarddirect.co.uk/fuelcardsapple.asmx";
	private String METHOD_NAME = "InsertEnquiry";
	private String NAMESPACE = "http://tempuri.org/";
	private String SOAP_ACTION = "http://tempuri.org/InsertEnquiry";
//	private SoapObject requestSOAP = null;
//	private SoapSerializationEnvelope envelope = null;
//	private HttpTransportSE androidHttpTransport = null;
	
	private Runnable getSOAPService;
	private ProgressDialog dialog = null;
	private Context context=this;
	
	private EditText name,company,email;
	private Button register,dont;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo);
        
        register=(Button)this.findViewById(R.id.register);
        dont=(Button)this.findViewById(R.id.dont);
        name=(EditText)this.findViewById(R.id.name);
        company=(EditText)this.findViewById(R.id.companyname);
        email=(EditText)this.findViewById(R.id.email);
        
        register.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(!getErrors()){
        			getSOAPService = new Runnable(){
			            
			            public void run() {
			                getData();
			            }
			        };
			        Thread thread =  new Thread(null, getSOAPService, "Background");
			        thread.start();
			        dialog =new ProgressDialog(context);
			        dialog.setMessage("Registering...Please Wait");
			        dialog.setIndeterminate(true);
			        dialog.setCancelable(false);
			        dialog.show();
        		}
        	}
        });
        dont.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent intent= new Intent(UserInfo.this,Search.class );
    			startActivity(intent);
    			finish();
        	}
        });
    }
	public void getData(){
		try{
//			requestSOAP = new SoapObject(NAMESPACE, METHOD_NAME);
//			requestSOAP.addProperty("FullName",name.getText().toString());
//			requestSOAP.addProperty("CompanyName",company.getText().toString());
//			requestSOAP.addProperty("Email", email.getText().toString());
//		
//			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//			envelope.dotNet = true;
//			envelope.setOutputSoapObject(requestSOAP);
//			androidHttpTransport = new HttpTransportSE(URL);
//			androidHttpTransport.call(SOAP_ACTION, envelope);
//
//			// 	resultSOAP
//			SoapObject resultSOAP = (SoapObject) envelope.getResponse();
//			System.out.println(resultSOAP.getProperty(0));
//			Intent intent= new Intent(UserInfo.this,Search.class );
//			startActivity(intent);
//			finish();
		}
		catch(Exception e){System.out.println(e.toString());}
		runOnUiThread(UIUpdate);
	}
	private Runnable UIUpdate = new Runnable() {
        
        public void run() {
        	dialog.dismiss();
        }
	};
    public boolean getErrors(){
    	if(name.getText().toString().trim().length()==0){
    		name.setError("Please enter Name");
    		return true;
    	}
    	if(company.getText().toString().trim().length()==0){
    		company.setError("Please enter Company Name");
    		return true;
    	}
    	if(email.getText().toString().trim().length()>0){
    		String EMAIL_PATTERN ="^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,4})$";
    		Pattern pattern= Pattern.compile(EMAIL_PATTERN);
    		Matcher matcher= pattern.matcher(email.getText().toString());
			boolean errorStatus=matcher.matches();
			if(errorStatus==false){
				email.setError("invalid email address");
				email.requestFocus();
				return true;
			}
    	}
    	return false;
    }
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Ignore orientation change not to restart activity
		super.onConfigurationChanged(newConfig);
		}
}