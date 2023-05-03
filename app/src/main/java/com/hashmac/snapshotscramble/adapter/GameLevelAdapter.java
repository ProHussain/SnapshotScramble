package com.hashmac.snapshotscramble.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hashmac.snapshotscramble.databinding.ItemGameLevelBinding;
import com.hashmac.snapshotscramble.models.GameLevel;
import com.hashmac.snapshotscramble.ui.PlayGameActivity;

import java.util.ArrayList;
import java.util.List;

public class GameLevelAdapter extends RecyclerView.Adapter<GameLevelAdapter.ViewHolder> {
    List<GameLevel> gameLevels = new ArrayList<>();
    int level;

    public GameLevelAdapter(int level) {
        gameLevels.clear();
        gameLevels = GameLevel.getGameLevels();
        this.level = level;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGameLevelBinding binding = ItemGameLevelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        GameLevel level = gameLevels.get(position);
        viewHolder.onBind(level);
    }

    @Override
    public int getItemCount() {
        return gameLevels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemGameLevelBinding binding;
        public ViewHolder(@NonNull ItemGameLevelBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(GameLevel gameLevel) {
            binding.tvLevelName.setText(gameLevel.getName());
            String target = gameLevel.getType() == GameLevel.TYPE_TIME ? "Time: " + gameLevel.getTarget() + "s" : "Moves: " + gameLevel.getTarget();
            binding.tvLevelTarget.setText(target);
            if (gameLevel.getNumber() <= level) {
                binding.flLevelStatus.setVisibility(View.GONE);
            } else {
                binding.flLevelStatus.setVisibility(View.VISIBLE);
            }
            binding.getRoot().setOnClickListener(view -> {
                if (gameLevel.getNumber() <= level) {
                    Intent intent = new Intent(binding.getRoot().getContext(), PlayGameActivity.class);
                    intent.putExtra("Game", "level");
                    intent.putExtra("Level", gameLevel.getNumber());
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
        }
    }
}
