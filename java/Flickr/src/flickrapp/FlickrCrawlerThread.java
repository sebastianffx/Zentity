package flickrapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FlickrCrawlerThread extends Thread implements Runnable {

	public FlickrCrawlerThread(int threadId, String APIKey, String PATH,
			BufferedReader br, int jumpedImages) {
		this.threadId = threadId;
		this.APIKey = APIKey;
		this.PATH = PATH;
		this.br = br;
		this.jumpedImages = jumpedImages;
	}

	@Override
	public void run() {
		System.out.println("[Thread " + String.valueOf(threadId)
				+ "] Started Running");
		boolean workLeft = true;
		while (workLeft) {
			synchronized (br) {
				try {
					String strLine;
					//Jump n works
					for (int i=0;i<jumpedImages;i++)
						br.readLine();
					// Read File Line By Line
					if ((strLine = br.readLine()) != null) {
						// Print the content on the console
						System.out.println("[Thread "
								+ String.valueOf(threadId) + "] Readed : "
								+ strLine);
						String[] data = strLine.split("\\.");
						server = data[0];
						secret = data[1];
						id = data[2];
						farm = data[3];
					} else {
						workLeft = false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("[Thread " + String.valueOf(threadId)
							+ "] [ERROR] Error reading workfile");
					continue;
				}
			}
			tagsURL = "http://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key="
					+ this.APIKey + "&photo_id=" + id;
			fileXMLPath = PATH + "/xml/" + id + ".xml";
			fileTagPath = PATH + "/txt/" + id + ".xml";
			fileImagePath = PATH + "/images/" + id + ".jpg";
			try {
				getTags();
				System.out.println("[Thread " + String.valueOf(threadId)
						+ "] Downloading tags for image " + String.valueOf(id));
				downloadImage();
				System.out.println("[Thread " + String.valueOf(threadId)
						+ "] Downloading file for image " + String.valueOf(id));
			} catch (Exception e) {
				System.out.println("[Thread " + String.valueOf(threadId)
						+ "] [ERROR] Error while downloading" + id + " files. DELETING everything realted to this files.");
				File f = new File(fileXMLPath);
				if (f.exists())
					f.delete();
				f = new File(fileTagPath);
				if (f.exists())
					f.delete();
				f = new File(fileImagePath);
				if (f.exists())
					f.delete();

			}
		}

	}

	public void getTags() throws Exception {

		try {

			// Download XML File containing Tags
			URLConnection uc = new URL(tagsURL).openConnection();
			DataInputStream dis = new DataInputStream(uc.getInputStream());
			File fileXML = new File(fileXMLPath);
			File fileTag = new File(fileTagPath);
			// FileWriter fw = new FileWriter(fileXML);
			OutputStreamWriter fw = new OutputStreamWriter(
					new FileOutputStream(fileXML), "UTF-8");
			String nextline;

			while ((nextline = dis.readLine()) != null) {
				nextline = new String(nextline.getBytes(), "UTF8");
				fw.append(nextline);
			}
			dis.close();
			fw.close();
			// Read Downloaded File
			XMLInputFactory factory = XMLInputFactory.newInstance();

			String text = "";

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileXML);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("tags");

			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList fstNmElmntLst = fstElmnt
							.getElementsByTagName("tag");
					for (int k = 0; k < fstNmElmntLst.getLength(); k++) {
						Element fstNmElmnt = (Element) fstNmElmntLst.item(k);
						NodeList fstNm = fstNmElmnt.getChildNodes();
						text = text + " "
								+ ((Node) fstNm.item(0)).getNodeValue();
					}
				}

			}
			// Store extracted tags
			FileOutputStream fos = new FileOutputStream(fileTag);
			fos.write(text.getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			throw e;
		}
	}

	private void downloadImage() throws Exception {
		try {
			String flickrurl = "http://farm" + String.valueOf(farm)
					+ ".static.flickr.com/" + server + "/" + id + "_" + secret
					+ "_" + "z" + ".jpg";
			// http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}_[mstzb].jpg
			// http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{o-secret}_o.(jpg|gif|png)
			URL u = new URL(flickrurl);
			// String filename = id + ".jpg";
			// filename = filename.substring(filename.lastIndexOf("/") +
			// 1,filename.length());
			File newFile = new File(fileImagePath);

			BufferedInputStream inStream = new BufferedInputStream(
					u.openStream());

			FileOutputStream fos = new FileOutputStream(newFile);

			int read;

			while ((read = inStream.read()) != -1) {
				fos.write(read);
			}
			fos.flush();
			fos.close();
			inStream.close();
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	String APIKey;
	String PATH;
	String id;
	String server;
	String secret;
	String farm;
	BufferedReader br;
	int threadId;
	int jumpedImages;
	String tagsURL;
	String fileXMLPath;
	String fileTagPath;
	String fileImagePath;
}
