package com.example.hw9.model;

import android.content.Context;
import android.graphics.Typeface;
import android.telephony.CellSignalStrength;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw9.R;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public final class StockSection extends Section {

    private final String title;
    private final List<Stock> list;
    private final ClickListener clickListener;
    private Context context;

    public StockSection(@NonNull final String title, @NonNull final List<Stock> list,
                        @NonNull final ClickListener clickListener, Context context) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_item)
                .headerResourceId(R.layout.section_header)
                .build());

        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
        this.context = context;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    public void changeData(List<Stock> stocks) {
        this.list.clear();
        list.addAll(stocks);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        final Stock stock = list.get(position);
        if(stock.stocksOwned<=0) itemHolder.name.setText(stock.name);
        else itemHolder.name.setText(String.valueOf(stock.stocksOwned)+ " shares");
        itemHolder.ticker.setText(stock.ticker);

        if(stock.ticker.equalsIgnoreCase("Net Worth")){
            itemHolder.price.setVisibility(View.GONE);
            itemHolder.change.setVisibility(View.GONE);
            itemHolder.gotoArrow.setVisibility(View.GONE);
            itemHolder.ticker.setTypeface(Typeface.DEFAULT);
            itemHolder.changeArrow.setVisibility(View.GONE);
            itemHolder.ticker.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 27);
            itemHolder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            itemHolder.name.setTextColor(ContextCompat.getColor(context, R.color.cleanBlack));
            itemHolder.name.setTypeface(Typeface.DEFAULT_BOLD);
            ViewGroup.LayoutParams params = itemHolder.rootView.getLayoutParams();
            params.height = 300;
            itemHolder.rootView.setLayoutParams(params);
        } else {
            itemHolder.price.setText(stock.price);
            double value = Double.parseDouble(stock.changePrice);
            if(value>0){
                itemHolder.changeArrow.setImageResource(R.drawable.ic_twotone_trending_up_24);
                itemHolder.change.setTextColor(ContextCompat.getColor(context, R.color.cleanGreen));
                itemHolder.change.setText(stock.changePrice);
            } else if(value<0){
                itemHolder.changeArrow.setImageResource(R.drawable.ic_baseline_trending_down_24);
                itemHolder.change.setTextColor(ContextCompat.getColor(context, R.color.cleanRed));
                itemHolder.change.setText(stock.changePrice.substring(1));
            } else {
                itemHolder.change.setTextColor(ContextCompat.getColor(context, R.color.detArticleDesc));
                itemHolder.change.setText(stock.changePrice);
            }


            itemHolder.gotoArrow.setOnClickListener(v ->
                    clickListener.onItemRootViewClicked(this, itemHolder.getAdapterPosition())
            );
            itemHolder.rootView.setOnClickListener(v ->
                    clickListener.onItemRootViewClicked(this, itemHolder.getAdapterPosition())
            );
        }
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.section.setText(title);
    }

    public interface ClickListener {
        void onItemRootViewClicked(@NonNull final StockSection section, final int itemAdapterPosition);
    }
}
