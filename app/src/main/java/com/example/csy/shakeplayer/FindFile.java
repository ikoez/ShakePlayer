package com.example.csy.shakeplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindFile extends AppCompatActivity {
    private String[] items;
    private String[] itempath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_file);
        scanfolder();

        ListView listView = (ListView)findViewById(R.id.findfile);

        listView.setAdapter(new ArrayAdapter<String>(FindFile.this,
                android.R.layout.simple_list_item_1,
                items));


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(FindFile.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("uri", itempath[arg2]);
                Toast.makeText(FindFile.this, itempath[arg2], Toast.LENGTH_SHORT);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }

        });
    }

    public void scanfolder(){
        String path="/mnt/sdcard/shakeplayer/";
        File file=new File(path);
        File[] filelist =file.listFiles();
        List<String> filename=new ArrayList<String>();
        List<String> filepath=new ArrayList<String>();

        for(int i=0;i<filelist.length;i++){
            // filepath.add(path+filelist[i].getName().toString());
            Pattern p = Pattern.compile(".+\\..+");
            Matcher m=p.matcher(filelist[i].getName().toString());
            if(m.find()==true)
            {

                filepath.add(path+filelist[i].getName().toString());
                filename.add(filelist[i].getName().toString());
            }

        }


        String path2="/mnt/sdcard/Download/";
        // Toast.makeText(FindFile.this, path, Toast.LENGTH_SHORT);
        File file2=new File(path2);
        File[] filelist2 =file2.listFiles();


        for(int i=0;i<filelist2.length;i++){
            // filepath.add(path+filelist[i].getName().toString());
            Pattern p = Pattern.compile(".+\\..+");
            String namestring=filelist2[i].getName().toString();
            Matcher m=p.matcher(namestring);
            if((m.find()==true)&(namestring.endsWith(".mp3")))
            {

                filepath.add(path2+namestring);
                filename.add(namestring);
            }

        }

        items=new String[filename.size()];
        for(int i=0;i<filename.size();i++)
            items[i]=filename.get(i).toString();
        itempath=new String[filepath.size()];
        for(int i=0;i<filepath.size();i++)
            itempath[i]=filepath.get(i).toString();

    }
}
