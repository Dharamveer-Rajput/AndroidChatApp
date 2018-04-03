package com.androidchatapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.authentication.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class Chat extends AppCompatActivity {

    LinearLayout layout1;
    RelativeLayout layout_2;
    ImageView sendButton,cameraClick,imageViewCamera;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    Bitmap bitmap;
    ProgressBar progressBar;
    String currTime;

    public static String getFormatedTime(long dateTime,String timeZone, String format){
        String time = null;
        try
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateTime);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
            time = sdf.format(cal.getTime());
        }
        catch(Exception e){
            //logger.log(Level.SEVERE,"\n ERROR**********, Exception during get formated time: "+e+"\n");
        }
        return time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Calendar calendar = Calendar.getInstance();
        TimeZone zone = calendar.getTimeZone();
        final String timeZone = zone.getID();


        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        cameraClick = (ImageView) findViewById(R.id.cameraClick);

        Firebase.setAndroidContext(this);

        reference1 = new Firebase("https://androidchatapp-d7c0e.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://androidchatapp-d7c0e.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);


        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(Chat.this);
                    startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);

                } catch(ActivityNotFoundException anfe){
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    Toast.makeText(Chat.this, errorMessage, Toast.LENGTH_SHORT).show();
                }


            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                String currTime = getFormatedTime(System.currentTimeMillis(),timeZone,"MMM dd, hh:mm a");

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("time",currTime);
                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String time = map.get("time").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox("You:-\n" + message, 1,time);
                }
                else{
                    addMessageBox(UserDetails.chatWith + ":-\n" + message, 2,time);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type,String time){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        TextView textViewTime = new TextView(this);
        textViewTime.setText(time);
        textViewTime.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        imageViewCamera = new ImageView(Chat.this);

        progressBar = new ProgressBar(Chat.this,null,android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);



        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        lp2.weight = 1.0f;


        if(type == 1) {

            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);


        }
        else
        {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);

        }

        textView.setLayoutParams(lp2);
        imageViewCamera.setLayoutParams(lp2);
        layout1.addView(textView);
        layout1.addView(imageViewCamera);
        layout1.addView(textViewTime);

        scrollView.fullScroll(View.FOCUS_DOWN);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_IMAGE_ID:

                bitmap = ImagePicker.getImageFromResult(this, resultCode, data);


                bitmap = getRoundedBitmap(bitmap, 20);

                bitmap = addBorderToRoundedBitmap(bitmap, 20, 4, Color.YELLOW);

                bitmap = addBorderToRoundedBitmap(bitmap, 20, 4, Color.DKGRAY);

                imageViewCamera.setImageBitmap(bitmap);

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    protected Bitmap getRoundedBitmap(Bitmap srcBitmap, int cornerRadius) {
        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap.getWidth(), // Width
                srcBitmap.getHeight(), // Height

                Bitmap.Config.ARGB_8888 // Config
        );
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect rect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        RectF rectF = new RectF(rect);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(srcBitmap, 0, 0, paint);

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
    }

    // Custom method to add a border around rounded bitmap
    protected Bitmap addBorderToRoundedBitmap(Bitmap srcBitmap, int cornerRadius, int borderWidth, int borderColor){
        // We will hide half border by bitmap
        borderWidth = borderWidth*2;

        // Initialize a new Bitmap to make it bordered rounded bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap.getWidth() + borderWidth, // Width
                srcBitmap.getHeight() + borderWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        // Initialize a new Canvas instance
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance to draw border
        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);

        // Initialize a new Rect instance
        Rect rect = new Rect(
                borderWidth/2,
                borderWidth/2,
                dstBitmap.getWidth() - borderWidth/2,
                dstBitmap.getHeight() - borderWidth/2
        );

        // Initialize a new instance of RectF;
        RectF rectF = new RectF(rect);

        // Draw rounded rectangle as a border/shadow on canvas
        canvas.drawRoundRect(rectF,cornerRadius,cornerRadius,paint);

        // Draw source bitmap to canvas
        canvas.drawBitmap(srcBitmap, borderWidth / 2, borderWidth / 2, null);


        srcBitmap.recycle();

        // Return the bordered circular bitmap
        return dstBitmap;
    }
}