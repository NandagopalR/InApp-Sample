package com.nanda.bookslibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nanda.bookslibrary.R;
import com.nanda.bookslibrary.adapter.BookAdapter;
import com.nanda.bookslibrary.base.BaseActivity;
import com.nanda.bookslibrary.model.BooksModel;
import com.nanda.bookslibrary.model.GooglsBooksResponse;
import com.nanda.bookslibrary.model.PurchaseModel;
import com.nanda.bookslibrary.utils.IabHelper;
import com.nanda.bookslibrary.utils.IabResult;
import com.nanda.bookslibrary.utils.Inventory;
import com.nanda.bookslibrary.utils.Purchase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity implements BookAdapter.BookPurchaseListener {

    private static final int RC_REQUEST = 1243;
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private String googleBooksJson;
    private Gson gson;
    private List<BooksModel> booksModelList;
    private List<String> bookListIds = new ArrayList<>();
    private IabHelper mHelper;
    private String base64EncodedPublicKey;
    private List<PurchaseModel> purchasedItems;

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
                    bookListIds.clear();
                    bookListIds.addAll(getBookListIds(booksModelList));
                    initiateInApp();
                }
            }
        }

    }

    private void updateViews() {
        if (purchasedItems != null && purchasedItems.size() > 0) {

            for (int i = 0, purchasedItemsSize = purchasedItems.size(); i < purchasedItemsSize; i++) {
                PurchaseModel model = purchasedItems.get(i);
                if (model != null) {
                    for (int i1 = 0, booksModelListSize = booksModelList.size(); i1 < booksModelListSize; i1++) {
                        BooksModel item = booksModelList.get(i1);
                        String bookId = getPackageName() + "." + item.getId();
                        if (item != null && bookId.equals(model.getId())) {
                            item.setPurchased(true);
                        }
                    }
                }
            }
        }
        if (booksModelList != null && booksModelList.size() > 0) {
            adapter.setBooksModelList(booksModelList);
        }
    }

    private List<String> getBookListIds(List<BooksModel> booksModelList) {
        if (booksModelList == null) {
            return null;
        }
        List<String> bookIdsList = new ArrayList<>(booksModelList.size());
        for (int i = 0, booksModelListSize = booksModelList.size(); i < booksModelListSize; i++) {
            BooksModel model = booksModelList.get(i);
            if (model != null) {
                String id = String.format(Locale.getDefault(), "%s.%s", getPackageName(), model.getId());
                bookIdsList.add(id);
            }
        }
        return bookIdsList;
    }

    private void initiateInApp() {
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    showToast("Problem setting up In-app Billing: " + result);
                    updateViews();
                } else {
                    loadProducts();
                }
            }
        });
    }

    private void loadProducts() {
        try {
            mHelper.queryInventoryAsync(true, bookListIds, null, new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    purchasedItems = new ArrayList<>();
                    PurchaseModel item;

                    for (BooksModel model : booksModelList) {
                        String s = String.format(Locale.getDefault(), "%s.%s", getPackageName(), model.getId());

                        item = new PurchaseModel();
                        if (inv != null && inv.getSkuDetails(s) != null) {
                            item.setTitle(inv.getSkuDetails(s).getTitle());
                            item.setDescription(inv.getSkuDetails(s).getDescription());
                            item.setPrice(inv.getSkuDetails(s).getPrice());
                            item.setId(inv.getSkuDetails(s).getSku());
                            item.setPurchased(true);
                            purchasedItems.add(item);
                            Log.e("Purchased Desc - ", " - " + item.getDescription());
                            Log.e("Purchased Id - ", " - " + item.getId());
                            Log.e("Purchased Price - ", " - " + item.getPrice());
                            Log.e("Purchased Title - ", " - " + item.getTitle());
                        }
                    }

                    if (purchasedItems.isEmpty()) {
                        // Do stuff for empty list
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0, itemsSize = purchasedItems.size(); i < itemsSize; i++) {
                            PurchaseModel model = purchasedItems.get(i);
                            builder.append(model.getTitle());
                            builder.append("");
                        }
                        Toast.makeText(HomeActivity.this, builder, Toast.LENGTH_SHORT).show();
                    }
                    updateViews();
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
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

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("", "Purchase finished: " + result + ", purchase: " + purchase);

            Toast.makeText(HomeActivity.this, "Purchase Finish Callback.", Toast.LENGTH_SHORT).show();
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                showToast("Error purchasing: " + result);
                return;
            }
            loadProducts();
            Toast.makeText(HomeActivity.this, "Purchase successful." + purchase, Toast.LENGTH_SHORT).show();

            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.e("", "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                Log.e("", "Consumption successful. Provisioning." + purchase.getSku());
            } else {
                Log.e("", "Error while consuming: " + result);
            }
            Log.e("", "End consumption flow.");
        }
    };

    @Override
    public void onPurchaseClicked(BooksModel model) {
        if (!model.isPurchased()) {
            try {
                if (mHelper != null) mHelper.flagEndAsync();
                String id = String.format(Locale.getDefault(), "%s.%s", getPackageName(), model.getId());
                mHelper.launchPurchaseFlow(this, id, RC_REQUEST, mPurchaseFinishedListener, "");
            } catch (IabHelper.IabAsyncInProgressException e) {
                Log.e("", "onProductClick: " + e.getMessage());
                showToast(e.getMessage());
                try {
                    mHelper.dispose();
                } catch (IabHelper.IabAsyncInProgressException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "You're already purchased", Toast.LENGTH_SHORT).show();
        }
    }
}
