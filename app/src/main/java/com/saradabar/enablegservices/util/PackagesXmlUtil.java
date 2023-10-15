package com.saradabar.enablegservices.util;

import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class PackagesXmlUtil {

    static final String FILE_ENCODE = "UTF-8";
    public static final String PATH_SYSTEM_FILE = "/cache/../data/system/packages.xml";
    public static final String PATH_TMP_FILE = "/cache/.." + new File(Environment.getExternalStorageDirectory(), "packages.xml").getPath();
    final Document document;
    final NodeList elementsPackages;
    final NodeList elementsSharedUser;
    final NodeList elementsUpdated;

    PackagesXmlUtil(Document document) {
        this.document = document;
        Element documentElement = document.getDocumentElement();
        this.elementsPackages = documentElement.getElementsByTagName("package");
        this.elementsSharedUser = documentElement.getElementsByTagName("shared-user");
        this.elementsUpdated = documentElement.getElementsByTagName("updated-package");
    }

    public void grantPermission(String str, String str2) {
        Node node;
        if (!isPermissionGranted(str, str2)) {
            int queryOfName = queryOfName(this.elementsSharedUser, str);
            if (queryOfName != -1) {
                node = this.elementsSharedUser.item(queryOfName);
            } else {
                int queryOfName2 = queryOfName(this.elementsUpdated, str);
                if (queryOfName2 != -1) {
                    node = this.elementsUpdated.item(queryOfName2);
                } else {
                    int queryOfName3 = queryOfName(this.elementsPackages, str);
                    if (queryOfName3 != -1) {
                        node = this.elementsPackages.item(queryOfName3);
                    } else {
                        return;
                    }
                }
            }
            grantPermissionNode(node, str2);
        }
    }

    void grantPermissionNode(Node node, String str) {
        Node item = node.getChildNodes().item(queryOfTag(node.getChildNodes()));
        Element createElement = this.document.createElement("item");
        createElement.setAttribute("name", str);
        item.appendChild(createElement);
    }

    int queryOfTag(NodeList nodeList) {
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            if (nodeList.item(i).getNodeName().equals("perms")) {
                return i;
            }
        }
        return -1;
    }

    int queryOfName(NodeList nodeList, String str) {
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            if (Objects.equals(getAttribute(nodeList.item(i)), str)) {
                return i;
            }
        }
        return -1;
    }

    String getAttribute(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return null;
        }
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            if (attributes.item(i).getNodeName().equals("name")) {
                return attributes.item(i).getNodeValue();
            }
        }
        return null;
    }

    boolean isPermissionGranted(String str, String str2) {
        Node node;
        int queryOfName = queryOfName(this.elementsSharedUser, str);
        if (queryOfName != -1) {
            node = this.elementsSharedUser.item(queryOfName);
        } else {
            int queryOfName2 = queryOfName(this.elementsUpdated, str);
            if (queryOfName2 != -1) {
                node = this.elementsUpdated.item(queryOfName2);
            } else {
                int queryOfName3 = queryOfName(this.elementsPackages, str);
                if (queryOfName3 == -1) {
                    return false;
                }
                node = this.elementsPackages.item(queryOfName3);
            }
        }
        NodeList childNodes = node.getChildNodes();
        int queryOfTag = queryOfTag(childNodes);
        return queryOfTag != -1 && queryOfName(childNodes.item(queryOfTag).getChildNodes(), str2) != -1;
    }

    public void outputToSystem(IDchaService mDchaService) throws Exception {
        if (mDchaService != null) {
            File file = new File(new File(Environment.getExternalStorageDirectory(), "packages.xml").getPath());
            Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
            newTransformer.setOutputProperty("indent", "yes");
            newTransformer.setOutputProperty("encoding", FILE_ENCODE);
            newTransformer.transform(new DOMSource(this.document), new StreamResult(file));
            if (!mDchaService.copyUpdateImage(PATH_TMP_FILE, PATH_SYSTEM_FILE)) {
                file.delete();
                throw new SecurityException();
            }
            file.delete();
        } else {
            throw new NullPointerException();
        }
    }

    public static PackagesXmlUtil inputFromSystem(IDchaService mDchaService) throws Exception {
        if (mDchaService != null) {
            if (!mDchaService.copyUpdateImage(PATH_SYSTEM_FILE, PATH_TMP_FILE))
                throw new SecurityException();
            DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File file = new File(new File(Environment.getExternalStorageDirectory(), "packages.xml").getPath());
            Document parse = newDocumentBuilder.parse(file);
            file.delete();
            return new PackagesXmlUtil(parse);
        } else {
            throw new NullPointerException();
        }
    }
}