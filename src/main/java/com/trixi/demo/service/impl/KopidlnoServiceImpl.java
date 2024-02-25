package com.trixi.demo.service.impl;

import com.trixi.demo.constant.Constant;
import com.trixi.demo.model.entity.*;
import com.trixi.demo.repository.*;
import com.trixi.demo.service.KopidlnoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class KopidlnoServiceImpl implements KopidlnoService {

    private final VillageRepository villageRepository;
    private final DistrictRepository districtRepository;

    /***
     * Main method for upload data from xml into database
     */
    public void saveDataFromXml() {
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(Constant.baseURL).openStream());
            Document document = getDocument(in);
            NodeList villagesList = document.getElementsByTagName(Constant.villageRootTag);
            for(int i=0; i<villagesList.getLength();i++) {
                Node node = villagesList.item(i);
                Village village = parseVillage(node);
                log.info("village: {}", village);
                villageRepository.save(village);
            }
            NodeList districtsList = document.getElementsByTagName(Constant.districtRootTag);
            for(int i=0; i<districtsList.getLength();i++) {
                Node node = districtsList.item(i);
                District district = parseDistrict(node);
                log.info("district: {}", district);
                districtRepository.save(district);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
    private Document getDocument(BufferedInputStream input) throws IOException, ParserConfigurationException, SAXException {
        ZipInputStream zis = new ZipInputStream(input);
        Document document = null;
        while (zis.getNextEntry() != null) {
            byte[] zipData = zis.readAllBytes();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            //
            document = builder.parse(new ByteArrayInputStream(zipData));
            document.getDocumentElement().normalize();
        }
        zis.close();
        return document;
    }

    /***
     * Method for parse District object from Document.
     * @param input
     * @return District
     */
    private District parseDistrict(Node input) {
        District district = new District();
        if (input.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) input;

            Node villageNode = element.getElementsByTagName(Constant.districtVillageTag).item(0);
            String villageCode = getTagValue(Constant.districtVillageCodeTag,(Element) villageNode);

            district.setCode(getTagValue(Constant.districtCodeTag, element));
            district.setName(getTagValue(Constant.districtNameTag, element));
            district.setVillageCode(villageCode);
        }
        return district;
    }

    /***
     * Method for parse Village object from Document.
     * @param input
     * @return Village
     */
    private Village parseVillage(Node input){
        Village village = new Village();
        if (input.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) input;

            village.setCode(getTagValue(Constant.villageCodeTag, element));
            village.setName(getTagValue(Constant.villageNameTag, element));
        }
        return village;
    }

    /***
     * Get value of tag from element
     * @param tag
     * @param element
     * @return String
     */
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

}
