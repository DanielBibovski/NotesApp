package com.example.notesapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NotesDatabase;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NotesViewModel extends AndroidViewModel {

    private final CompositeDisposable compositeDisposable;
    public final MutableLiveData<List<Note>> notes;
    public final MutableLiveData<Boolean> error;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        compositeDisposable = new CompositeDisposable();
        notes = new MutableLiveData<>();
        error = new MutableLiveData<>();
    }

    public void fetchNotes() {
        compositeDisposable.add(NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .getAllNotes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, this::onError));
    }

    public void search(String query) {
        query = "%" + query + "%";
        compositeDisposable.add(NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .search(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, this::onError));
    }

    private void onSuccess(List<Note> listOfNotes) {
        error.setValue(false);
        notes.setValue(listOfNotes);
    }


    private void onError(Throwable throwable) {
        Log.e("NotesViewModel", "", throwable);
        error.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

}
