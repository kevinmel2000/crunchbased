package com.github.zyro.crunchbase.service;

import android.content.Context;
import android.widget.ImageView;
import com.github.zyro.crunchbase.R;
import com.github.zyro.crunchbase.entity.Company;
import com.github.zyro.crunchbase.entity.Person;
import com.github.zyro.crunchbase.util.HomeData;
import com.google.gson.reflect.TypeToken;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.api.Scope;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@EBean(scope = Scope.Singleton)
public class CrunchbaseClient {

    /** Application root context. */
    @RootContext
    protected Context context;

    /** Crunchbase API key. */
    protected static final String API_KEY = "api_key=9za7pzrvfch6quf3vgwp2dja";

    /** Get primary home page data elements. */
    public HomeData getHomeData() throws ClientException {
        try {
            // Open a connection and pull data.
            final String response =
                    Ion.with(context, "http://www.crunchbase.com/").asString().get();

            final HomeData data = new HomeData();

            // Parse HTML response.
            final Document document = Jsoup.parse(response);

            // Extract Trending items.
            final List<HomeData.Trending> trendingItems =
                    new ArrayList<HomeData.Trending>();
            for(final Element elem : document.getElementById(
                    "trending-now").getElementsByTag("li")) {
                final HomeData.Trending item = new HomeData.Trending();

                final String[] link = elem.getElementsByTag("a").first()
                        .attr("href").replace("http://www.crunchbase.com/", "")
                        .split("/");
                item.setNamespace(link[link.length - 2]);
                item.setPermalink(link[link.length - 1].replaceAll("[?].*", ""));
                item.setPoints(elem.getElementsByTag("img").size());
                item.setName(elem.getElementsByTag("a").text().trim().replace(
                        "\\u7684CrunchBase\\u7b80\\u4ecb", ""));

                trendingItems.add(item);
            }
            data.setTrending(trendingItems);

            // Extract Recent items.
            final List<HomeData.Recent> recentItems =
                    new ArrayList<HomeData.Recent>();
            for(final Element elem : document.getElementById(
                    "content-newlyfunded").getElementsByTag("li")) {
                final HomeData.Recent item = new HomeData.Recent();

                item.setPermalink(elem.getElementsByTag("a").attr("href")
                        .replace("/company/", ""));
                item.setName(elem.getElementsByTag("a").text().trim());
                item.setSubtext(elem.getElementsByTag("strong").size() > 1 ?
                        elem.getElementsByTag("strong").last().text().trim() :
                        elem.getElementsByTag("span").size() > 0 ?
                                elem.getElementsByTag("span").last().text().trim() :
                                context.getString(R.string.unknown));
                item.setFunds(elem.getElementsByClass("horizbar").text().trim());

                recentItems.add(item);
            }
            data.setRecent(recentItems);

            // Extract Biggest (Top Funded This Year) items.
            final List<HomeData.Biggest> biggestItems =
                    new ArrayList<HomeData.Biggest>();
            for(final Element elem : document.getElementById(
                    "content-biggestfunded").getElementsByTag("li")) {
                final HomeData.Biggest item = new HomeData.Biggest();

                item.setPermalink(elem.getElementsByTag("a").attr("href")
                        .replace("/company/", ""));
                item.setName(elem.getElementsByTag("a").text().trim());
                item.setSubtext(elem.getElementsByTag("strong").size() > 1 ?
                        elem.getElementsByTag("strong").last().text().trim() :
                        elem.getElementsByTag("span").size() > 0 ?
                                elem.getElementsByTag("span").last().text().trim() :
                                context.getString(R.string.unknown));
                item.setFunds(elem.getElementsByClass("horizbar").text().trim());

                biggestItems.add(item);
            }
            data.setBiggest(biggestItems);

            return data;
        }
        catch(final NullPointerException e) {
            throw new ClientException(e);
        }
        catch(final InterruptedException e) {
            throw new ClientException(e);
        }
        catch(final ExecutionException e) {
            throw new ClientException(e);
        }
    }

    /**
     * Trigger an image fetching process with a required asset and target view
     * it is intended to be displayed in. Disabling image loading through the
     * preferences effectively makes this a no-op.
     */
    public void loadImage(final String asset, final ImageView view) {
        //if(!preferences.loadImages().get()) {
        //    return;
        //}

        Ion.with(view).error(R.drawable.no_image_placeholder)
                .load("http://www.crunchbase.com/" + asset);
    }

    /** */
    public void getCompany(final String permalink,
                           final FutureCallback<Company> callback) {
        try {
            Ion.with(context, "http://api.crunchbase.com/v/1/company/" +
                    URLEncoder.encode(permalink.toLowerCase(), "UTF-8") +
                    ".js?" + API_KEY)
                .as(new TypeToken<Company>() {})
                .setCallback(callback);
        }
        catch(final UnsupportedEncodingException e) {
            callback.onCompleted(e, null);
        }
    }

    /** */
    public void getPerson(final String permalink,
                          final FutureCallback<Person> callback) {
        try {
            Ion.with(context, "http://api.crunchbase.com/v/1/person/" +
                        URLEncoder.encode(permalink.toLowerCase(), "UTF-8") +
                        ".js?" + API_KEY)
                    .as(new TypeToken<Person>(){})
                    .setCallback(callback);
        }
        catch(final UnsupportedEncodingException e) {
            callback.onCompleted(e, null);
        }
    }

    public String getFinancialOrganization(final String permalink) {
        throw new UnsupportedOperationException();
        //return performGetRequest(permalink, "financial-organization");
    }

    public String getProduct(final String permalink) {
        throw new UnsupportedOperationException();
        //return performGetRequest(permalink, "product");
    }

    public String getServiceProvider(final String permalink) {
        throw new UnsupportedOperationException();
        //return performGetRequest(permalink, "service-provider");
    }

}