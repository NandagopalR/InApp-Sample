package com.nanda.bookslibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nanda.bookslibrary.R;
import com.nanda.bookslibrary.adapter.BookAdapter;
import com.nanda.bookslibrary.base.BaseActivity;
import com.nanda.bookslibrary.model.BooksModel;
import com.nanda.bookslibrary.model.GooglsBooksResponse;
import com.nanda.bookslibrary.utils.IabHelper;
import com.nanda.bookslibrary.utils.IabResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HomeActivity extends BaseActivity implements BookAdapter.BookPurchaseListener {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private String googleBooksJson;
    private Gson gson;
    private List<BooksModel> booksModelList;
    private IabHelper mHelper;
    private String base64EncodedPublicKey;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        googleBooksJson = loadJSONFromAsset();
        gson = new GsonBuilder().create();

        base64EncodedPublicKey = getString(R.string.public_key);

        adapter = new BookAdapter(this, this);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(adapter);

        if (!TextUtils.isEmpty(googleBooksJson)) {
            GooglsBooksResponse response = gson.fromJson(googleBooksJson, GooglsBooksResponse.class);
            if (response != null && response.getBooksModelList() != null) {
                booksModelList = response.getBooksModelList();
                if (booksModelList != null && booksModelList.size() > 0) {
                    adapter.setBooksModelList(booksModelList);
                    initiateInApp();
                }
            }
        }

    }

    private void initiateInApp() {
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    showToast("Problem setting up In-app Billing: " + result);
                } else {
//                    loadProducts();
                }
            }
        });
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("books.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onPurchaseClicked(BooksModel model) {
        Toast.makeText(this, model.getVolumeInfo().getTitle(), Toast.LENGTH_SHORT).show();
    }
}
