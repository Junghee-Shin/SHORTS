package com.example.shorts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SubActivity extends AppCompatActivity {
    myDBHelper myDBHelper;
    Button btnWrite, btnImage;
    TextView selectDay;
    String date;
    EditText title, contents;
    SQLiteDatabase sqlDB;
    File file;

    ImageView imageView;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        selectDay = findViewById(R.id.selectDay);
        title = (EditText) findViewById(R.id.title);
        contents = (EditText) findViewById(R.id.contents);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnImage = (Button) findViewById(R.id.btnImage);
        myDBHelper = (myDBHelper) new SubActivity.myDBHelper(this);
        imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        selectDay.setText(intent.getExtras().getString("date"));
        title.setText(intent.getExtras().getString("title"));
        contents.setText(intent.getExtras().getString("contents"));
        date = (intent.getExtras().getString("date")).replace(".","");

        if( intent.getExtras().getInt("type") == 1 ) {
            btnWrite.setText("작성");
        }else {
            btnWrite.setText("수정");
        }

        myDBHelper = new myDBHelper(this);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int sqlDate = Integer.parseInt(date);
                String sqlTitle = title.getText().toString();
                String sqlContents = contents.getText().toString();
                String sqlFilePath = date+"_"+file.getName();
                sqlDB = myDBHelper.getWritableDatabase();

                if( intent.getExtras().getInt("type") == 1 ) {
                    try {
                        sqlDB.execSQL("INSERT INTO diaryTBL VALUES ("+ sqlDate +",'"+ sqlTitle +"','"+ sqlContents +"','"+ sqlFilePath +"');");
                        sqlDB.close();
                        Toast.makeText(getApplicationContext(),"입력되었습니다.",Toast.LENGTH_SHORT).show();
                        addPicture();
                    }catch (Exception e) {}

                }else {
                    try {
                        sqlDB.execSQL("UPDATE diaryTBL SET title = '"+ sqlTitle +"', contents = '"+ sqlContents +"', filepath = '"+ sqlFilePath +"' WHERE date ="+ sqlDate +";");
                        sqlDB.close();
                        Toast.makeText(getApplicationContext(),"수정되었습니다.",Toast.LENGTH_SHORT).show();
                        addPicture();
                    }catch (Exception e) {}
                }
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission()){
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,10);
                    }else{
                        requestPermission();
                    }

                }else{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,10);
                }
            }
        });
    }

    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(SubActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(SubActivity.this, "Please Give Permission to Upload File", Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(SubActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission(){
        int result= ContextCompat.checkSelfPermission(SubActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context) {
            super(context,"diaryDB.db",null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE diaryTBL (date int, title text, contents BLOB, filepath text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS diaryTBL");
            onCreate(db);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            Context context = SubActivity.this;
            path = RealPathUtil.getRealPath(context, uri);
            file = new File(path);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void addPicture() {
        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://192.168.10.66/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image",date+"_"+file.getName(),requestFile);

        ApiService apiService = retrofit.create(ApiService.class);
        Call<AddPictureRes> call = apiService.addPicture(body);

        call.enqueue(new Callback<AddPictureRes>() {
            @Override
            public void onResponse(Call<AddPictureRes> call, Response<AddPictureRes> response) {
                if(response.isSuccessful()){
                    if(response.body().getStatus().toString().equals("200")){
//                        Toast.makeText(getApplicationContext(),"Customer Added", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getApplicationContext(),"not Added", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AddPictureRes> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}