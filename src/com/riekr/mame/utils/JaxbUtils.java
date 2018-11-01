package com.riekr.mame.utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class JaxbUtils {

	private JaxbUtils() {
	}

	public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

	public static <T> T unmarshal(File file, Class<T> clazz) throws IOException, ParserConfigurationException, SAXException, JAXBException {
		try (InputStream is = new FileInputStream(file)) {
			final DocumentBuilder docBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			final Document document = docBuilder.parse(is);
			final org.w3c.dom.Element varElement = document.getDocumentElement();
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final JAXBElement<T> loader = unmarshaller.unmarshal(varElement, clazz);
			return loader.getValue();
		}
	}

	public static <T> T unmarshal(String xml, Class<T> clazz) throws IOException, ParserConfigurationException, SAXException, JAXBException {
		try (StringReader reader = new StringReader(xml)) {
			final DocumentBuilder docBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			final Document document = docBuilder.parse(new InputSource(reader));
			final org.w3c.dom.Element varElement = document.getDocumentElement();
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final JAXBElement<T> loader = unmarshaller.unmarshal(varElement, clazz);
			return loader.getValue();
		}
	}

	public static <T> T unmarshal(byte[] data, Class<T> clazz) throws IOException, ParserConfigurationException, SAXException, JAXBException {
		try (ByteArrayInputStream baos = new ByteArrayInputStream(data)) {
			final DocumentBuilder docBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			final Document document = docBuilder.parse(baos);
			final org.w3c.dom.Element varElement = document.getDocumentElement();
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final JAXBElement<T> loader = unmarshaller.unmarshal(varElement, clazz);
			return loader.getValue();
		}
	}

}
