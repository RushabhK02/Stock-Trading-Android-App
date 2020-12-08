package com.example.hw9.functionalities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw9.R;
import com.example.hw9.model.ArticleItem;
import com.example.hw9.MainActivity.OnListFragmentInteractionListener;
import com.example.hw9.model.ArticleItem.Article;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.example.hw9.model.ArticleItem.Article} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ArticleRecyclerViewAdapter extends RecyclerView.Adapter<ArticleRecyclerViewAdapter.ViewHolder> {

    private List<ArticleItem.Article> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context context;
    private RecyclerView.Adapter adapter;
    private static HashMap<String, Integer> imageMapping = new HashMap<String, Integer>();

    final Transformation atransformation = new RoundedCornersTransformation(45, 0, RoundedCornersTransformation.CornerType.ALL);

    @Override
    public int getItemViewType(int position) {
        if(position==0) return 0;
        return 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(adapter==null) adapter = this;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType==1?R.layout.fragment_article: R.layout.head_article, parent, false);
        switch (viewType) {
            case 0: return new HeadViewHolder(view);
            default: return new ArticleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final ArticleRecyclerViewAdapter.ViewHolder holder,int position) {
        switch (holder.getItemViewType()) {
            case 0: {
                final HeadViewHolder vh = (HeadViewHolder) holder;
                vh.mSource.setText(mValues.get(position).source);
                vh.mTitle.setText(mValues.get(position).title);
                vh.mTime.setText(mValues.get(position).timeAgo);
                vh.mItem = mValues.get(position);
                Picasso.with(context)
                        .load(mValues.get(position).imageUrl)
                        .transform(atransformation)
                        .fit()
                        .into(vh.mImg, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
//                                    Picasso.get()
                                Picasso.with(context)
                                        .load(R.drawable.no_image)
                                        .transform(atransformation)
                                        .fit()
                                        .into(vh.mImg);
                            }
                        });
                vh.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(context,"Opening article...",Toast.LENGTH_SHORT).show();
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(vh.mItem, false, adapter, false);
                        }
                    }
                });
                vh.mView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(vh.mItem, true, adapter, false);
                            return true;
                        }
                        return false;
                    }
                });
                break;
            }
            case 1: {
                final ArticleViewHolder viewHolder = (ArticleViewHolder) holder;
                viewHolder.mTitle.setText(mValues.get(position).title);
                viewHolder.mTime.setText(mValues.get(position).timeAgo);
                viewHolder.mSource.setText(mValues.get(position).source);
                viewHolder.mItem = mValues.get(position);
                Picasso.with(context)
                        .load(mValues.get(position).imageUrl)
                        .transform(atransformation)
                        .fit()
                        .into(viewHolder.mImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
//                                    Picasso.get()
                                Picasso.with(context)
                                        .load(R.drawable.no_image)
                                        .transform(atransformation)
                                        .fit()
                                        .into(viewHolder.mImage);
                            }
                        });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(context,"Opening article...",Toast.LENGTH_SHORT).show();
                        if (null != mListener) {
                            Log.i("on click", "Listener exists");
                            mListener.onListFragmentInteraction(viewHolder.mItem, false, adapter, false);
                        }
                    }
                });

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(viewHolder.mItem, true, adapter, false);
                            return true;
                        }
                        return false;
                    }
                });
                break;
            }
        }
    }

    public ArticleRecyclerViewAdapter(List<ArticleItem.Article> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    public void swapItems(List<Article> items) {
        mValues = items;
        this.notifyDataSetChanged();
    }

    public void notifyChange(){
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImage;
        public Article mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.item_image);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    class HeadViewHolder extends ViewHolder {
        public final View mView;
        public final ImageView mImg;
        public final TextView mSource;
        public final TextView mTime;
        public final TextView mTitle;

        public HeadViewHolder(View view) {
            super(view);
            mView = view;
            mImg = view.findViewById(R.id.item_image);
            mSource = view.findViewById(R.id.article_source);
            mTime = view.findViewById(R.id.article_time);
            mTitle = view.findViewById(R.id.article_title);
        }
    }

    class ArticleViewHolder extends ViewHolder {
        public final View mView;
        public final TextView mTitle;
        public final TextView mSource;
        public final TextView mTime;
        public ArticleViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.article_title);
            mTime = view.findViewById(R.id.article_time);
            mSource = view.findViewById(R.id.article_source);
        }
    }
}