package com.nanda.bookslibrary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nanda.bookslibrary.R;
import com.nanda.bookslibrary.model.BooksModel;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private Context context;
    private List<BooksModel> booksModelList;
    private BookPurchaseListener listener;

    public BookAdapter(Context context, BookPurchaseListener listener) {
        this.context = context;
        this.listener = listener;
        booksModelList = new ArrayList<>();
    }

    public interface BookPurchaseListener {
        void onPurchaseClicked(BooksModel model);
    }

    public void setBooksModelList(List<BooksModel> itemList) {
        if (itemList == null) {
            return;
        }
        booksModelList.clear();
        booksModelList.addAll(itemList);
        notifyItemRangeChanged(0, booksModelList.size());
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, viewGroup, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder bookViewHolder, int position) {
        BooksModel model = booksModelList.get(position);
        bookViewHolder.bindDataToviews(model);
    }

    @Override
    public int getItemCount() {
        return booksModelList.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvTitle;
        private TextView tvSubTitle;
        private Button btnPurchase;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubTitle = itemView.findViewById(R.id.tv_sub_title);
            btnPurchase = itemView.findViewById(R.id.btn_purchase);
            btnPurchase.setOnClickListener(this);
        }

        public void bindDataToviews(BooksModel model) {
            if (model.isPurchased()) {
                btnPurchase.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                btnPurchase.setText("Purchased");
            } else {
                btnPurchase.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
                btnPurchase.setText("Purchase");
            }
            tvTitle.setText(model != null && model.getVolumeInfo() != null ? model.getVolumeInfo().getTitle() : "NA");
            tvSubTitle.setText(String.format("Author - %s", model != null && model.getVolumeInfo() != null &&
                    model.getVolumeInfo().getAuthors() != null && model.getVolumeInfo().getAuthors().size() > 0 ?
                    model.getVolumeInfo().getAuthors().get(0) : "NA"));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position < 0)
                return;
            if (listener != null) {
                listener.onPurchaseClicked(booksModelList.get(position));
            }
        }
    }

}
