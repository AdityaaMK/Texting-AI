package com.adityaamk.textingai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    String phoneNumber;
    SmsManager smsManager = SmsManager.getDefault();
    int stateNum = 0;
    TextView mainText, subText;

    String[][] aiResp = new String[][]
            {{"Hi this is McDonald's, what would you like to order?","Hello what would you like to order?", "McDonald's here, how can I help you today?", "Good morning. What do you want to order?"},
                    {"Great, what size would you like that in?", "Sounds good, do you want a small, medium, or large?", "Okay, do you want to take that in a small, medium, or large?", "Small, medium, or large?"},
                    {"Okay your total is $8.99.", "Okay your total is $10.99.", "Okay your total is $12.99", "Okay your total is $14.99"},
                    {"Awesome, we are getting your order ready.", "You should receive your order 10 minutes.", "Great, your order will arrive in about 10 minutes", "It'll be ready in about 15 minutes."}};

    String[][] possibleResp = new String[][]
            {{"Hi", "Hello", "Hey", "Wassup", "Yo"},
                    {"Cheeseburger", "Hamburger", "Salad", "Sandwich", "Fries", "Coke", "Pepsi", "Coffee", "Happy Meal", "Chicken McNuggets", "Apple Pie", "Egg McMuffin", "Snack Wrap", "Big Mac"},
                    {"Small", "Medium", "Large"},
                    {"K", "kk", "Ok", "Okay", "Alright", "Aight", "Yes", "Sounds good"}};

    String[] states = new String[]{"Intro", "Size", "Cost", "Done"};

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public boolean progress(String text, String[] possibles) {
        for (String ai : possibles){
            if (text.toLowerCase().contains(ai.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = findViewById(R.id.id_tv);
        subText = findViewById(R.id.id_sub);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS},0);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle!=null) {

                    if(mainText.getText().equals("Waiting for Text")){
                        mainText.setText("");
                    }

                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] smsMessage;

                    assert pdus != null;

                    for (int i = 0; i < pdus.length; i++) {
                        smsMessage = new SmsMessage[pdus.length];
                        smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                        final String text = smsMessage[i].getMessageBody();
                        phoneNumber = smsMessage[i].getOriginatingAddress();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (stateNum == 0 && progress(text, possibleResp[0])) {
                                    smsManager.sendTextMessage(phoneNumber, null, aiResp[stateNum][(int) (Math.random()) * 5], null, null);
                                    subText.setText("Greeted customer");
                                    stateNum++;
                                } else if (stateNum == 1 && progress(text, possibleResp[1])) {
                                    smsManager.sendTextMessage(phoneNumber, null, aiResp[stateNum][(int) (Math.random()) * 5], null, null);
                                    subText.setText("Received order & Asking size");
                                    stateNum++;
                                } else if (stateNum == 2 && progress(text, possibleResp[2])) {
                                    smsManager.sendTextMessage(phoneNumber, null, aiResp[stateNum][(int) (Math.random()) * 5], null, null);
                                    subText.setText("Stating final price");
                                    stateNum++;
                                } else if (stateNum == 3 && progress(text, possibleResp[3])) {
                                    smsManager.sendTextMessage(phoneNumber, null, aiResp[stateNum][(int) (Math.random()) * 5], null, null);
                                    subText.setText("Transaction done and Order is getting ready");
                                    stateNum++;
                                } else {
                                    smsManager.sendTextMessage(phoneNumber, null, "Could you be more clear?", null, null);
                                    subText.setText("Incoherent Response");
                                }
                                if(!subText.getText().equals("Incoherent Response")){
                                    if(stateNum-1!=-1) {
                                        mainText.setText(states[stateNum - 1]);
                                    }
                                    if(stateNum==4)
                                        stateNum=0;
                                }
                            }
                        }, 3000);
                    }
                }
            }
        };
    }

}
