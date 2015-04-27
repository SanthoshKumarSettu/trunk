package com.dealersaleschannel.tv;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;

public class DataHandler extends DefaultHandler {

	// list for imported product data
	private ArrayList<Slide> theSlides;
	// string to track each entry
	private Slide currSlide;
	// flag to keep track of XML processing
	private boolean isText = false;

	private boolean isName = false;

	private StringBuilder currentNameStringBuilder;
	private StringBuilder currentTextStringBuilder;
	private StringBuilder currText;
	
	public boolean isInitialLoad = false;
	
	private int nodeCount = 0;

	// constructor
	public DataHandler() {
		super();
		theSlides = new ArrayList<Slide>();
		currentNameStringBuilder = new StringBuilder();
		currentTextStringBuilder = new StringBuilder();
		currText = new StringBuilder();
	}

	// start of the XML document
	public void startDocument() {

		Log.i("DataHandler", "Start of XML document");

	}

	// end of the XML document
	public void endDocument() {

		Log.i("DataHandler", "End of XML document");
		currSlide = null;
		currentNameStringBuilder = null;
		currentTextStringBuilder = null;
		currText = null;

	}

	// opening element tag
	public void startElement(String uri, String name, String qName,
			Attributes atts) throws SAXException {

		if(nodeCount > 200 && isInitialLoad)
		{
			throw new SAXException();
		}
		
		// find out if the element is a slide
		if (qName.equals("slide")) {

			// set content tags to false
			isText = false;
			isName = false;
			// create View item for brand display
			Slide slide = new Slide();

			// add the attribute value to the displayed text
			slide.order = atts.getValue("order");
			slide.textFileMD5 = atts.getValue("textfilemd5");
			slide.contentType = atts.getValue("contenttype");
			slide.backgroundColor = atts.getValue("backgroundcolor");
			slide.displayTime = atts.getValue("displaytime");
			slide.textColor = atts.getValue("textcolor");
			slide.textSize = atts.getValue("textsize");
			slide.headerImageMD5 = atts.getValue("headerimagemd5");
			slide.headerImage = atts.getValue("headerimage");
			slide.footerImageMD5 = atts.getValue("footerimagemd5");
			slide.footerImage = atts.getValue("footerimage");
			slide.make = atts.getValue("make");
			slide.model = atts.getValue("model");
			slide.stock = atts.getValue("stock");
			slide.year = atts.getValue("year");
			slide.industry = atts.getValue("industry");
			slide.location = atts.getValue("location");
			slide.price = atts.getValue("price");
			slide.hours = atts.getValue("hours");
			slide.notes = atts.getValue("notes");
			slide.layout = atts.getValue("layout");
			slide.backgroundimage = atts.getValue("backgroundimage");
			slide.equipmentinfohighlightcolor = atts
					.getValue("equipmentinfohighlightcolor");
			slide.textshadowcolor = atts.getValue("textshadowcolor");
			slide.istextdropshadow = atts.getValue("istextdropshadow");
			slide.ishighlightequipmentinfo = atts
					.getValue("ishighlightequipmentinfo");
			slide.backgroundImageMD5 = atts.getValue("backgroundimagemd5");
			slide.category = atts.getValue("category");
			slide.salescontact = atts.getValue("salescontact");

			// Set current slide
			currSlide = slide;

		}
		// the element is a text
		else if (qName.equals("text")) {
			isText = true;
			isName = false;

		} else if (qName.equals("name")) {

			isName = true;
			isText = false;

		}
	}

	// closing element tag
	public void endElement(String uri, String name, String qName) {
		if (qName.equals("slide")) {
			currSlide.data = currentTextStringBuilder.toString();
			currSlide.name = currentNameStringBuilder.toString();
			// add the new slide to the list
			theSlides.add(currSlide);
			// reset the variable for future items
			currSlide = null;
			currentNameStringBuilder.setLength(0);
			currentTextStringBuilder.setLength(0);
			nodeCount = nodeCount + 1;
		}
		
		
	}

	// element content
	public void characters(char ch[], int start, int length) {

		// string to store the character content
		currText.setLength(0);

		// prepare for the next item
		if (isText) {
			if (currSlide.contentType.equals("text")) {
				// loop through the character array
				for (int i = start; i < start + length; i++) {
					switch (ch[i]) {
					case '\\':
						break;
					case '"':
						break;
					case '\'':
						break;
					default:
						currText.append(ch[i]);
						break;
					}
				}
				currentTextStringBuilder.append(currText);

			} else {
				// loop through the character array
				for (int i = start; i < start + length; i++) {
					switch (ch[i]) {
					case '\\':
						break;
					case '"':
						break;
					case '\'':
						break;
					case '\n':
						break;
					case '\r':
						break;
					case '\t':
						break;
					default:
						currText.append(ch[i]);
						break;
					}
				}

				currentTextStringBuilder.append(currText);
			}

		}

		if (isName) {
			// loop through the character array
			for (int i = start; i < start + length; i++) {
				switch (ch[i]) {
				case '\\':
					break;
				case '"':
					break;
				case '\'':
					break;
				case '\n':
					break;
				case '\r':
					break;
				case '\t':
					break;
				default:
					currText.append(ch[i]);
					break;
				}
			}
			currentNameStringBuilder.append(currText);
		}

	}

	public ArrayList<Slide> getData(String xmlFileLocation) {
		// take care of SAX, input and parsing errors
		try {
			// set the parsing driver
			System.setProperty("org.xml.sax.driver",
					"org.xmlpull.v1.sax2.Driver");
			// create a parser
			SAXParserFactory parseFactory = SAXParserFactory.newInstance();
			SAXParser xmlParser = parseFactory.newSAXParser();
			// get an XML reader
			XMLReader xmlIn = xmlParser.getXMLReader();
			// instruct the application to use this object as the handler
			xmlIn.setContentHandler(this);
			// provide the name and location of the XML file **ALTER THIS FOR
			// YOUR FILE**
			// URL xmlURL = new URL("http://mydomain.com/mydata.xml");
			// //open the connection and get an input stream
			// URLConnection xmlConn = xmlURL.openConnection();
			// InputStreamReader xmlStream = new
			// InputStreamReader(xmlConn.getInputStream());
			// build a buffered reader
			// BufferedReader xmlBuff = new BufferedReader(xmlStream);
			// parse the data
			xmlIn.parse(new InputSource(new FileInputStream(xmlFileLocation)));
		} catch (SAXException se) {
			Log.e("DealerTv", "SAX Error " + se.getMessage());			
		} catch (IOException ie) {
			Log.e("DealerTv", "Input Error " + ie.getMessage());
		} catch (Exception oe) {
			Log.e("DealerTv", "Unspecified Error " + oe.getMessage());
		}
		// return the parsed product list
		return theSlides;
	}
}
