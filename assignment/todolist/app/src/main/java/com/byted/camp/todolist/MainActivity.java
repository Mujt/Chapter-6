package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.sql.Date;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        /*dbHelper = new TodoDbHelper(getBaseContext());
        database = dbHelper.getReadableDatabase();*/
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        dbHelper = new TodoDbHelper(getBaseContext());
        database = dbHelper.getReadableDatabase();

        int pos1 = 0;
        int pos2 = 0;

        if (database == null) {
            return Collections.emptyList();
        }
        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(TodoContract.FeedEntry.TABLE_NAME,
                    new String[]{TodoContract.FeedEntry.COLUMN_NAME_CONTENT, TodoContract.FeedEntry.COLUMN_NAME_DATE,
                            TodoContract.FeedEntry.COLUMN_NAME_STATE, TodoContract.FeedEntry.COLUMN_NAME_PRI},
                    null,null,
                    null,null,
                    TodoContract.FeedEntry.COLUMN_NAME_DATE + " DESC");

            while (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_CONTENT));
                long dateMs = cursor.getLong(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_DATE));
                int intState = cursor.getInt(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_STATE));
                int pri = cursor.getInt(cursor.getColumnIndex(TodoContract.FeedEntry.COLUMN_NAME_PRI));

                Note note = new Note(10);
                note.setContent(content);
                System.out.println("dateMs:"+dateMs);
                note.setDate(new Date(dateMs));
                note.setState(State.from(intState));
                note.setPri(pri);
                if (pri == 2) {
                    result.add(pos1,note);
                    pos1++;
                    pos2++;
                } else if (pri == 1) {
                    result.add(pos2,note);
                    pos2++;
                } else if (pri == 0) {
                     ((LinkedList<Note>) result).addLast(note);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbHelper.close();
        }
        return result;
    }



    private void deleteNote(Note note) {
        dbHelper = new TodoDbHelper(this);
        //database = null;
        database = dbHelper.getReadableDatabase();
        String selection = TodoContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = { "" + note.getDate().getTime() };
        int dele =  database.delete(TodoContract.FeedEntry.TABLE_NAME,selection,selectionArgs);
        dbHelper.close();
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.refresh(loadNotesFromDatabase());
        // TODO 删除数据
    }

    private void updateNode(Note note) {
        // 更新数据
        dbHelper = new TodoDbHelper(this);
        database = null;
        database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TodoContract.FeedEntry.COLUMN_NAME_STATE,1);
        String selection = TodoContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = { "" + note.getDate().getTime() };
        long count = database.update(TodoContract.FeedEntry.TABLE_NAME,values,selection,selectionArgs);
        dbHelper.close();
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.refresh(loadNotesFromDatabase());
        //return true;
    }

}
