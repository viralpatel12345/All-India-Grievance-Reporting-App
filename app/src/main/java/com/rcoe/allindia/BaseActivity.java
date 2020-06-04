package com.rcoe.allindia;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity
{

    void copyToClipboard(Context context,String str)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("COPY", str);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    void alert(final int resource, final Context context, final FirebaseAuth firebaseAuth)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(resource==R.layout.quit_alert)
                {
                    finish();
                }
                else if(resource==R.layout.logout_alert)
                {
                    firebaseAuth.signOut();
                    startActivity(new Intent(context, LoginActivity.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                }

            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    void success_alert(final Context context, String data)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert,null);
        TextView tv = view.findViewById(R.id.message);
        tv.setText(data);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(context, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    void passwordAlertdialogBox(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.alert_dialouge,null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog ad= builder.create();
        ad.show();
    }

    void share()
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "https://drive.google.com/open?id=1FmTnfl6juTR8u4gDsrjF3Gj1-tT1mXZ_";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share App");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share App"));
    }

    public static boolean isConnected(Context context)
    {
        boolean connected = false;
        try
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        }
        catch (Exception e)
        {
            Toast.makeText(context,"Connectivity Exception : "+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return connected;
    }
}
