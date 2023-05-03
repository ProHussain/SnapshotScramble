package com.hashmac.snapshotscramble.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hashmac.snapshotscramble.databinding.ItemLeaderBoardBinding;
import com.hashmac.snapshotscramble.models.LeaderBoard;

import java.util.List;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder> {
    List<LeaderBoard> leaderBoardList;

    public LeaderBoardAdapter(List<LeaderBoard> leaderBoardList) {
        this.leaderBoardList = leaderBoardList;
    }

    @NonNull
    @Override
    public LeaderBoardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderBoardBinding binding = ItemLeaderBoardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderBoardAdapter.ViewHolder holder, int position) {
        LeaderBoard leaderBoard = leaderBoardList.get(position);
        holder.onBind(leaderBoard, position);
    }

    @Override
    public int getItemCount() {
        return leaderBoardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderBoardBinding binding;
        public ViewHolder(@NonNull ItemLeaderBoardBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(LeaderBoard leaderBoard, int position) {
            binding.tvPlayerName.setText(leaderBoard.getName());
            binding.tvLevel.setText(String.valueOf(leaderBoard.getScore()));
            if (position == 0) {
                binding.imgCrown.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.imgCrown.setVisibility(View.INVISIBLE);
            }
        }
    }
}
