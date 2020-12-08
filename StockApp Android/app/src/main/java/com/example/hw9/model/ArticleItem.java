package com.example.hw9.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


//change to capture correct news fields
public class ArticleItem {

    public static final List<Article> ITEMS = new ArrayList<Article>();

    public static final Map<String, Article> ITEM_MAP = new HashMap<String, Article>();

    private static final int COUNT = 10;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Article item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.title, item);
    }

    private static Article createDummyItem(int position) {
        return new Article("Item " + position, "Item " + position, makeDetails(position), "Unknown Source",
                "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Article implements Serializable {
        public String source;
        public String title;
        public String pubDate;
        public String timeAgo;
        public String artUrl;
        public String imageUrl;
        public String description;

        public Article(String title, String pubDate, String desc, String source, String artUrl, String imageUrl) {
            this.title = title;
            this.source = source;
            this.pubDate = pubDate;
            this.description = desc;
            this.artUrl = artUrl;
            this.imageUrl = imageUrl;
            formattedDate();
        }

       public void formattedDate() {
            try
            {
                LocalDateTime ldt = LocalDateTime.parse(pubDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                ZoneId zoneId = ZoneId.of("America/Los_Angeles");
                ZonedDateTime zdt = ldt.atZone(zoneId);

                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(zdt);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date past = format.parse(date);
                Date now = new Date();
                long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                long minutes= TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                long hours= TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                long days= TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                if(seconds<60)
                {
                    timeAgo = seconds+"s ago";
//                    Log.i("formattedDate: ",seconds+"s ago");
                }
                else if(minutes<60)
                {
                    timeAgo = minutes+"m ago";
//                    Log.i("formattedDate: ",minutes+"m ago");
                }
                else if(hours<24)
                {
                    timeAgo = hours+"h ago";
//                    Log.i("formattedDate: ",hours+"h ago");
                }
                else
                {
                    timeAgo = days+" days ago";
//                    Log.i("formattedDate: ",days+"d ago");
                }
            }
            catch (Exception j){
                j.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "Article{" +
                    "source='" + source + '\'' +
                    ", title='" + title + '\'' +
                    ", pubDate='" + pubDate + '\'' +
                    ", timeAgo='" + timeAgo + '\'' +
                    ", artUrl='" + artUrl + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
