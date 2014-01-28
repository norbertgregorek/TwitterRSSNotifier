package com.example.twitterrssnotifier.rsslibrary;

/*
 * Class that holds information about a single message from feed.
 * If pubdate of message is not defined I set it when we got this message,
 * because pubdate is compulsory.
 */
public class RssMessage
{
	//We have to have pubdate of message. If it is not provided we 
	//use as a date the day when we grabbed this message.
	//We indicate that date using this token
	public static final String MANUAL_PUBDATE = "manual ";

	private String title;
	private String description;
	private String link; //Link to the full article
	private Long pubdate;
	private String rssLink; //Holds a parent link (from where this message comes from)
	private String author;
	
	public RssMessage(String title, String description, String link,
	                  Long pubdata, String author, String rssLink)
	{
		super();
		this.title = title;
		this.description = description;
		this.link = link;
		this.pubdate = pubdata;
		this.rssLink = rssLink;
		this.author = author;
	}
	
	public String getTitle()
	{
		return title;
	}
	public String getDescription()
	{
		return description;
	}
	public String getLink()
	{
		return link;
	}
	public Long getPubdata()
	{
		return pubdate;
	}
	public String getRssLink()
	{
		return rssLink;
	}
	public String getAuthor()
	{
		return author;
	}
}