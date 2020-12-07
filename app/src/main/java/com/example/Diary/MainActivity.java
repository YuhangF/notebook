package com.example.Diary;


import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnClickListener{
    private List<Integer> IDList = new ArrayList<>();
    private List<String> TADList = new ArrayList<>();
    private List<String>  imageList=new ArrayList<>();
    ArrayAdapter simpleAdapter;
    Button ButtonSeek;
    EditText EditTextSeek;
    String EditTextSeekString ;
    private void InitNote() {       //进行数据填装
        MyDataBaseHelper dbHelper = new MyDataBaseHelper(this,"Note.db",null,1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();     //通过dbhelper获得可写文件
        Cursor cursor  = db.rawQuery("select * from Note",null);
        imageList.clear();
        IDList.clear();
        TADList.clear();        //清空两个list
        while(cursor.moveToNext()){
            int id=cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String image=cursor.getString(cursor.getColumnIndex("image"));
            imageList.add(image);
            IDList.add(id);
            TADList.add(title+"\n"+ date);      //对两个list填充数据
        }
    }

    public void RefreshTADList(){       //返回该界面时刷新的方法
        int size = TADList.size();
        //if(size>0){
        TADList.removeAll(TADList);
        IDList.removeAll(IDList);
        imageList.removeAll(imageList);
        simpleAdapter.notifyDataSetChanged();       //清空两个list中的值
        //}
        MyDataBaseHelper dbHelper = new MyDataBaseHelper(this,"Note.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();         //实例化SQLitedatabase
        Cursor cursor  = db.rawQuery("select * from Note",null);
        while(cursor.moveToNext()){         //对两个list重新赋予值
            int id=cursor.getInt(cursor.getColumnIndex("id"));

            String title = cursor.getString(cursor.getColumnIndex("title"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String image=cursor.getString(cursor.getColumnIndex("image"));
            imageList.add(image);
            IDList.add(id);
            TADList.add(title+"\n"+ date);      //将title和时间分开显示
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        RefreshTADList();       //调用刷新方法
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        InitNote();


        Button ButtonAdd;
        ButtonAdd = (Button)findViewById(R.id.ButtonAdd);
        ButtonAdd.setOnClickListener(new View.OnClickListener(){                  //点击“添加备忘录”按钮，跳转到Add活动
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, com.example.Diary.Add.class);
                startActivity(intent);
            }
        });

        simpleAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,TADList);       //配置适配器
        ListView ListView = (ListView)findViewById(R.id.ListView);
        ListView.setAdapter(simpleAdapter);                 //将两个list中的值通过ArrayList显示出来


        ButtonSeek = findViewById(R.id.ButtonSeek);
        EditTextSeek = findViewById(R.id.EditTextSeek);
        ButtonSeek.setOnClickListener(new View.OnClickListener(){       //点击查询按钮，根据EditTextSeek里的内容跳转到查询活动
            @Override
            public void onClick(View v){
                EditTextSeekString="";
                EditTextSeekString = String.valueOf(EditTextSeek.getText());
                //Log.d("title is ",EditTextSeekString);
                if(EditTextSeekString.length()==0){             //查询为空，给出提示信息
                    RefreshTADList();
                    Toast.makeText(MainActivity.this,"查询值不能为空",Toast.LENGTH_LONG).show();
                }
                else{           //否则通过intent给查询界面传入查询的title
                    Intent intent = new Intent(MainActivity.this, com.example.Diary.Research.class);
                    intent.setAction("ACTION_OPEN_DOCUMENT");
                    //intent.putExtra("tranTitle",EditTextSeekString);
                    intent.putExtra("tranTitletoRE",EditTextSeekString);
                    startActivity(intent);

                }
            }
        });

        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){      //配置ArrayList点击按钮，根据点击的ArrayList的某一行跳转到Edit界面
            @Override
            public void  onItemClick(AdapterView<?> parent, View view , int position , long id){
                int tran = IDList.get(position);        //点击不同的行，返回不同的id
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                Intent intent = new Intent(MainActivity.this, com.example.Diary.Edit.class);
                intent.setAction("ACTION_OPEN_DOCUMENT");
                intent.putExtra("tran",tran);

                startActivity(intent);      //通过intent传输
            }
        });


    }
    @Override
    public void onClick(View v){
        switch (v.getId()){

        }

    }

}
