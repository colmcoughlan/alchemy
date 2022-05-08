package com.colmcoughlan.colm.alchemy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        RecyclerView recyclerView = findViewById(R.id.categories_recycler);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        List<String> categories = Arrays.asList(getResources().getStringArray(R.array.categories_array));
        recyclerView.setAdapter(new CategoryAdapter(this, recyclerView, categories));
    }

    @Override
    public void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.my_donations:
                Intent donations = new Intent(this, MyDonations.class);
                startActivity(donations);
                break;
            case R.id.about:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // a class of the view
    private static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView textView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.card_view);
            this.textView = cardView.findViewById(R.id.card_view_text);
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

        private final List<Integer> colors;
        private final List<String> categories;
        private final Context mContext;
        private final RecyclerView mRecyclerView;

        public CategoryAdapter(Context context, RecyclerView recyclerView, List<String> categories) {
            this.mContext = context;
            this.mRecyclerView = recyclerView;
            this.categories = categories;
            this.colors = Arrays.asList(
                    Color.parseColor("#f9dc5c"),
                    Color.parseColor("#3185fc"),
                    Color.parseColor("#06A77D"),
                    Color.parseColor("#C9DDFF"),
                    Color.parseColor("#2CF6B3"),
                    Color.parseColor("#D282A6"),
                    Color.parseColor("#258EA6")
                    );
        }

        @NonNull
        @Override
        // how to get a view of the right type
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_card_item, parent, false);
            view.setOnClickListener(v -> {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                StaticState.setCurrentCategory(categories.get(itemPosition));
                Intent intent = new Intent(mContext, CharityActivity.class);
                startActivity(intent);
            });
            return new CategoryViewHolder(view);
        }

        @Override
        // how to populate it given its position
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            holder.textView.setText(categories.get(position));
            holder.cardView.setCardBackgroundColor(chooseColor(position));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        private int chooseColor(int position) {
            return colors.get(position % colors.size());
        }
    }
}
