package com.example.hw9.functionalities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw9.MainActivity;
import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.model.ArticleItem.Article;
import com.example.hw9.model.Stock;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DetailedArticleActivity extends AppCompatActivity
        implements MainActivity.OnListFragmentInteractionListener {
    final Transformation atransformation = new RoundedCornersTransformation(35, 0, RoundedCornersTransformation.CornerType.TOP);

    GridView simpleGrid;
    RecyclerView newsView;
    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    boolean bookmarkFlag=false;
    boolean loading1 = true, loading2 = true, loading3 = true;
    boolean isSearchStart = false;
    double remainingAmount = 0.0;
    boolean isEllipsize;
    String query;
    String ticker;
    String name;

    RequestQueue queue;
//    String url = "http://10.0.2.2:8081";
    String url = "https://cryptic-waters-26273.herokuapp.com";
    private LinearLayout progressLayout;
    private NestedScrollView detailsCard;
    private Menu menu;
    private BookmarkCache bookmarkCache;
    private Button tradeBtn;
    private Context context;
    private String m_Text;
    Stock stock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = DetailedArticleActivity.this;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        queue = Volley.newRequestQueue(getApplicationContext());

        isSearchStart = false;
        Intent intent = getIntent();
        handleIntent(intent);
        bookmarkCache = BookmarkCache.getInstance(this);

        View view = findViewById(R.id.det_layout).getRootView();
        detailsCard = view.findViewById(R.id.det_card);
        detailsCard.setVisibility(View.GONE);
        progressLayout = view.findViewById(R.id.progress_layout);
        progressLayout.setVisibility(View.VISIBLE);
        remainingAmount = bookmarkCache.getAmount();

        if(!isSearchStart) {
            stock = (Stock) getIntent().getSerializableExtra("Stock");
            ticker = stock.getTicker().toUpperCase();
            name = stock.getName();
        }
        bookmarkFlag = bookmarkCache.getFavorites().contains(ticker);

        loading1 = true;
        loading2 = true;
        loading3 = true;
        getData(view);
    }

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            isSearchStart = true;
            query = intent.getStringExtra(SearchManager.QUERY);
            String[] tokens = query.split("-");
            ticker = tokens[0].trim().toUpperCase();
            if(tokens.length>1) name = tokens[1].trim();
            Log.i("Search Query", query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if(!loading1 || !loading2 || !loading3) getMenuInflater().inflate(R.menu.detailed_article_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(loading1 || loading2 || loading3) return super.onOptionsItemSelected(item);
        int id = item.getItemId();
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_bookmark:
                //Toggle Bookmark
                bookmarkFlag = !bookmarkFlag;
                bookmarkCache.toggleFavorite(stock);
                bookmarkFlag = bookmarkCache.getFavorites().contains(stock.ticker);
                if(bookmarkFlag) Toast.makeText(this, "\""+stock.ticker+"\" was added to favorites", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "\""+stock.ticker+"\" was removed from favorites", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(loading1 || loading2 || loading3) return super.onPrepareOptionsMenu(menu);
        MenuItem bookmarkItem = this.menu.findItem(R.id.action_bookmark);
        // set your desired icon here based on a flag if you like
        if(bookmarkFlag) bookmarkItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
        else bookmarkItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));

        return super.onPrepareOptionsMenu(menu);
    }

    public void setMenu(){
        invalidateOptionsMenu();
    }

    private void getData(View view){
        if(stock == null) stock = new Stock(ticker, name, "0", "0");
//        company details
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url+"/apis/stocks/companyDetails?ticker="+ticker, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    stock.description = response.getString("description");
                    Log.i("Volley data: ", stock.description);
                    loading1 = false;
                    if(!loading2 && !loading3) loadData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("Error getting company description!",error.getMessage()));

//      stock info
        JsonArrayRequest objectRequest2 = new JsonArrayRequest(Request.Method.GET, url+"/apis/stocks/lastPrice?ticker="+ticker, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject jsonObject = response.getJSONObject(0);
                    stock.high = String.valueOf(jsonObject.optDouble("high", 0));
                    stock.low = String.valueOf(jsonObject.optDouble("low", 0));
                    stock.openPrice = String.valueOf(jsonObject.optDouble("open", 0));
                    stock.changePrice = String.valueOf(jsonObject.optDouble("change", 0));
                    stock.bidPrice = String.valueOf(jsonObject.optDouble("bidPrice", 0));
                    stock.volume = String.valueOf(jsonObject.optDouble("volume", 0));
                    stock.mid = String.valueOf(jsonObject.optDouble("mid", 0));
                    stock.currentPrice = String.valueOf(jsonObject.optDouble("last", 0));
                    stock.price = stock.currentPrice;
                    loading2 = false;
                    if(!loading1 && !loading3) loadData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("Error getting company description!",error.getMessage()));

//        news articles
        JsonArrayRequest objectRequest3 = new JsonArrayRequest(Request.Method.GET, url+"/apis/news/"+ticker, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    List<Article> articles = new ArrayList<>();
                    for(int i=0; i<response.length(); i++){
                        JSONObject artObj = response.getJSONObject(i);
                        Article article = new Article(artObj.getString("title"),
                                artObj.getString("publishedAt"),
                                artObj.getString("description"),
                                artObj.getString("source"),
                                artObj.getString("url"),
                                artObj.getString("urlToImage"));
                        articles.add(article);
                    }
                    stock.articles = articles;
                    loading3 = false;
                    if(!loading1 && !loading2) loadData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("Error getting company description!",error.getMessage()));

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        objectRequest2.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        objectRequest3.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(objectRequest);
        queue.add(objectRequest2);
        queue.add(objectRequest3);

    }

    private void loadChartData(View view){
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url+"/apis/stocks/history?ticker="+ticker, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    stock.description = response.getString("description");
                    Log.i("Volley data: ", stock.description);
                    if(!loading2 && !loading3 && !loading1) loadData(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("Error getting company description!",error.getMessage()));
        objectRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(objectRequest);
    }

    private void loadData(View view){
        WebView webview;
        webview = (WebView) findViewById(R.id.detailed_chart);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.clearCache(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("file:///android_asset/index.html");
        webview.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                view.loadUrl("javascript:loadGraph('"+stock.ticker+"')");
            }
        });

        TextView tickerTv = view.findViewById(R.id.detailed_ticker);
        tickerTv.setText(ticker);
        TextView nameTv = view.findViewById(R.id.detailed_name);
        nameTv.setText(stock.name);
        TextView price = view.findViewById(R.id.detailed_price);
        price.setText("$"+stock.price);
        TextView changePrice = view.findViewById(R.id.detailed_change);

        if(Double.parseDouble(stock.changePrice)>0) {
            changePrice.setTextColor(getResources().getColor(R.color.cleanGreen));
            changePrice.setText("$"+stock.changePrice);
        } else if(Double.parseDouble(stock.changePrice)<0) {
            changePrice.setTextColor(getResources().getColor(R.color.cleanRed));
            changePrice.setText("-$"+stock.changePrice.substring(1));
        } else {
            changePrice.setText("$"+stock.changePrice);
        }

        simpleGrid = (GridView) findViewById(R.id.stats_grid_view); // init GridView
        simpleGrid.setNestedScrollingEnabled(false);

        String numString = String.format("%,.2f", Double.parseDouble(stock.volume));
        // Create an object of CustomAdapter and set Adapter to GirdView
        String[] stats = new String[]{"Current Price: "+stock.currentPrice,
                "Low: "+stock.low, "Bid Price: "+stock.bidPrice,
                "OpenPrice: "+stock.openPrice, "Mid: "+stock.mid, "High: "+stock.high,
                "Volume: "+numString};
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), stats);
        simpleGrid.setAdapter(customAdapter);

        TextView companyInfo = view.findViewById(R.id.det_section);
        companyInfo.setText(stock.description);
        TextView showMoreBtn = view.findViewById(R.id.showText);
        int length = "Microsoft (Nasdaq “MSFT” @microsoft) enables digital transformation for the era of an intelligent cloud and an int".length();
        if(length<stock.description.length()){
            companyInfo.setMaxLines(2);
            companyInfo.setEllipsize(TextUtils.TruncateAt.END);
            showMoreBtn.setOnClickListener(v -> {
                TextView tv = (TextView) v;
                if(tv.getText().toString().contains("more")) {
                    companyInfo.setMaxLines(Integer.MAX_VALUE);
                    tv.setText("Show less");
                }
                else {
                    companyInfo.setMaxLines(2);
                    tv.setText("Show more...");
                }
            });
            showMoreBtn.setVisibility(View.VISIBLE);
        } else {
            companyInfo.setMaxLines(2);
            companyInfo.setEllipsize(null);
            showMoreBtn.setVisibility(View.GONE);
        }

        TextView detShares = view.findViewById(R.id.det_shares);

        Stock portfolioStock = bookmarkCache.getPortfolioStock(stock.ticker);
        if(portfolioStock == null) {

            detShares.setText(Html.fromHtml("<span>You have 0 shares of " + stock.ticker + ".</span><br />" +
                    "<span align='center'>Start trading!</span>"));
        } else {
            stock.stocksOwned = portfolioStock.stocksOwned;
            stock.buyValue = portfolioStock.buyValue;
            double marketValue = stock.stocksOwned*Double.parseDouble(stock.currentPrice);
            detShares.setText(Html.fromHtml("<span>Shares owned: "+stock.stocksOwned +"</span><br />" +
                            "<span> Market Value: $"+marketValue+"</span>"));
        }

        tradeBtn = view.findViewById(R.id.det_trade_btn);
        tradeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.trade_dialog,
                        (ViewGroup) findViewById(R.id.dialog_root));
                TextView title = layout.findViewById(R.id.dialog_title);
                TextView amt = layout.findViewById(R.id.trade_amt);
                TextView tradeTotal = layout.findViewById(R.id.trade_total);

                remainingAmount = bookmarkCache.getAmount();
                amt.setText("$"+ String.format("%.2f", remainingAmount) +" available to buy "+stock.ticker);
                tradeTotal.setText("0 x $"+stock.currentPrice+"/share = $0.00");

                title.setText("Trade "+ stock.getName()+" shares");
                builder.setView(layout);
                AlertDialog dialog = builder.create();

                Button buyBtn = layout.findViewById(R.id.trade_buy);
                Button sellBtn = layout.findViewById(R.id.trade_sell);
                EditText input = layout.findViewById(R.id.trade_shares);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String token = "";
                        if(s.length()>0) token = String.valueOf(s);
                        else token = "0";
                        Double v = Double.parseDouble(token);
                        Double price = Double.parseDouble(stock.currentPrice);
                        String total = String.valueOf(v*price);
                        tradeTotal.setText(s+" x $"+stock.currentPrice+"/share = $"+total);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                buyBtn.setOnClickListener((View.OnClickListener) v -> {
                    String sharesTraded = input.getText().toString();
                    double value = 0;
                    if(sharesTraded.length()>0) value = Double.parseDouble(sharesTraded);

                    // if failed
                    if(checkValue(sharesTraded, true)) {
                        double oldValue = stock.stocksOwned*Double.parseDouble(stock.currentPrice);
                        stock.stocksOwned += value;
                        bookmarkCache.updatePortfolio(stock);
                        remainingAmount = bookmarkCache.getAmount();
                        double marketValue = stock.stocksOwned*Double.parseDouble(stock.currentPrice);
                        remainingAmount = remainingAmount - (marketValue - oldValue);
                        bookmarkCache.updateAmount(remainingAmount);
                        if(stock.stocksOwned>0) {
                            detShares.setText(Html.fromHtml("<span>Shares owned: " + stock.stocksOwned + "</span><br />" +
                                    "<span> Market Value: $" + String.format("%.2f", marketValue) + "</span>"));
                        } else detShares.setText(Html.fromHtml("<span>You have 0 shares of " + stock.ticker + ".</span><br />" +
                                "<span align='center'>Start trading!</span>"));
                        dialog.dismiss();
                        AlertDialog.Builder successBuilder = new AlertDialog.Builder(context);

                        LayoutInflater inflater1 = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View view1 = inflater1.inflate(R.layout.trade_success_dialog,
                                (ViewGroup) findViewById(R.id.dialog_root));

                        TextView msg = view1.findViewById(R.id.success_msg);
                        Button doneBtn = view1.findViewById(R.id.done_btn);

                        msg.setText("You have successfully bought " + sharesTraded + " shares of "+ stock.ticker);
                        successBuilder.setView(view1);
                        AlertDialog successDialog = successBuilder.create();
                        doneBtn.setOnClickListener(v1 -> successDialog.dismiss());
                        successDialog.getWindow().setBackgroundDrawableResource(R.drawable.round_shape_btn);
                        successDialog.show();
                    }
                });


                sellBtn.setOnClickListener((View.OnClickListener) v -> {
                    String sharesTraded = input.getText().toString();
                    double value = 0;
                    if(sharesTraded.length()>0) value = Double.parseDouble(sharesTraded);

                    if(checkValue(sharesTraded, false)) {
                        double oldValue = stock.stocksOwned*Double.parseDouble(stock.currentPrice);
                        stock.stocksOwned -= value;
                        bookmarkCache.updatePortfolio(stock);
                        double marketValue = stock.stocksOwned*Double.parseDouble(stock.currentPrice);
                        remainingAmount = bookmarkCache.getAmount() + (oldValue - marketValue);
                        bookmarkCache.updateAmount(remainingAmount);
                        if(stock.stocksOwned>0) {
                            detShares.setText(Html.fromHtml("<span>Shares owned: " + stock.stocksOwned + "</span><br />" +
                                    "<span> Market Value: $" + String.format("%.2f", marketValue) + "</span>"));
                        } else detShares.setText(Html.fromHtml("<span>You have 0 shares of " + stock.ticker + ".</span><br />" +
                                "<span align='center'>Start trading!</span>"));
                        dialog.dismiss();
                        AlertDialog.Builder successBuilder = new AlertDialog.Builder(context);

                        LayoutInflater inflater12 = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View view1 = inflater12.inflate(R.layout.trade_success_dialog,
                                (ViewGroup) findViewById(R.id.dialog_root));
                        TextView msg = view1.findViewById(R.id.success_msg);
                        Button doneBtn = view1.findViewById(R.id.done_btn);

                        msg.setText("You have successfully sold " + sharesTraded + " shares of "+stock.ticker);
                        successBuilder.setView(view1);
                        AlertDialog successDialog = successBuilder.create();
                        doneBtn.setOnClickListener(v12 -> successDialog.dismiss());
                        successDialog.getWindow().setBackgroundDrawableResource(R.drawable.round_shape_btn);
                        successDialog.show();
                    }
                });
                dialog.show();
            }
        });

        newsView = findViewById(R.id.news_recycler_view);
        newsView.setNestedScrollingEnabled(false);

        ArticleRecyclerViewAdapter newsAdp = new ArticleRecyclerViewAdapter(stock.articles, this, getApplicationContext());
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        dividerItemDecoration = new DividerItemDecoration(newsView.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(new ColorDrawable(getResources().getColor(R.color.detArticleDesc)));
        newsView.setHasFixedSize(true);
        newsView.setLayoutManager(linearLayoutManager);
        newsView.addItemDecoration(dividerItemDecoration);
        newsView.setAdapter(newsAdp);
        Log.i("Detailed Stock", stock.getTicker());

        setMenu();
        detailsCard.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    public boolean checkValue(String sharesTraded, boolean buy){
        double value = 0.0;
        try{
            value = Double.parseDouble(sharesTraded);
            if(!buy && value<=0) {
                Toast.makeText(context, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                return false;
            }
            if(buy && value<=0) {
                Toast.makeText(context, "Cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                return false;
            }
            if(!buy && value>stock.stocksOwned){
                Toast.makeText(context, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                return false;
            }
            double marketValue = value*Double.parseDouble(stock.currentPrice);
            if(buy && marketValue>bookmarkCache.getAmount()){
                Toast.makeText(context, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                return false;
            }

        } catch (NumberFormatException e){
            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    @Override
    public void onListFragmentInteraction(ArticleItem.Article item, Boolean isLongPress, RecyclerView.Adapter adapter,
                                          Boolean bookmarkFrag) {
        if(!isLongPress) {
            Uri uriUrl = Uri.parse(item.artUrl);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else {
            openArticleDialog(item, adapter, bookmarkFrag);
        }
    }

    public void openArticleDialog(ArticleItem.Article item, RecyclerView.Adapter adapter, Boolean bookmarkFrag) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.article_dialog);
        dialog.setTitle("Article");
        TextView textView = dialog.findViewById(R.id.dialog_title);
        textView.setText(item.title);
        ImageView imageView = dialog.findViewById(R.id.dialog_image);
//        Picasso.get()
        Picasso.with(this)
                .load(item.imageUrl)
                .error(R.drawable.no_image)
                .transform(new RoundedCornersTransformation(10,0))
                .fit()
                .into(imageView);

        ImageView twitter = dialog.findViewById(R.id.dialog_twitter);
        twitter.setOnClickListener(v -> {
//                Toast.makeText(context, "Sharing to twitter...", Toast.LENGTH_SHORT).show();
            String artUrl = item.artUrl;
            Uri webpage = Uri.parse("https://twitter.com/intent/tweet?text="+"Check out this Link: "+artUrl+"&hashtags=CSCI571StockApp");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

//        ShareLinkContent content = new ShareLinkContent.Builder()
//                .setContentTitle("Sharing from Stock App")
//                .setContentDescription(item.title)
//                .setContentUrl(Uri.parse(item.artUrl))
//                .build();
//        ShareButton shareButton = (ShareButton)dialog.findViewById(R.id.fb_share_button);
//        shareButton.setShareContent(content);

        ImageView chrome = dialog.findViewById(R.id.dialog_bookmark);
        chrome.setOnClickListener(v -> {
            Uri uriUrl = Uri.parse(item.artUrl);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
//            shareButton.performClick();
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("message/rfc822");
//                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
//                i.putExtra(Intent.EXTRA_SUBJECT, "I found this article helpful!");
//                i.putExtra(Intent.EXTRA_TEXT  , item.title + "/nLink: "+item.artUrl);
//                try {
////                    Toast.makeText(context,"Sharing via mail...", Toast.LENGTH_SHORT).show();
//                    startActivity(Intent.createChooser(i, "Share via mail"));
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//                }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if(bookmarkCache!= null) {
//            bookmarkCache.updatePortfolio(stock);
            bookmarkCache.commitChanges();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if(bookmarkCache!= null) {
            bookmarkCache.refresh();
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
//        if(autoSuggestAdapter!= null) autoSuggestAdapter.clearData();
        if(bookmarkCache!= null) bookmarkCache.refresh();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        if(bookmarkCache!= null) {
//            bookmarkCache.updatePortfolio(stock);
            bookmarkCache.commitChanges();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(bookmarkCache!= null) {
//            bookmarkCache.updatePortfolio(stock);
            bookmarkCache.commitChanges();
        }
        super.onStop();
    }
}
