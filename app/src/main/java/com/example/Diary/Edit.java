package com.example.Diary;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class Edit extends AppCompatActivity implements OnClickListener{
    Button ButtonDelete,ButtonSave,ButtonAddinsert_img,ButtonCancel;
    EditText EditTextContent,EditTextTitle,EditTextEditAuthor;
    ImageView EditTextEditimag;
    private Uri imageUri;//存储照片的uri
    int tran = 0;
    String Author="";
    public static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO=2;
    MyDataBaseHelper dbHelper = new MyDataBaseHelper(this,"Note.db",null,1);
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getRealFilePath(final Context context, final Uri uri ) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
        private void InitNote() {       //进行数据填装
        MyDataBaseHelper dbHelper = new MyDataBaseHelper(this,"Note.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.query("Note",new String[]{"id","title","content","image"},"id=?",new String[]{tran+""},null,null,null);
        if(cursor.moveToNext()) {       //根据mainactivity传来的id值选择数据库中对应的行，将值返回
            do {
                String Title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String image = cursor.getString(cursor.getColumnIndex("image"));
                System.out.println("将要用于初始化的imageUri:"+image);
                Uri imageU=Uri.parse(image);
                System.out.println("用于初始化的URI："+imageU);
                EditTextEditimag.setImageURI(imageU);
          //      Bitmap bitmap = stringToBitmap(image);
           //     System.out.println("bitmap:"+bitmap);
           //     EditTextEditimag.setImageBitmap(bitmap);
                EditTextContent.setText(content);
                EditTextTitle.setText(Title);
            } while (cursor.moveToNext());
        }

        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        String name = pref.getString("author","");      //通过sharedpreferences传递作者信息
        //Log.d("MainActivity","name is " + name);
        EditTextEditAuthor.setText(name);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        EditTextContent = (EditText)findViewById(R.id.EditTextEditContent);
        EditTextTitle = (EditText)findViewById(R.id.EditTextEditTitle) ;
        ButtonCancel = (Button)findViewById(R.id.ButtonCancel);
        ButtonAddinsert_img=(Button)findViewById(R.id.ButtonAddinsert_img);
        ButtonSave = (Button)findViewById(R.id.ButtonSave);
        ButtonDelete = (Button)findViewById(R.id.ButtonDelete);
        EditTextEditAuthor = findViewById(R.id.EditTextEditAuthor);
        EditTextEditimag =findViewById(R.id.EditTextEditimag);


        ButtonCancel.setOnClickListener(this);
        ButtonSave.setOnClickListener(this);
        ButtonDelete.setOnClickListener(this);
        ButtonAddinsert_img.setOnClickListener(this);

        Intent intent = getIntent();
        tran = intent.getIntExtra("tran",-1);       //取出mainactivity传来的id值

        InitNote();


    }
    @Override
    protected  void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                System.out.println("TAKE_PHOTO");

                if (resultCode == RESULT_OK) {
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        EditTextEditimag.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                System.out.println("CHOOSE_PHOTO");
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用这个方法处理图片
                        System.out.println("4.4及以上系统");
                        String imagePath = null;
                        Uri uri = data.getData();
                        this.imageUri=data.getData();
                        ContentResolver cr = Edit.this.getContentResolver();
                        System.out.print("uri:");
                        System.out.println(uri);
                        Bitmap bitmap = null;
                        Bundle extras = null;
                        try {
                            //将对象存入Bitmap中
                            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));


                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        int imgWidth = bitmap.getWidth();
                        int imgHeight = bitmap.getHeight();
                        double partion = imgWidth * 1.0 / imgHeight;
                        double sqrtLength = Math.sqrt(partion * partion + 1);
                        //新的缩略图大小
                        double newImgW = 480 * (partion / sqrtLength);
                        double newImgH = 480 * (1 / sqrtLength);
                        float scaleW = (float) (newImgW / imgWidth);
                        float scaleH = (float) (newImgH / imgHeight);

                        Matrix mx = new Matrix();
                        //对原图片进行缩放
                        mx.postScale(scaleW, scaleH);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
                        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
                        SpannableString spannableString = new SpannableString("test");
                        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
                        //光标移到下一行
                        this.imageUri=null;            //对于访问相册里的图片，需要ACTION_OPEN_DOCUMENT权限。
                        EditTextEditimag.setImageBitmap(bitmap);
                    }
                    else {
                        //4.4以下系统使用这个方法处理图片
                        System.out.println("4.4及以下系统");

                        Uri uri = data.getData();
                        this.imageUri=data.getData();
                        String imagePath = getImagePath(uri, null);
                        if(imagePath!=null){
                            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
                            EditTextEditimag.setImageBitmap(bitmap);
                        }else{
                            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
                        }
                        this.imageUri=null;
                    }
                }
                break;
            default:
                break;
        }
    }
    private byte[] getPicture(Drawable drawable) {
        //将drawable转换成可以用来存储的byte[]类型
        if(drawable == null) {
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
    private String getImagePath(Uri uri, String selection) {
        String path =null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.ButtonAddinsert_img:
                View popView = View.inflate(this,R.layout.img_insert,null);
                Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
                Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
                Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
                //获取屏幕宽高
                int weight = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels*1/3;

                final PopupWindow popupWindow = new PopupWindow(popView,weight,height);

                popupWindow.setFocusable(true);
                //点击外部popueWindow消失
                popupWindow.setOutsideTouchable(true);

                bt_album.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*"); // 设定类型为image
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT); // 设置action
                        startActivityForResult(intent, CHOOSE_PHOTO); // 从本地相册中选择图片
                        popupWindow.dismiss();

                    }
                });
                bt_camera.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            //   创建File对象，用于存储拍照后的图片  takeCamera;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
                                // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
                                if (ContextCompat.checkSelfPermission(Edit.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(Edit.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                                    } else {

                                        // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                                        //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                                        ActivityCompat.requestPermissions(Edit.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                123);

                                    }
                                }

                            }
                            if (ContextCompat.checkSelfPermission(Edit.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                //请求权限
                                ActivityCompat.requestPermissions(Edit.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
                            }
                            System.out.println("请求权限成功");
                            File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                            try{
                                if(outputImage.exists()){
                                    outputImage.delete();
                                }
                                outputImage.createNewFile();
                            } catch (IOException e) {
                                //捕获异常
                                e.printStackTrace();
                            }
                            if(Build.VERSION.SDK_INT>=24){
                                //设备的系统版本大于等于Android7
                                imageUri= FileProvider.getUriForFile(Edit.this,"com.example.Diary.fileprovider",outputImage);
                            }else{
                                imageUri=Uri.fromFile(outputImage);
                            }
                            System.out.println("imageUri"+imageUri);
                            //启动照相机
                            Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

                            startActivityForResult(intent,TAKE_PHOTO);


                            popupWindow.dismiss();
                        }
                    });
                bt_cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        popupWindow.dismiss();

                    }
                });
                //popupWindow消失屏幕变为不透明
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.alpha = 1.0f;
                        getWindow().setAttributes(lp);
                    }
                });
                //popupWindow出现屏幕变为半透明
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);
                popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);
                break;

            case R.id.ButtonDelete:     //将对应的id行删除

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("Note","id=?",new String[]{tran+""});
                Edit.this.setResult(RESULT_OK,getIntent());
                Edit.this.finish();
                break;
            case R.id.ButtonSave:       //保存该界面的数据
                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                Date date = new Date();
                ContentValues values = new ContentValues();
                String Title = String.valueOf(EditTextTitle.getText());
                String Content = String.valueOf(EditTextContent.getText());

                System.out.println("保存时的imageUri："+imageUri);

                if(Title.length()==0){
                    Toast.makeText(this, "请输入一个标题", Toast.LENGTH_LONG).show();
                }else {
                    values.put("title", Title);
                    values.put("content", Content);
                    values.put("image", String.valueOf(imageUri));//将图片转换成bytes保存到String字段。
                    db1.update("Note", values, "id=?", new String[]{tran + ""});        //对数据进行更新
                    Edit.this.setResult(RESULT_OK, getIntent());
                    Edit.this.finish();
                }


                Author = String.valueOf(EditTextEditAuthor.getText());
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("author",Author);      //写入作者信息
                editor.apply();

                break;


            case R.id.ButtonCancel:
                Edit.this.setResult(RESULT_OK,getIntent());
                Edit.this.finish();
                break;

        }

    }

}
