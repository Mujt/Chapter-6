package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.util.Date;


public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;
    private Button pri2;
    private Button pri1;
    private Button pri0;

    private int pri = 0;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        pri2 = findViewById(R.id.pri2);
        pri1 = findViewById(R.id.pri1);
        pri0 = findViewById(R.id.pri0);

        pri2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pri2.setText("OK");
                pri0.setText("");
                pri1.setText("");
                pri = 2;
            }
        });

        pri1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pri1.setText("OK");
                pri2.setText("");
                pri0.setText("");
                pri = 1;
            }
        });

        pri0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pri0.setText("OK");
                pri1.setText("");
                pri2.setText("");
                pri = 0;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功
        dbHelper = new TodoDbHelper(getBaseContext());
        database = null;
        database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TodoContract.FeedEntry.COLUMN_NAME_CONTENT,content);
        Time t = new Time();
        t.setToNow();
        values.put(TodoContract.FeedEntry.COLUMN_NAME_DATE,t.toMillis(true));
        System.out.println(t.year+"-"+(t.month+1)+"-"+t.monthDay+"-"+t.hour+"-"+t.minute+"-"+t.second);
       // t.year+"年"+t.month+1+"月"+t.monthDay+"日 "+t.hour+":"+t.minute+":"+t.second
        values.put(TodoContract.FeedEntry.COLUMN_NAME_STATE,0);
        /*ToDo: priority*/
        values.put(TodoContract.FeedEntry.COLUMN_NAME_PRI,pri);
        long newRowId = database.insert(TodoContract.FeedEntry.TABLE_NAME,null,values);
        dbHelper.close();
        return true;
    }

   /* class InsertTask extends AsyncTask<String,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            SQLiteDatabase database =
            ContentValues values = new ContentValues();

            return null;
        }
    }*/
}
