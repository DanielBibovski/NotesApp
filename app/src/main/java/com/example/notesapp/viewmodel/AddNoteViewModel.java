package com.example.notesapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NotesDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddNoteViewModel extends AndroidViewModel {

    private Disposable disposable;
    public final MutableLiveData<Boolean> success;

    public AddNoteViewModel(@NonNull Application application) {
        super(application);
        success = new MutableLiveData<>();
    }

    public void addNote(String noteText) {
        Note note = getNote(noteText);

        disposable = NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .insertAll(note)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    success.setValue(true);
                    Log.d("AddNoteViewModel", "success");
                }, error -> {
                    success.setValue(false);
                    Log.e("AddNoteViewModel", "error", error);
                });


//        NotesDatabase notesDatabase = NotesDatabase.getInstance(getApplication().getApplicationContext());
//        NoteDao noteDao = notesDatabase.noteDao();
//        Single<List<Long>> single = noteDao.insertAll(note);
//        Single<List<Long>> single1 = single.subscribeOn(Schedulers.newThread());
//        Single<List<Long>> single2 = single1.observeOn(AndroidSchedulers.mainThread());
//        disposable = single2.subscribe(result -> {
//                },
//                error -> {
//
//                });

    }

    private Note getNote(String textNote) {
        Note note = new Note();
        note.noteText = textNote;
        note.createdAt = System.currentTimeMillis();
        note.reminder = 0;
        return note;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
