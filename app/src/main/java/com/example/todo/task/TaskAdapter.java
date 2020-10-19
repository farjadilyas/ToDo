package com.example.todo.task;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.ThemeVar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ItemViewHolder>{

    private ArrayList<TaskItem> mItemList;
    private ClickListener listener;

    public TaskAdapter(ArrayList<TaskItem> itemList, ClickListener listener) {
        mItemList = itemList;
        this.listener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mCheckImageView, mDateImageView;
        public TextView mTaskTextView, mDateTextView, mTimeTextView;

        private WeakReference<ClickListener> listenerRef;

        public ItemViewHolder(@NonNull View itemView, final ClickListener listener ) {
            super(itemView);

            mCheckImageView = itemView.findViewById(R.id.icon);
            mDateImageView = itemView.findViewById(R.id.date_image);
            mTaskTextView = itemView.findViewById(R.id.text);
            mDateTextView = itemView.findViewById(R.id.date_text);
            mTimeTextView = itemView.findViewById(R.id.time_text);

            listenerRef = new WeakReference<>(listener);
            mCheckImageView.setOnClickListener(this);
            mDateImageView.setOnClickListener(this);
            mDateTextView.setOnClickListener(this);
            mTimeTextView.setOnClickListener(this);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.icon) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                mCheckImageView.setImageResource(R.drawable.ic_checked);

                if (ThemeVar.getData() == 2)
                    mCheckImageView.setColorFilter(Color.argb(255, 255, 255, 255));
                else
                    mCheckImageView.setColorFilter(Color.argb(0, 0, 0, 0));
                listenerRef.get().onPositionClicked(getAdapterPosition());
            }
            else if (v.getId() == R.id.date_image || v.getId() == R.id.date_text) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                listenerRef.get().onDateClicked(getAdapterPosition());
            }
            else if (v.getId() == R.id.time_text) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                listenerRef.get().onTimeClicked(getAdapterPosition());
            }
            else {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                listenerRef.get().onCardClicked(getAdapterPosition());
            }
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_card, parent, false);

        return new ItemViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        TaskItem currentItem = mItemList.get(position);

        holder.mCheckImageView.setImageResource(currentItem.getCheckImageResource());
        holder.mCheckImageView.setImageResource(R.drawable.ic_unchecked);
        //holder.mCheckImageView.setColorFilter(Color.argb(255, 0, 0, 0));
        holder.mDateImageView.setImageResource(currentItem.getCalendarImageResource());
        holder.mTaskTextView.setText(currentItem.getTaskText());
        holder.mDateTextView.setText(currentItem.getDateText());
        holder.mTimeTextView.setText(((currentItem.getTimeText().equals("00:00"))?"":" " + currentItem.getTimeText()));
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}
