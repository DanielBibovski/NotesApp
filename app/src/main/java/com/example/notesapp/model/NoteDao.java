package com.example.notesapp.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface NoteDao {

    @Insert
    Single<List<Long>> insertAll(Note... notes);

    @Query("SELECT * FROM notes")
    Single<List<Note>> getAllNotes();

    @Update
    Completable update(Note note);

    @Delete
    Completable delete(Note note);

    @Query("SELECT * FROM notes WHERE id= :id")
    Single<Note> getNote(int id);

    @Query("SELECT * FROM notes WHERE note_text LIKE :query")
    Single<List<Note>> search(String query);

}
