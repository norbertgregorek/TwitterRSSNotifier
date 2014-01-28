package com.example.twitterrssnotifier.rsslibrary;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.example.twitterrssnotifier.miscellaneous.Miscellaneous;

/*
 * Class that represents single feed that we want to follow.
 * To Create it use static method Create. It will automaitcally
 * parse the whole XML content.
 */
public class RssFeed
{
	private static final String CHANNEL = "channel";
	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String LINK = "link";
	private static final String AUTHOR = "author";
	private static final String PUB_DATE = "pubDate";
	
	//We store only required elements
	private String title = null;
	private String link = null;
	private String description = null;
	private String rssLink = null;
	
	private URL url = null;
	
	private List<RssMessage> messageList = new ArrayList<RssMessage>();
	
	
	public static RssFeed Create(String feedUrl) throws IOException, BadRssFromUrlException, XmlPullParserException
	{
		RssFeed feed = new RssFeed();
		feed.rssLink = feedUrl;
		feed.url = new URL(feedUrl);
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		
		XmlPullParser parser = factory.newPullParser();
		
		parser.setInput(feed.url.openStream(), null);
	
		boolean isFeedHeader = true;
		boolean done = false;
		String description = null;
	    String title = null;
	    String link = null;
	    String author = null;
	    String pubdate = null;
	    String localPart = null;
		
		int eventType = parser.getEventType();
		
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			switch (eventType)
			{
				case XmlPullParser.START_TAG:
					localPart = parser.getName();
					if (localPart.equalsIgnoreCase(ITEM) && isFeedHeader)
					{
						isFeedHeader = false;
						//Here we have to define how to check if URL is RSS
	    				if (title == null || link == null)
	    					throw new BadRssFromUrlException();
	    				feed.title = title;
	    				feed.link = link;
	    				feed.description = description;
	    				
	    				description = title = link = author = pubdate = null;
					}
					else if (localPart.equalsIgnoreCase(DESCRIPTION))
						description = parser.nextText();
					else if (localPart.equalsIgnoreCase(LINK))
						link = parser.nextText();
					else if (localPart.equalsIgnoreCase(AUTHOR))
						author = parser.nextText();
					else if (localPart.equalsIgnoreCase(PUB_DATE))
						pubdate = parser.nextText();
					else if (localPart.equalsIgnoreCase(TITLE))
						title = parser.nextText();
					break;
				case XmlPullParser.END_TAG:
					localPart = parser.getName();
					if (localPart.equalsIgnoreCase(ITEM))
					{
						//We ignore items where there is no link.
						if (link != null)
						{
							if (title == null)
							{
								title = "Auto generated title";
							}
							if (description == null)
							{
								description = "Auto generated description";
							}
							if (author == null)
							{
								author = "Unknown author";
							}
							if (pubdate == null)
							{
								//If there is no pubdate 
								Calendar c = Calendar.getInstance();
								SimpleDateFormat df = new SimpleDateFormat(Miscellaneous.RFC822, Locale.ENGLISH);
								pubdate = df.format(c.getTime());
							}
							
							Date truc = null;
							try
							{
								truc = new SimpleDateFormat(Miscellaneous.RFC822, Locale.ENGLISH).parse(pubdate);
								//TODO
								//If the year has two digit we add 2000 years to it...
								//It seems awfully because we use deprecated functions
								//but I don't how to fix it at present
								//I change the year because some rss feeds store year as two digits
								//(for instance TVN)
								if (truc.getYear() < 100)
								{
									truc.setYear(2000 + truc.getYear());
								}
							}
							catch (ParseException e)
							{
								Log.e("DataTime", "Probably bad format of data");
							}
							feed.messageList.add(new RssMessage(title, description, link,
									truc.getTime(), author, feed.getRssLink()));
						}
						
						description = title = link = author = pubdate = null;
					}
					else if (localPart.equalsIgnoreCase(CHANNEL))
					{
						done = true;
						if (isFeedHeader)
						{
							isFeedHeader = false;
							//Here we have to define how to check if URL is RSS
		    				if (title == null || link == null)
		    					throw new BadRssFromUrlException();
		    				feed.title = title;
		    				feed.link = link;
		    				feed.description = description;
		    				
		    				description = title = link = author = pubdate = null;
						}
					}
					break;
			}
			eventType = parser.next();
		}
		return feed;
	}
	
	public String getTitle()
	{
		return title;
	}
	public String getLink()
	{
		return link;
	}
	public String getDescription()
	{
		return description;
	}
	public String getRssLink()
	{
		return rssLink;
	}
	public List<RssMessage> getMessageList()
	{
		return messageList;
	}
}