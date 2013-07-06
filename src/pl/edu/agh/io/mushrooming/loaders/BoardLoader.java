package pl.edu.agh.io.mushrooming.loaders;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 11.05.13
 * Time: 18:16
 */
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class BoardLoader {

    private static final String TAG = "MUSHROOMING:BOARDLOADER";
    private static final String XML_FIELDS = "fields";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";
    private static final String ATTR_VALUE = "value";
    private static final String RES_DRAWABLE = "drawable";
    private static final String XML_PLAYERS = "players";
    private static final String XML_ADDONS = "addons";
    private static final String XML_FOREGROUND = "foreground";

    private final InputSource inputBoard;
    private final Resources resources;
    private final String packageName;
    private Document dom;
    private List<Element> fields;
    private List<Element> players;
    private List<Element> addons;
    private List<Element> foreground;

    public BoardLoader(InputSource inputBoard, Resources resources, String packageName) {
        this.inputBoard = inputBoard;
        this.resources = resources;
        this.packageName = packageName;
    }

    public void load() {
        parseFile();
        loadData();
    }

    private void parseFile() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse(inputBoard);
        } catch (Exception e) {
            Log.d(TAG, "Parser exception");
        }
    }

    private void loadData() {
        fields = loadChildrenElements(XML_FIELDS);
        players = loadChildrenElements(XML_PLAYERS, true);
        addons = loadChildrenElements(XML_ADDONS);
        foreground = loadChildrenElements(XML_FOREGROUND);
    }

    private List<Element> loadChildrenElements(String rootName) {
        return loadChildrenElements(rootName, false);
    }

    private List<Element> loadChildrenElements(String rootName, boolean loadThumbs) {
        Node root = dom.getElementsByTagName(rootName).item(0);
        NodeList children = root.getChildNodes();
        int childrenCount = children.getLength();
        List<Element> elements = new ArrayList<Element>();

        for(int i = 0; i < childrenCount; i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = readElement(child, loadThumbs);
                elements.add(element);
            }
        }

        return elements;
    }

    private Element readElement(Node child, boolean loadThumbs) {
        String fileName = child.getTextContent();
        NamedNodeMap attrs = child.getAttributes();
        String x = attrs.getNamedItem(ATTR_X).getNodeValue();
        String y = attrs.getNamedItem(ATTR_Y).getNodeValue();
        Node valueNode = attrs.getNamedItem(ATTR_VALUE);

        String value;
        if(valueNode != null) {
            value = valueNode.getNodeValue();
        } else {
            value = "0";
        }

        int bitmapId = resources.getIdentifier(fileName, RES_DRAWABLE, packageName);
        Bitmap bitmap = BitmapFactory.decodeResource(resources, bitmapId);
        Bitmap miniBitmap = null;

        if(loadThumbs) {
            int miniId = resources.getIdentifier(fileName+"_thumb", RES_DRAWABLE, packageName);
            miniBitmap = BitmapFactory.decodeResource(resources, miniId);
        }

        return new Element(bitmap, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(value), miniBitmap);
    }

    public List<Element> getFields() {
        return fields;
    }

    public List<Element> getPlayers() {
        return players;
    }

    public List<Element> getAddons() {
        return addons;
    }

    public List<Element> getForeground() {
        return foreground;
    }
}