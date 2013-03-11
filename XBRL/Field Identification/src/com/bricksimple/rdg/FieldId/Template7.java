package com.bricksimple.rdg.FieldId;

import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import com.bricksimple.rdg.FieldId.Template7Col;

//import com.mysql.jdbc.Connection;

public class Template7 {
    public ArrayList<Integer>    FirstColumn = new ArrayList<Integer>();
    public ArrayList<Integer>    LastColumn  = new ArrayList<Integer>();
    public ArrayList<Integer>    myUid = new ArrayList<Integer>();
}
