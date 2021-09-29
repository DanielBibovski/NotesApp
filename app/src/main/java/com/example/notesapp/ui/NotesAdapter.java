package com.example.notesapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.databinding.ItemNoteBinding;
import com.example.notesapp.model.Note;
import com.example.notesapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> notes;

    public NotesAdapter() {
        this.notes = new ArrayList<>();
    }

    public void addAll(List<Note> noteList) {
        notes.clear();
        notes.addAll(noteList);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View viewHolder = inflater.inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        private final ItemNoteBinding binding;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNoteBinding.bind(itemView);
        }

        public void bind(Note note) {
            binding.note.setText(note.noteText);
            binding.createdDate.setText(DateTimeUtil.format(note.createdAt));
            if (note.reminder == 0) {
                binding.reminder.setVisibility(View.GONE);
            } else {
                binding.reminder.setVisibility(View.VISIBLE);
                binding.reminder.setText(DateTimeUtil.format(note.reminder));
            }

            binding.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotesFragmentDirections.ActionNoteDetailsFragment direction = NotesFragmentDirections.actionNoteDetailsFragment();
                    direction.setNoteId(note.id);
                    Navigation.findNavController(v).navigate(direction);
                }
            });
        }
    }
}
