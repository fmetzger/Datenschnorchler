package fm.datenschnorchler;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SchnorchelActivity extends Activity {

    private static final String SEP = ";";
    private List<String> calllog;
    private List<String> phonecontacts;
    private List<String> sms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schnorchel);

        Log.d("Datenschnorchler", "calling logger");
        readCallLog();
        readPhonebook();
        readSms();
        writecsv();
    }


    private void readCallLog() {
        calllog = new LinkedList<String>();

        String columns[] = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME};
        Cursor c;
//        c = getContentResolver().query(Uri.parse("content://call_log/calls"),
//                columns, null, null, "Calls._ID DESC"); q
        c = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                columns, null, null,  CallLog.Calls.DATE + " DESC");
        while (c.moveToNext()) {
            long dialed = c.getLong(c.getColumnIndex(CallLog.Calls.DATE));
            String number = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            long duration = c.getLong(c.getColumnIndex(CallLog.Calls.DURATION));
            String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String type;
            switch(c.getInt(c.getColumnIndex(CallLog.Calls.TYPE))){
                case(CallLog.Calls.INCOMING_TYPE):
                    type = "incoming";
                    break;
                case(CallLog.Calls.OUTGOING_TYPE):
                    type = "outgoing";
                    break;
                case(CallLog.Calls.MISSED_TYPE):
                    type = "missed";
                    break;
                default:
                    type = "n/a";
            }
            calllog.add(name+SEP+number+SEP+dialed+SEP+duration+SEP+type);
            Log.i("Datenschnorchler", "type: " + type + "; call to number: " + number + "; registered at: " + dialed + "; duration: " + duration + "; name: " + name);
        }
    }


    private void readPhonebook() {
        phonecontacts = new LinkedList<String>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phonecontacts.add(name+SEP+number);
            Log.i("Datenschnorchler", "name: " + name + "; number: " + number);
        }

//        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
//        while(c.moveToNext()){
//            String a = c.getString(c.getColumnIndex(ContactsContract.Contacts.Entity.));
//            String data = c.getString(c.getColumnIndex(ContactsContract.RawContacts.));
//        }

    }

    private void readSms(){
        // https://github.com/android/platform_frameworks_base/blob/ics-mr1-release/core/java/android/provider/Telephony.java
        sms = new LinkedList<String>();

        Cursor c = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);

        while(c.moveToNext()){
            String id = c.getString(c.getColumnIndexOrThrow("_id"));
            String address = c.getString(c.getColumnIndexOrThrow("address"));
            String person = c.getString(c.getColumnIndexOrThrow("person"));
            String body = c.getString(c.getColumnIndexOrThrow("body"));
            String readstate = c.getString(c.getColumnIndex("read"));
            String seen = c.getString(c.getColumnIndex("seen"));
            String date_received = c.getString(c.getColumnIndexOrThrow("date"));
            //String date_sent = c.getString(c.getColumnIndexOrThrow("date_sent"));
            String subject = c.getString(c.getColumnIndexOrThrow("subject"));
            String protocol = c.getString(c.getColumnIndexOrThrow("protocol"));
            String reply_path_present = c.getString(c.getColumnIndexOrThrow("reply_path_present"));
            String service_center = c.getString(c.getColumnIndexOrThrow("service_center"));
            String locked = c.getString(c.getColumnIndexOrThrow("locked"));
            String error_code = c.getString(c.getColumnIndexOrThrow("error_code"));
            String location;
            switch (c.getInt(c.getColumnIndexOrThrow("type"))){
                case(0):
                    location = "all";
                    break;
                case(1):
                    location = "inbox";
                    break;
                case(2):
                    location = "sent";
                    break;
                case(3):
                    location = "draft";
                    break;
                case(4):
                    location = "outbox";
                    break;
                case(5):
                    location = "failed";
                    break;
                case(6):
                    location = "queued";
                    break;
                default:
                    location = "n/a";

            }
            String status;
            switch(c.getInt(c.getColumnIndexOrThrow("status"))){
                case(-1):
                    status = "none";
                    break;
                case(0):
                    status = "complete";
                    break;
                case(32):
                    status = "pending";
                    break;
                case(64):
                    status = "failed";
                    break;
                default:
                    status = "n/a";

            }
            sms.add(id + SEP + address + SEP + person + SEP + readstate + SEP + date_received + SEP + location + SEP + seen + SEP + subject
                    + SEP + protocol + SEP + reply_path_present + SEP + service_center + SEP + locked + SEP + error_code + SEP + status + SEP + body);
            Log.i("Datenschnorchler", "id: " + id + "; address: " + address + "; person: " + person + "; body: " + body + "; readstate: " + readstate + "; date_received: " + date_received + "; location: " + location + "; seen: " + seen +
            "; subject: " + subject + "; protocol: " + protocol + "; reply_path_present: " + reply_path_present +
            "; service_center: " + service_center + "; locked: " + locked + "; error_code: " + error_code + "; status: " + status);
        }
        c.close();


    }


    private void writecsv(){
        File calllogFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "schnorchel-calllog.csv");
        File phonecontactsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "schnorchel-phonecontacts.csv");
        File smsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "schnorchel-sms.csv");

        try {

            FileWriter calllogWriter = new FileWriter(calllogFile);
            BufferedWriter bufCalllogWriter = new BufferedWriter(calllogWriter);
            bufCalllogWriter.write("name"+SEP+"number" + SEP + "time" + SEP + "duration" + SEP + "type");
            bufCalllogWriter.newLine();
            for(String line: calllog){
                bufCalllogWriter.write(line);
                bufCalllogWriter.newLine();
            }

            FileWriter phonecontactsWriter = new FileWriter(phonecontactsFile);
            BufferedWriter bufPhonecontactsWriter = new BufferedWriter(phonecontactsWriter);
            bufPhonecontactsWriter.write("name"+SEP+"number");
            bufPhonecontactsWriter.newLine();
            for(String line: phonecontacts){
                bufPhonecontactsWriter.write(line);
                bufPhonecontactsWriter.newLine();
            }

            FileWriter smsWriter = new FileWriter(smsFile);
            BufferedWriter bufsmsWriter = new BufferedWriter(smsWriter);
            bufsmsWriter.write("id" + SEP + "address" + SEP + "person" + SEP + "readstate" + SEP + "date_received" + SEP + "location" + SEP + "seen" + SEP + "subject"
                    + SEP + "protocol" + SEP + "reply_path_present" + SEP + "service_center" + SEP + "locked" + SEP + "error_code" + SEP + "status" + SEP + "body");
            bufsmsWriter.newLine();
            for(String line: sms){
                bufsmsWriter.write(line);
                bufsmsWriter.newLine();
            }

            bufPhonecontactsWriter.close();
            bufCalllogWriter.close();
            bufsmsWriter.close();
            phonecontactsWriter.close();
            calllogWriter.close();
            smsWriter.close();

        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.d("Datenschnorchler", sw.toString());
        }


    }



    private boolean externalMediaWriteable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schnorchel, menu);
        return true;
    }

}
