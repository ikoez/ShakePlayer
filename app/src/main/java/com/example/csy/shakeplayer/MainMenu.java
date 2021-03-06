package com.example.csy.shakeplayer;


import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button chooseFile = (Button) findViewById(R.id.choosefile);
        Button webBrowser = (Button) findViewById(R.id.webbrowser);

        webBrowser.setOnClickListener(new ButtonClickListener());
        chooseFile.setOnClickListener(new ButtonClickListener());
    }

    class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.choosefile) {
                String path="/mnt/sdcard/shakeplayer/";

                File file=new File(path);
                if (!file.exists()){
                    Toast.makeText(MainMenu.this, "There is no file", Toast.LENGTH_SHORT).show();
                    file.mkdir();
                }
                else{
                    File[] filelist =file.listFiles();
                    if (filelist.length>0) {
                        Intent intent = new Intent();
                        intent.setClass(MainMenu.this, FindFile.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainMenu.this, "Please download MP3 into Soundspread or Download folder", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            if (v.getId() == R.id.webbrowser) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                String link="http://www.google.ca";
                Uri contenturl = Uri.parse(link);
                intent.setData(contenturl);
                startActivity(intent);
            }
        }


    }
}
