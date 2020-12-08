package com.example.hw9;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.functionalities.BookmarkCache;
import com.example.hw9.functionalities.DetailedArticleActivity;
import com.example.hw9.functionalities.SwipeToDeleteCallback;
import com.example.hw9.functionalities.search.AutoSuggestAdapter;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ItemViewHolder;
import com.example.hw9.model.Stock;
import com.example.hw9.model.StockSection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements StockSection.ClickListener {

    private static final int TRIGGER_AUTO_COMPLETE = 300;
    private static final long AUTO_COMPLETE_DELAY = 500;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    AppCompatActivity activity;
    BookmarkCache bookmarkCache;
    Integer bookmarkCount;
    RequestQueue queue;
    private static SectionedRecyclerViewAdapter sectionedAdapter;
    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    static String[] monthName = {"January", "February",
            "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};

    static String url = "https://cryptic-waters-26273.herokuapp.com";
//    static String url = "http://10.0.2.2:8081";

    private static RecyclerView mList;
    private static LinearLayout progressLayout;
    private static List<Stock> stocks;
    final static Map<StockSection, List<Stock>> itemMap = new HashMap<>();
    final static Map<StockSection, String> sectionTags = new HashMap<>();

    private static LinearLayoutManager linearLayoutManager;
    private static View view;
    private static DividerItemDecoration dividerItemDecoration;
    private static Context context;
    private static List<String> favorites;
    private static List<Stock> portfolio;
    private static HashMap<String, Stock> favoriteObj;
    private static HashMap<String, Stock> portfolioObj;
    private static int counter = 0;
    private static boolean loading = true;
    private static LinearLayout homeView;

    public BookmarkCache getBookmarkCache() { return bookmarkCache; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        init();
    }

    public void init() {
        if(bookmarkCache!= null) return;
        bookmarkCache = BookmarkCache.getInstance(activity);
        queue = Volley.newRequestQueue(getApplicationContext());

        View view = findViewById(R.id.mainActivityRoot);
        TextView tiingoCreds = findViewById(R.id.tingo_creds);
        tiingoCreds.setVisibility(View.GONE);
        homeView = view.findViewById(R.id.home_view);
        progressLayout = view.findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.VISIBLE);
        portfolioObj = new HashMap<String, Stock>();
        favoriteObj = new HashMap<String, Stock>();
        stocks = new ArrayList<Stock>();
        getData(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_action_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

//        searchView.setBackgroundColor(getResources().getColor(R.color.sectionTabColor));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchAutoComplete.setTextColor(R.color.cleanBlack);
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setColorFilter(getResources().getColor(R.color.searchIcons), PorterDuff.Mode.SRC_ATOP);

//        searchAutoComplete.setBackgroundColor(getResources().getColor(R.color.sectionTabColor));
        ImageView searchHintIcon = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchHintIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        ComponentName component = new ComponentName(this, DetailedArticleActivity.class);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(component);
        searchView.setSearchableInfo(searchableInfo);

//         Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            String queryString=(String)adapterView.getItemAtPosition(itemIndex);
            searchAutoComplete.setText(queryString);
        });

        setupAutoSuggest(searchView);
        return true;
    }

    private void setupAutoSuggest(SearchView searchView) {

        final AppCompatAutoCompleteTextView autoCompleteTextView =
                searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        autoCompleteTextView.setTextColor(getResources().getColor(R.color.cleanBlack));
        autoCompleteTextView.setDropDownBackgroundResource(R.color.sectionTabColor);
        autoCompleteTextView.setDropDownHeight(1100);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(3);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        handler = new Handler(msg -> {
            if (msg.what == TRIGGER_AUTO_COMPLETE) {
                if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                    fetchSuggestions(autoCompleteTextView.getText().toString());
                }
            }
            return false;
        });
    }

    private void fetchSuggestions(String toString) {
        if(toString.length()<3) {
            autoSuggestAdapter.clearData();
            autoSuggestAdapter.notifyDataSetChanged();
            return;
        }
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET,
                url+"/apis/stocks/autocomplete?query="+toString, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                List<String> stringList = new ArrayList<>();
                Log.i("Autosuggest data",response.toString());
                try {

                    for(int i=0; i<response.length(); i++){
                       JSONObject obj = new JSONObject(response.getString(i));
                       String ticker = obj.getString("ticker");
                       String name = obj.getString("name");
                       Log.i("Suggestion -", ticker);
                       stringList.add(ticker+" - "+name);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, error -> {
            Log.e("Error getting search data", error.toString() + error.getMessage());
        }){};

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(objectRequest);
    }

    private void getData(View view){
        String tickers = "";
        counter = 0;
        loading = true;
        portfolioObj.clear();
        favoriteObj.clear();

        favorites = new ArrayList<String>(bookmarkCache.getFavorites());
        portfolio = new ArrayList<Stock>(bookmarkCache.getPortfolio());

        if(favorites.isEmpty()) getPortfolioData(view);
        for(String fav:favorites) {
            tickers = tickers+fav+",";
            favoriteObj.put(fav, new Stock(fav.toUpperCase(), "", "0", "0"));
        }

        if(tickers.length()>0) tickers = tickers.substring(0, tickers.length()-1);

        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url+"/apis/stocks/lastPrice?ticker="+tickers, null, response -> {
            try {
                for(int i=0; i<response.length(); i++){
                    JSONObject jsonObject = response.getJSONObject(i);
                    Stock stock = favoriteObj.get(jsonObject.getString("ticker").toUpperCase());
                    stock.high = String.valueOf(jsonObject.optDouble("high", 0));
                    stock.low = String.valueOf(jsonObject.optDouble("low", 0));
                    stock.openPrice = String.valueOf(jsonObject.optDouble("open", 0));
                    stock.changePrice = String.valueOf(jsonObject.optDouble("change", 0));
                    stock.bidPrice = String.valueOf(jsonObject.optDouble("bidPrice", 0));
                    stock.volume = String.valueOf(jsonObject.optDouble("volume", 0));
                    stock.mid = String.valueOf(jsonObject.optDouble("mid", 0));
                    stock.currentPrice = String.valueOf(jsonObject.optDouble("last", 0));
                    stock.price = stock.currentPrice;
                }

                loading = false;
                if(!loading && counter == favorites.size())
                    getPortfolioData(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> error.printStackTrace());
        queue.add(objectRequest);

        for(String fav:favorites){
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url+"/apis/stocks/companyDetails?ticker="+fav,
                    null, response -> {
                try {
                    counter++;
                    favoriteObj.get(response.getString("ticker").toUpperCase()).description = response.getString("description");
                    favoriteObj.get(response.getString("ticker").toUpperCase()).name = response.getString("name");
                    Log.i("Volley data: ", String.valueOf(counter));
                    if(!loading && counter == favorites.size())
                        getPortfolioData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> error.printStackTrace());
            queue.add(request);
        }

    }

    private void getPortfolioData(View view){
        String tickers = "";
        counter = 0;
        loading = true;

        if(portfolio.isEmpty()) loadView(view);
        for(Stock pfStock:portfolio){
            if(pfStock.ticker.equalsIgnoreCase("Net Worth")) continue;
            tickers = tickers+pfStock.ticker.toUpperCase()+",";
            portfolioObj.put(pfStock.ticker.toUpperCase(), pfStock);
        }
        if(tickers.length()>0) tickers = tickers.substring(0, tickers.length()-1);

        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url+"/apis/stocks/lastPrice?ticker="+tickers, null, response -> {
            try {
                for(int i=0; i<response.length(); i++){
                    JSONObject jsonObject = response.getJSONObject(i);
                    Stock stock = portfolioObj.get(jsonObject.getString("ticker").toUpperCase());
                    stock.high = String.valueOf(jsonObject.optDouble("high", 0));
                    stock.low = String.valueOf(jsonObject.optDouble("low", 0));
                    stock.openPrice = String.valueOf(jsonObject.optDouble("open", 0));
                    stock.changePrice = String.valueOf(jsonObject.optDouble("change", 0));
                    stock.bidPrice = String.valueOf(jsonObject.optDouble("bidPrice", 0));
                    stock.volume = String.valueOf(jsonObject.optDouble("volume", 0));
                    stock.mid = String.valueOf(jsonObject.optDouble("mid", 0));
                    stock.currentPrice = String.valueOf(jsonObject.optDouble("last", 0));
                    stock.price = stock.currentPrice;
                }

                loading = false;
                if(!loading && counter == portfolio.size())
                    loadView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> error.printStackTrace());
        queue.add(objectRequest);

        for(Stock fav:portfolio){
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url+"/apis/stocks/companyDetails?ticker="+fav.ticker, null, response -> {
                try {
                    counter++;
                    portfolioObj.get(response.getString("ticker").toUpperCase()).description = response.getString("description");
                    portfolioObj.get(response.getString("ticker").toUpperCase()).name = response.getString("name");
                    Log.i("Volley data: ", String.valueOf(counter));
                    if(!loading && counter == portfolio.size())
                        loadView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> error.printStackTrace());
            queue.add(request);
        }
    }

    private void loadView(View view){
        itemMap.clear();
        stocks.clear();
        double value = 0;

        TextView currentDate = findViewById(R.id.home_date);
        TextView tiingoCreds = findViewById(R.id.tingo_creds);
        tiingoCreds.setVisibility(View.VISIBLE);
        tiingoCreds.setOnClickListener(v -> {
            Uri uriUrl = Uri.parse("https://www.tiingo.com/");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        });

        Calendar cal = Calendar.getInstance();
        String month = monthName[cal.get(Calendar.MONTH)];
        int day = cal.get(Calendar.DATE);
        int year = cal.get(Calendar.YEAR);
        currentDate.setText(month+" "+String.valueOf(day)+", "+String.valueOf(year));

        mList = findViewById(R.id.recyclerview);
        mList.setNestedScrollingEnabled(false);
        homeView.setVisibility(View.GONE);

        sectionedAdapter = new SectionedRecyclerViewAdapter();
        int idx = 0;
        int remove = -1;
        for(Stock st:portfolio){
            if(st.ticker.equalsIgnoreCase("Net Worth")) remove = idx;
            if(st.ticker.equalsIgnoreCase("Net Worth")) continue;
            value += st.stocksOwned*Double.parseDouble(st.price);
            if(favoriteObj.containsKey(st.ticker.toUpperCase())){
                favoriteObj.get(st.ticker.toUpperCase()).stocksOwned = st.stocksOwned;
            }
            idx++;
        }
        if(remove>=0) portfolio.remove(remove);
        sectionTags.clear();

        value += bookmarkCache.getAmount();
        portfolio.add(0, new Stock("Net Worth", String.format("%.2f", value), "", ""));
        StockSection portfolioSection = new StockSection("PORTFOLIO", portfolio, this, context);
        itemMap.put(portfolioSection, portfolio);
        stocks.addAll(portfolio);

        List<Stock> favs = new ArrayList<Stock>(favoriteObj.values());
        StockSection favoritesSection = new StockSection("FAVORITES", favs, this, context);
        itemMap.put(favoritesSection, favs);
        stocks.addAll(favs);

        sectionedAdapter.addSection(portfolioSection);
        sectionTags.put(portfolioSection, "PORTFOLIO");
        sectionedAdapter.addSection(favoritesSection);
        sectionTags.put(favoritesSection, "FAVORITES");

        linearLayoutManager = new LinearLayoutManager(context);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(new ColorDrawable(getResources().getColor(R.color.detArticleDesc)));
        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(sectionedAdapter);
        enableSwipeToDelete();
        progressLayout.setVisibility(View.GONE);
        homeView.setVisibility(View.VISIBLE);
    }

    private void updateData(){
        if(sectionedAdapter == null) return;
        getData(view);
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(context) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                StockSection section = (StockSection) sectionedAdapter.getSectionForPosition(position);
                int sectionPosition = sectionedAdapter.getPositionInSection(position);
                Stock stock = itemMap.get(section).remove(sectionPosition);
                sectionedAdapter.notifyDataSetChanged();
//                Toast.makeText(context, "Stock "+stock.ticker+" was removed from "+sectionTags.get(section).toLowerCase(),
//                        Toast.LENGTH_SHORT).show();
                if(sectionTags.get(section).equalsIgnoreCase("FAVORITES")) bookmarkCache.toggleFavorite(stock);
                else bookmarkCache.updatePortfolio(stock);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                SectionedRecyclerViewAdapter adapter = (SectionedRecyclerViewAdapter) recyclerView.getAdapter();
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                StockSection section = (StockSection) sectionedAdapter.getSectionForPosition(from);
                int sectionPosition = sectionedAdapter.getPositionInSection(from);

                int toPosition = sectionedAdapter.getPositionInSection(to);
                List<Stock> sectionList = itemMap.get(section);
                Stock st = sectionList.remove(sectionPosition);
                sectionList.add(toPosition, st);
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                          int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof ItemViewHolder) {
                        ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
                        myViewHolder.rootView.setBackgroundColor(getResources().getColor(R.color.detArticleDesc));
                    }
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public void clearView(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (viewHolder instanceof ItemViewHolder) {
                    ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
                    myViewHolder.rootView.setBackgroundColor(getResources().getColor(R.color.cleanWhite));
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(mList);
    }

    @Override
    public void onItemRootViewClicked(@NonNull StockSection section, int itemAdapterPosition) {
        int sectionPosition = sectionedAdapter.getPositionInSection(itemAdapterPosition);
        Stock stock = itemMap.get(section).get(sectionPosition);
//        Toast.makeText(context, "Fetching details for "+stock.ticker, Toast.LENGTH_SHORT).show();
        Log.i("Fetching details for ", stock.ticker);
        Intent articleIntent = new Intent(activity.getApplicationContext(), DetailedArticleActivity.class);

        articleIntent.putExtra("Stock", stock);
        startActivity(articleIntent);
    }

    @Override
    protected void onDestroy() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null)
        {
            bookmarkCache.refresh();
        }
        updateData();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null) bookmarkCache.refresh();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        bookmarkCount = bookmarkCache.getFavorites().size();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(bookmarkCache!= null) bookmarkCache.commitChanges();
        super.onStop();
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ArticleItem.Article item, Boolean isLongPress, RecyclerView.Adapter adapter, Boolean bookmarkFrag);
    }
}
