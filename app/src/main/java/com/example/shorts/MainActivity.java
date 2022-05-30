package com.example.shorts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    myDBHelper myDBHelper;
    CalendarView calendarView;
    TextView title, selectDay, contents, uri;
    String formatDate, day;
    SQLiteDatabase sqlDB;

    ImageView imageView;
    Bitmap bitmap;
    String imageUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectDay = (TextView) findViewById(R.id.selectDay);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        title = (TextView) findViewById(R.id.title);
        contents = (TextView) findViewById(R.id.contents);
        uri = (TextView) findViewById(R.id.uri);
        myDBHelper = (myDBHelper) new myDBHelper(this);
        imageView = (ImageView) findViewById(R.id.imageView);

        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.");
        Date date = new Date(calendarView.getDate());
        formatDate = formatter.format(date);
        selectDay.setText(formatDate);
        day = formatDate;

        title.setVisibility(View.INVISIBLE);
        contents.setVisibility(View.INVISIBLE);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                title.setVisibility(View.VISIBLE);
                contents.setVisibility(View.VISIBLE);

                String yyyy = String.valueOf(year);
                String mm = String.valueOf(month+1);
                String dd = String.valueOf(dayOfMonth);

                day = yyyy +"."+ ( month+1 < 10 ? "0"+mm : mm ) +"."+ dd;
                selectDay.setText(day);
                int sqlDate = Integer.parseInt(day.replace(".",""));

                sqlDB = myDBHelper.getReadableDatabase();
                Cursor cursor;
                cursor = sqlDB.rawQuery("SELECT * FROM diaryTBL WHERE date = "+ sqlDate +";",null);
                String titleExtra = "";
                String contentsExtra = "작성된 일기가 없습니다.";
                String uriExtra = "";
                if( cursor.getCount() > 0 ) {
                    while (cursor.moveToNext()){
                        titleExtra = cursor.getString(1);
                        contentsExtra = cursor.getString(2);
                        uriExtra = cursor.getString(3);

                        imageUrl = uriExtra;
                    }
                }

                Thread Thread = new Thread() {
                    @Override
                    public void run(){
                        try{
                            URL url = new URL(imageUrl);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            //     HttpURLConnection의 인스턴스가 될 수 있으므로 캐스팅해서 사용한다
                            //     conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            //inputStream 값 가져오기
                            bitmap = BitmapFactory.decodeStream(is);
                            // Bitmap으로 반환
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                };

                Thread.start();

                try{
                    //join() 호출하여 별도의 작업 Thread가 종료될 때까지 메인 Thread가 기다림
                    Thread.join();
                    imageView.setImageBitmap(bitmap);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                title.setText(titleExtra);
                contents.setText(contentsExtra);
                uri.setText(uriExtra);
                cursor.close();
                sqlDB.close();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,1,0,"작성하기");
        menu.add(0,2,0,"수정하기");
        menu.add(0,3,0,"삭제하기");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), SubActivity.class);
        intent.putExtra("type",item.getItemId());
        intent.putExtra("date",day);
        int sqlDate = Integer.parseInt(day.replace(".",""));

        sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor;

        switch (item.getItemId()){
            case 1:
                cursor = sqlDB.rawQuery("SELECT * FROM diaryTBL WHERE date = "+ sqlDate +";",null);
                if( cursor.getCount() > 0 ) {
                    Toast.makeText(getApplicationContext(),"이미 작성된 일기가 있습니다.",Toast.LENGTH_SHORT).show();
                    return false;
                }else {
                    startActivity(intent);
                    return true;
                }

            case 2:
                String titleExtra = "";
                String contentsExtra = "";
                cursor = sqlDB.rawQuery("SELECT * FROM diaryTBL WHERE date = "+ sqlDate +";",null);
                if( cursor.getCount() > 0 ) {
                    while (cursor.moveToNext()){
                        titleExtra = cursor.getString(1);
                        contentsExtra = cursor.getString(2);
                    }
                }
                intent.putExtra("title",titleExtra);
                intent.putExtra("contents",contentsExtra);
                startActivity(intent);
                return true;

            case 3:
                sqlDB.execSQL("DELETE FROM diaryTBL WHERE date = "+ sqlDate +";");
                sqlDB.close();
                Toast.makeText(getApplicationContext(),"삭제됨",Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
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

}